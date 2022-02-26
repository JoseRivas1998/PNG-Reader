package com.tcg.pngreader.chunks;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageHeaderChunk extends Chunk {

    public final int width;
    public final int height;
    public final int bitDepth;
    public final int colorType;
    public final int compressionMethod;
    public final int filterMethod;
    public final int interlaceMethod;

    public ImageHeaderChunk(int length, byte[] chunkType, FileInputStream file) throws IOException {
        super(length, chunkType, file);
        final var dataBytes = ByteBuffer.wrap(this.data);
        this.width = dataBytes.getInt();
        this.height = dataBytes.getInt();
        this.bitDepth = dataBytes.get() & 0xFF;
        this.colorType = dataBytes.get() & 0xFF;
        this.compressionMethod = dataBytes.get() & 0xFF;
        this.filterMethod = dataBytes.get() & 0xFF;
        this.interlaceMethod = dataBytes.get() & 0xFF;
        this.validateBitDepthAndColorType();
        this.validateCompressionMethod();
        this.validateFilterMethod();
        this.validateInterlaceMethod();
    }

    private void validateInterlaceMethod() {
        if (this.interlaceMethod != 0 && this.interlaceMethod != 1) {
            throw new RuntimeException("Invalid interlace method");
        }
    }

    private void validateFilterMethod() {
        if (this.filterMethod != 0) {
            throw new RuntimeException("Invalid filter method");
        }
    }

    private void validateCompressionMethod() {
        if (this.compressionMethod != 0) {
            throw new RuntimeException("Invalid compression method");
        }
    }

    private void validateBitDepthAndColorType() {
        switch (this.colorType) {
            case 0:
                switch (this.bitDepth) {
                    case 1:
                    case 2:
                    case 4:
                    case 8:
                    case 16:
                        break;
                    default:
                        throw new RuntimeException("Invalid bit depth.");
                }
                break;
            case 2:
            case 4:
            case 6:
                switch (this.bitDepth) {
                    case 8:
                    case 16:
                        break;
                    default:
                        throw new RuntimeException("Invalid bit depth.");
                }
                break;
            case 3:
                switch (this.bitDepth) {
                    case 1:
                    case 2:
                    case 4:
                    case 8:
                        break;
                    default:
                        throw new RuntimeException("Invalid bit depth.");
                }
                break;
            default:
                throw new RuntimeException("Invalid color type.");
        }
    }
}
