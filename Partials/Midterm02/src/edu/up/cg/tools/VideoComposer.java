package edu.up.cg.tools;

import edu.up.cg.utils.ProcessRunner;
import edu.up.cg.utils.TempFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the final portrait-mode MP4 using FFmpeg.
 *
 * Pipeline:
 *   1. Build an MP4 clip for the opening image.
 *   2. Build one MP4 clip per media item (image → still clip, video → re-encoded clip).
 *   3. Build an MP4 clip for the closing map image.
 *   4. Concatenate all clips with a final loudness normalization pass.
 *
 * Portrait frame: 1080 x 1920 px (9:16).
 * Every clip is scaled to fill the frame while keeping the original aspect ratio;
 * remaining space is padded with black bars.
 */
public class VideoComposer {

    // Portrait frame size
    public static final int FRAME_W = 1080;
    public static final int FRAME_H = 1920;
    public static final int FPS     = 30;

    // Clip durations (seconds)
    private static final double DUR_OPENING = 4.0;
    private static final double DUR_PHOTO   = 5.0;
    private static final double DUR_CLOSING = 8.0;

    /**
     * FFmpeg video filter:
     *   scale to fit inside 1080x1920 (keeps aspect ratio), then
     *   pad any remaining space with black.
     */
    private static final String SCALE_PAD = String.format(
        "scale=%d:%d:force_original_aspect_ratio=decrease," +
        "pad=%d:%d:(ow-iw)/2:(oh-ih)/2:black",
        FRAME_W, FRAME_H, FRAME_W, FRAME_H);

    /** Final loudness normalization to YouTube standard. */
    private static final String LOUDNORM = "loudnorm=I=-14:TP=-1.5:LRA=7";

    private final TempFolder temp;

    /**
     * @param temp the temporary folder where intermediate clips are written
     */
    public VideoComposer(TempFolder temp) {
        this.temp = temp;
    }

    /**
     * Builds all clips and concatenates them into the final video file.
     *
     * @param openingImage    AI-generated opening PNG
     * @param openingAudio    narration MP3 for the opening
     * @param items           sorted media items (oldest first)
     * @param narrationAudios one MP3 per item (same order as items)
     * @param mapImage        closing map PNG with phrase overlay
     * @param closingAudio    narration MP3 for the closing
     * @param outputFile      destination MP4 file
     * @throws Exception if any FFmpeg step fails
     */
    public void compose(File openingImage, File openingAudio,
                        List<MediaItem> items, List<File> narrationAudios,
                        File mapImage, File closingAudio,
                        File outputFile) throws Exception {

        List<File> clips = new ArrayList<>();
        try {
            // Opening clip
            System.out.println("  Building opening clip...");
            clips.add(buildImageClip(openingImage, openingAudio, DUR_OPENING, "clip_00_opening"));

            // One clip per media item
            for (int i = 0; i < items.size(); i++) {
                System.out.printf("  Building media clip %d/%d...%n", i + 1, items.size());
                MediaItem item  = items.get(i);
                File      audio = narrationAudios.get(i);
                String    name  = String.format("clip_%02d_media", i + 1);

                File clip = (item.getType() == MediaItem.Type.PHOTO)
                        ? buildImageClip(item.getFile(), audio, DUR_PHOTO, name)
                        : buildVideoClip(item.getFile(), audio, name);
                clips.add(clip);
            }

            // Closing map clip
            System.out.println("  Building closing map clip...");
            clips.add(buildImageClip(mapImage, closingAudio, DUR_CLOSING, "clip_99_map"));

            // Concatenate everything
            System.out.println("  Concatenating " + clips.size() + " clips...");
            concatenate(clips, outputFile);

        } finally {
            // Clean up intermediate clips whether we succeeded or not
            for (File clip : clips) {
                if (clip != null && clip.exists()) clip.delete();
            }
        }
    }

    // clip buildere


