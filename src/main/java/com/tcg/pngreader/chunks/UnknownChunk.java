package com.tcg.pngreader.chunks;

import java.io.FileInputStream;
import java.io.IOException;

public class UnknownChunk extends Chunk {

    public UnknownChunk(int length, byte[] chunkType, FileInputStream file) throws IOException {
        super(length, chunkType, file);
    }
}
