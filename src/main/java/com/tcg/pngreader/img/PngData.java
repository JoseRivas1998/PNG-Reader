package com.tcg.pngreader.img;

import com.tcg.pngreader.chunks.DataChunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PngData {

    public final byte[] data;

    public PngData(final List<DataChunk> dataChunks) throws DataFormatException, IOException {
        final var decompressedData = this.decompressData(dataChunks);
        this.data = Arrays.copyOf(decompressedData, decompressedData.length);
    }

    private byte[] decompressData(final List<DataChunk> dataChunks) throws IOException, DataFormatException {
        try (final ByteArrayOutputStream decompressedBytesStream = new ByteArrayOutputStream()) {
            final var decompressor = new Inflater();
            decompressor.setInput(joinCompressedData(dataChunks));
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = decompressor.inflate(buffer)) > 0) {
                decompressedBytesStream.write(buffer, 0, bytesRead);
            }
            return decompressedBytesStream.toByteArray();
        }
    }

    private byte[] joinCompressedData(final List<DataChunk> dataChunks) throws IOException {
        try (final ByteArrayOutputStream compressedBytesStream = new ByteArrayOutputStream()) {
            for (final var dataChunk : dataChunks) {
                dataChunk.writeToOutputStream(compressedBytesStream);
            }
            return compressedBytesStream.toByteArray();
        }
    }

}