    /**
     * Converts a still image + audio into a portrait MP4 clip.
     * The image is looped (-loop 1) for the given duration.
     */
    private File buildImageClip(File image, File audio, double duration, String name)
            throws Exception {

        File out = new File(temp.getPath(), name + ".mp4");
        String[] cmd = {
            "ffmpeg", "-y",
            "-loop", "1",
            "-i",    image.getAbsolutePath(),
            "-i",    audio.getAbsolutePath(),
            "-vf",   SCALE_PAD,
            "-c:v",  "libx264", "-preset", "fast", "-crf", "23",
            "-c:a",  "aac",     "-b:a",    "192k",
            "-t",    String.valueOf(duration),
            "-r",    String.valueOf(FPS),
            "-pix_fmt", "yuv420p",
            "-shortest",
            out.getAbsolutePath()
        };
        runFfmpeg(cmd, "image clip: " + image.getName());
        return out;
    }

    /**
     * Re-encodes a video file to portrait format, replacing its original audio
     * with the AI narration. Video stream: track 0, audio: narration MP3.
     */
    private File buildVideoClip(File video, File audio, String name)
            throws Exception {

        double dur = getVideoDuration(video);
        File out   = new File(temp.getPath(), name + ".mp4");

        String[] cmd = {
            "ffmpeg", "-y",
            "-i",  video.getAbsolutePath(),
            "-i",  audio.getAbsolutePath(),
            "-vf", SCALE_PAD,
            "-map", "0:v:0",
            "-map", "1:a:0",
            "-c:v", "libx264", "-preset", "fast", "-crf", "23",
            "-c:a", "aac",     "-b:a",    "192k",
            "-t",   String.valueOf(dur),
            "-r",   String.valueOf(FPS),
            "-pix_fmt", "yuv420p",
            "-shortest",
            out.getAbsolutePath()
        };
        runFfmpeg(cmd, "video clip: " + video.getName());
        return out;
    }

    /**
     * Writes a concat list file and calls FFmpeg to join all clips into one MP4.
     * A final loudness normalisation pass is applied.
     */
    private void concatenate(List<File> clips, File outputFile) throws Exception {
        File listFile = new File(temp.getPath(), "concat_list.txt");
        try (PrintWriter pw = new PrintWriter(listFile)) {
            for (File clip : clips) {
                // Single-quote the path; escape embedded single quotes
                pw.println("file '" + clip.getAbsolutePath().replace("'", "'\\''") + "'");
            }
        } catch (FileNotFoundException e) {
            throw new Exception("Could not write concat list: " + e.getMessage(), e);
        }

        String[] cmd = {
            "ffmpeg", "-y",
            "-f",    "concat",
            "-safe", "0",
            "-i",    listFile.getAbsolutePath(),
            "-c:v",  "libx264", "-preset", "fast", "-crf", "22",
            "-c:a",  "aac",     "-b:a",    "192k",
            "-af",   LOUDNORM,
            "-pix_fmt", "yuv420p",
            outputFile.getAbsolutePath()
        };
        runFfmpeg(cmd, "concatenation");
        listFile.delete();
    }


    /**
     * Uses ffprobe to detect the video duration in seconds.
     * Falls back to 5.0 s if detection fails.
     */
    private double getVideoDuration(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "quiet",
                "-show_entries", "format=duration",
                "-of",  "csv=p=0",
                file.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line = br.readLine();
                if (line != null && !line.isBlank()) {
                    return Double.parseDouble(line.trim());
                }
            }
            p.waitFor();
        } catch (Exception e) {
            System.out.println("  Could not detect video duration, using 5 s default.");
        }
        return 5.0;
    }

    /** Runs an FFmpeg command via ProcessRunner, wrapping any exception. */
    private void runFfmpeg(String[] cmd, String taskName) throws Exception {
        try {
            ProcessRunner.run(cmd, taskName);
        } catch (Exception e) {
            throw new Exception("FFmpeg failed for '" + taskName + "': " + e.getMessage(), e);
        }
    }
}
