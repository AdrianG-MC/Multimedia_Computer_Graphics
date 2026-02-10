package edu.up.cg.Tools;

import edu.up.cg.Utils.RLEBlock;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Simple file handler for compressed images
// Format: width height tolerance numBlocks [block1] [block2] ...
// Each block: count R G B
public class FileHandler {

    // Save blocks to file
    public static void save(String filename, List<RLEBlock> blocks, int width, int height, int tolerance) throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

        // write header
        out.writeInt(width);
        out.writeInt(height);
        out.writeByte(tolerance);
        out.writeInt(blocks.size());

        // write blocks
        for (RLEBlock block : blocks) {
            out.writeByte(block.getCount());
            out.writeByte(block.getRed());
            out.writeByte(block.getGreen());
            out.writeByte(block.getBlue());
        }

        out.close();
    }

    // Load blocks from file
    public static CompressedData load(String filename) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(filename));

        // read header
        int width = in.readInt();
        int height = in.readInt();
        int tolerance = in.readByte() & 0xFF;
        int numBlocks = in.readInt();

        // read blocks
        List<RLEBlock> blocks = new ArrayList<>();
        for (int i = 0; i < numBlocks; i++) {
            int count = in.readByte() & 0xFF;
            int r = in.readByte() & 0xFF;
            int g = in.readByte() & 0xFF;
            int b = in.readByte() & 0xFF;
            blocks.add(new RLEBlock(r, g, b, count));
        }

        in.close();
        return new CompressedData(blocks, width, height, tolerance);
    }

    // Calculate file size
    public static long getFileSize(List<RLEBlock> blocks) {
        // header: 4+4+1+4 = 13 bytes
        // blocks: 4 bytes each
        return 13 + blocks.size() * 4;
    }

    // Helper class to store loaded data
    public static class CompressedData {
        public List<RLEBlock> blocks;
        public int width;
        public int height;
        public int tolerance;

        public CompressedData(List<RLEBlock> blocks, int width, int height, int tolerance) {
            this.blocks = blocks;
            this.width = width;
            this.height = height;
            this.tolerance = tolerance;
        }
    }
}

