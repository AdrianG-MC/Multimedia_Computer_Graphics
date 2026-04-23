package edu.up.cg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Runs external command-line tools (FFmpeg, ExifTool, etc.) as subprocesses.
 *
 * Features:
 *   - Merges stdout and stderr to prevent the pipe buffer from blocking.
 *   - Waits for the process to finish.
 *   - Throws a descriptive exception when the exit code is non-zero.
 *
 * This is a utility class and cannot be instantiated.
 */
public final class ProcessRunner {

    private static final Logger LOG = Logger.getLogger(ProcessRunner.class.getName());

    private ProcessRunner() {}

    /**
     * Runs the given command and waits for it to finish.
     *
     * @param cmd      command and arguments as a String array
     * @param taskName human-readable description used in error messages
     * @throws ProcessException if the process exits non-zero or cannot start
     */
    public static void run(String[] cmd, String taskName) throws ProcessException {
        LOG.fine("Running: " + Arrays.toString(cmd));

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true); // merge stderr into stdout

            Process process = pb.start();

            // Drain the output so the pipe buffer never fills and blocks the process
            StringBuilder output = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOG.warning("Process failed [" + taskName + "]:\n" + output);
                throw new ProcessException(
                    "Command failed for '" + taskName + "' (exit code " + exitCode + ").\n" +
                    "Command: " + Arrays.toString(cmd));
            }

        } catch (IOException e) {
            throw new ProcessException(
                "Could not start '" + cmd[0] + "'. Is it installed and in PATH?\n" +
                e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessException("Process interrupted: " + taskName, e);
        }
    }


    /**
     * Thrown when a subprocess exits with a non-zero code or cannot be started.
     */
    public static class ProcessException extends Exception {

        public ProcessException(String message) {
            super(message);
        }

        public ProcessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
