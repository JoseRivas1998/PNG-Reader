package com.tcg.pngreader.chunks;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Chunk {

    protected final int length;
    protected final byte[] chunkType;
    protected final byte[] data;
    protected final byte[] crc; // todo this might be an int or long later

    public Chunk(final int length, final byte[] chunkType, final FileInputStream file) throws IOException {
        this.length = length;
        this.chunkType = Arrays.copyOf(chunkType, chunkType.length);
        this.data = new byte[this.length];
        if (file.read(this.data) != this.length) {
            throw new RuntimeException("Unable to read chunk data");
        }
        this.crc = new byte[4];
        if (file.read(this.crc) != this.crc.length) {
            throw new RuntimeException("Unable to read chunk CRC");
        }
        this.validateCRC();
    }

    private void validateCRC() {
        // todo - implement
    }

    public int chunkCode() {
        return ByteBuffer.wrap(this.chunkType).getInt();
    }

}
