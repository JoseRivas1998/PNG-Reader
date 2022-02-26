package com.tcg.pngreader.chunks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataChunk extends Chunk{

    public DataChunk(int length, byte[] chunkType, FileInputStream file) throws IOException {
        super(length, chunkType, file);
        // todo implement
    }

    public void writeToOutputStream(final OutputStream os) throws IOException {
        os.write(this.data);
    }

}
