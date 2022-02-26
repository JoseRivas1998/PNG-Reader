package com.tcg.pngreader;

import com.tcg.pngreader.chunks.*;
import com.tcg.pngreader.img.PngData;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PngFile {

    private final List<Chunk> allChunks;
    private final ImageHeaderChunk header;
    private final List<DataChunk> dataChunks;
    private final byte[] data;

    private PaletteChunk pallete;

    public PngFile(final FileInputStream file) throws IOException, DataFormatException {
        this.allChunks = new ArrayList<>();
        this.dataChunks = new ArrayList<>();
        this.header = header(file);
        this.allChunks.add(this.header);
        while (file.available() > 0) {
            this.allChunks.add(this.decodeChunk(file));
        }
        if (!this.wasMostRecentChunkTrailer()) {
            throw new RuntimeException("Image file did not end with IEND chunk.");
        }
        this.data = new PngData(this.dataChunks).data;
    }

    private ImageHeaderChunk header(final FileInputStream file) throws IOException {
        final var header = decodeChunk(file);
        if (!(header instanceof ImageHeaderChunk)) {
            throw new RuntimeException("Image file must begin with IHDR chunk");
        }
        return (ImageHeaderChunk) header;
    }

    private byte[] decompressData() throws IOException, DataFormatException {
        try (final ByteArrayOutputStream decompressedBytesStream = new ByteArrayOutputStream()) {
            final var decompressor = new Inflater();
            decompressor.setInput(joinCompressedData());
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = decompressor.inflate(buffer)) > 0) {
                decompressedBytesStream.write(buffer, 0, bytesRead);
            }
            return decompressedBytesStream.toByteArray();
        }
    }

    private byte[] joinCompressedData() throws IOException {
        try (final ByteArrayOutputStream compressedBytesStream = new ByteArrayOutputStream()) {
            for (final var dataChunk : dataChunks) {
                dataChunk.writeToOutputStream(compressedBytesStream);
            }
            return compressedBytesStream.toByteArray();
        }
    }

    public Chunk decodeChunk(final FileInputStream file) throws IOException {
        final var lengthBytes = new byte[4];
        if (file.read(lengthBytes) != lengthBytes.length) {
            throw new RuntimeException("Unexpected end of file.");
        }
        final var length = ByteBuffer.wrap(lengthBytes).getInt();
        final var chunkType = new byte[4];
        if (file.read(chunkType) != chunkType.length) {
            throw new RuntimeException("Unexpected end of file.");
        }
        final var chunkTypeCode = ByteBuffer.wrap(chunkType).getInt();
        switch (chunkTypeCode) {
            case KnownChunkCodes.IHDR:
                return new ImageHeaderChunk(length, chunkType, file);
            case KnownChunkCodes.PLTE:
                if (this.pallete != null) {
                    throw new RuntimeException("Duplicate PLTE chunk.");
                }
                if (!this.dataChunks.isEmpty()) {
                    throw new RuntimeException("Found PLTE chunk after data chunk.");
                }
                this.pallete = new PaletteChunk(length, chunkType, file, this.colorType(), this.bitDepth());
                return this.pallete;
            case KnownChunkCodes.IDAT:
                if (this.header.colorType == 3 && this.pallete == null) {
                    throw new RuntimeException("No palette found before data.");
                }
                if (!this.dataChunks.isEmpty() && !this.wasMostRecentChunkData()) {
                    throw new RuntimeException("Found non data chunk in between data chunks.");
                }
                final var dataChunk = new DataChunk(length, chunkType, file);
                this.dataChunks.add(dataChunk);
                return dataChunk;
            case KnownChunkCodes.IEND:
                return new ImageTrailerChunk(length, chunkType, file);
            default:
                return new UnknownChunk(length, chunkType, file);
        }
    }

    private boolean wasMostRecentChunkData() {
        if (this.allChunks.isEmpty()) return false;
        return this.allChunks.get(this.allChunks.size() - 1).chunkCode() == KnownChunkCodes.IDAT;
    }

    private boolean wasMostRecentChunkTrailer() {
        if (this.allChunks.isEmpty()) return false;
        return this.allChunks.get(this.allChunks.size() - 1).chunkCode() == KnownChunkCodes.IEND;
    }

    public int getWidth() {
        return this.header.width;
    }

    public int getHeight() {
        return this.header.height;
    }

    public int bitDepth() {
        return this.header.bitDepth;
    }

    public int colorType() {
        return this.header.colorType;
    }

    public boolean interlace() {
        return this.header.interlaceMethod == 1;
    }

}
