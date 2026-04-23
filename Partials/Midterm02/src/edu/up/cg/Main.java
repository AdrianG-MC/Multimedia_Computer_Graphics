package edu.up.cg;

import edu.up.cg.services.AIService;
import edu.up.cg.services.MapService;
import edu.up.cg.tools.*;
import edu.up.cg.utils.ConsoleLogger;
import edu.up.cg.utils.EnvConfig;
import edu.up.cg.utils.ProjectFolder;
import edu.up.cg.utils.TempFolder;

import java.io.File;
import java.util.List;

/**
 * Entry point for the Video Creator.
 *
 * Full pipeline:
 *   1. Load media files from input/
 *   2. Extract EXIF metadata (GPS + date) with ExifTool
 *   3. Sort media chronologically (oldest → newest)
 *   4. Generate AI opening image (Gemini Imagen 3)
 *   5. Generate narration text per media item (Gemini 2.0 Flash)
 *   6. Generate closing inspirational phrase (Gemini 2.0 Flash)
 *   7. Synthesize narration audio (Google Cloud TTS) + normalize loudness
 *   8. Download map image (Geoapify) and overlay phrase
 *   9. Render final portrait video (FFmpeg)
 *  10. Save to output/Project_N/ and move input files there
 *
 * Required environment variables:
 *   GEMINI_API_KEY    — Google AI Studio key (also used for TTS)
 *   GEOAPIFY_API_KEY  — Geoapify Static Maps key
 *
 * Optional:
 *   GOOGLE_TTS_KEY    — separate Cloud TTS key (falls back to GEMINI_API_KEY)
 */
public class Main {

    /** Folder to put input media files into before running. */
    public static final String INPUT_DIR  = "input";

    /** Root folder where Project_N output folders are created. */
    public static final String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        ConsoleLogger.banner();

        // Env variables

        System.setProperty("GEMINI_API_KEY", "YOUR KEY HERE");
        System.setProperty("GEOAPIFY_API_KEY", "YOUR KEY HERE");

        try {
            EnvConfig.requireEnv("GEMINI_API_KEY");
            EnvConfig.requireEnv("GEOAPIFY_API_KEY");
        } catch (IllegalStateException e) {
            ConsoleLogger.error(e.getMessage());
            System.exit(1);
        }

        File      inputDir  = new File(INPUT_DIR);
        File      outputDir = new File(OUTPUT_DIR);
        TempFolder tempFolder = null;

        try {
            // load media
            ConsoleLogger.step(1, "Loading media files from input/");
            MediaLoader   loader = new MediaLoader(inputDir);
            List<File>    files  = loader.load();
            ConsoleLogger.info(files.size() + " file(s) found.");

            // exctract metadata
            ConsoleLogger.step(2, "Extracting EXIF metadata (GPS + date)");
            MetadataExtractor extractor = new MetadataExtractor();
            List<MediaItem>   items     = extractor.extractAll(files);
            ConsoleLogger.info(items.size() + " item(s) with valid metadata.");

            // Sort oldest to newest
            ConsoleLogger.step(3, "Sorting media chronologically");
            MediaSorter     sorter = new MediaSorter();
            List<MediaItem> sorted = sorter.sort(items);
            ConsoleLogger.ok("Sorted oldest → newest.");

            // Create temp folder
            tempFolder = new TempFolder("vc_work_");
            ConsoleLogger.info("Temp folder: " + tempFolder.getPath().getAbsolutePath());

            // AI content generator
            ConsoleLogger.step(4, "Generating AI content (Gemini)");
            AIService ai = new AIService();

            ConsoleLogger.progress("Generating opening AI image...");
            File openingImage = ai.generateOpeningImage(sorted, tempFolder);
            ConsoleLogger.ok("Opening image ready.");

            ConsoleLogger.progress("Generating narrations for each media item...");
            List<String> narrations = ai.generateNarrations(sorted);
            ConsoleLogger.ok("Narrations generated.");

            ConsoleLogger.progress("Generating inspirational closing phrase...");
            MediaItem first  = sorted.get(0);
            MediaItem last   = sorted.get(sorted.size() - 1);
            String    phrase = ai.generateInspirationalPhrase(first, last);
            ConsoleLogger.ok("Phrase: \"" + phrase + "\"");

            // TTS
            ConsoleLogger.step(5, "Generating narration audio (TTS + loudness normalisation)");
            AudioGenerator audio = new AudioGenerator();

            File openingAudio = audio.synthesize(
                "Welcome. Let's relive every moment of this journey.",
                tempFolder, "audio_opening");

            List<File> narrationAudios = audio.synthesizeAll(narrations, tempFolder);

            File closingAudio = audio.synthesize(
                "Thank you for watching. Until the next adventure.",
                tempFolder, "audio_closing");

            ConsoleLogger.ok("All audio files ready.");

            // Geoapify map maker
            ConsoleLogger.step(6, "Generating closing map (Geoapify)");
            MapService map      = new MapService();
            File       mapImage = map.generateMap(first, last, tempFolder);
            map.overlayPhrase(mapImage, phrase);
            ConsoleLogger.ok("Map image ready.");

            // Ffmpeg video part
            ConsoleLogger.step(7, "Rendering final portrait video (FFmpeg)");
            ProjectFolder project    = new ProjectFolder(outputDir);
            File          projectDir = project.create();
            ConsoleLogger.info("Project folder: " + projectDir.getAbsolutePath());

            File          outputVideo = new File(projectDir, "video.mp4");
            VideoComposer composer    = new VideoComposer(tempFolder);
            composer.compose(
                openingImage, openingAudio,
                sorted, narrationAudios,
                mapImage, closingAudio,
                outputVideo);
            ConsoleLogger.ok("Video rendered: " + outputVideo.getName());

            // Move to proyect folder
            ConsoleLogger.step(8, "Moving input files → project folder");
            loader.moveFilesTo(projectDir);
            ConsoleLogger.ok("Input folder emptied. Files saved to " + projectDir.getName());

            ConsoleLogger.done(outputVideo.getAbsolutePath());

        } catch (Exception e) {
            ConsoleLogger.error("Pipeline failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (tempFolder != null) {
                tempFolder.delete();
                ConsoleLogger.info("Temp folder cleaned up.");
            }
        }
    }
}
