package com.tcg.pngreader.chunks;

import com.tcg.pngreader.img.Color;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PaletteChunk extends Chunk {

    public final int size;
    private final Color[] colors;

    public PaletteChunk(int length, byte[] chunkType, FileInputStream file, final int colorType, final int bitDepth) throws IOException {
        super(length, chunkType, file);
        if (colorType == 0 || colorType == 4) {
            throw new RuntimeException("This color type must not contain a palette.");
        }
        if (super.length % 3 != 0) {
            throw new RuntimeException("Palette length must be divisible by 3.");
        }
        this.size = this.length / 3;
        if (colorType == 3 && (this.size > (1 << bitDepth))) {
            throw new RuntimeException("The palette has more entries than supported by the bit depth.");
        }
        this.colors = new Color[this.size];
        final var bytes = ByteBuffer.wrap(this.data);
        for (int i = 0; i < this.colors.length; i++) {
            final int r = bytes.get() & 0xFF;
            final int g = bytes.get() & 0xFF;
            final int b = bytes.get() & 0XFF;
            final int a = (1 << bitDepth) - 1;
            this.colors[i] = new Color(r, g, b, a);
        }
    }

    public Color get(final int i) {
        return this.colors[i];
    }

}
