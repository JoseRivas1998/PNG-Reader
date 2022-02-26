package com.tcg.pngreader;

import com.tcg.pngreader.chunks.Chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PingDecoder {

    public static void decode(final File file) throws Exception {
        try (final var fileInput = new FileInputStream(file)) {
            readHeader(fileInput);
            final var pngFile = new PngFile(fileInput);
            System.out.println("Done parsing file :)");
        }
    }

    private static void readHeader(final FileInputStream file) throws IOException {
        final byte[] headerBytes = new byte[8];
        final byte[] expectedBytes = {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (file.read(headerBytes) != headerBytes.length) {
            throw new RuntimeException("Unable to read file header.");
        }
        if (!Arrays.equals(headerBytes, expectedBytes)) {
            throw new RuntimeException("Invalid header bytes.");
        }
    }

}
