package com.tcg.pngreader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.tcg.pngreader.PNGChunk.*;
import static com.tcg.pngreader.PNGChunk.PNGChunkType.*;

public class PNGFile {

    private List<PNGChunk> chunks;
    private int width;
    private int height;
    private int bitDepth;
    private int colorType;
    private int compressionMethod;
    private int filterMethod;
    private int interlaceMethod;
    private PNGColorType pngColorType;

    public PNGFile(Path path) throws IOException {
        readChunks(path);
        validateChunks();
        readIDHR();

    }

    private void readChunks(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        chunks = new ArrayList<>();
        int currentOffset = 8;
        while (currentOffset < bytes.length) {
            PNGChunk chunk = new PNGChunk(bytes, currentOffset);
            chunks.add(chunk);
            currentOffset += chunk.size();
        }
    }

    private void validateChunks() throws IOException {
        Set<PNGChunkType> typeSet = Collections.synchronizedSet(EnumSet.noneOf(PNGChunkType.class));
        if(!IHDR.isType(chunks.get(0)) || !PNGChunkType.IEND.isType(chunks.get(chunks.size() - 1))) {
            throw new IOException("Invalid PNG file.");
        }
        PNGChunk previous = null;
        for (PNGChunk chunk : chunks) {
            if(IHDR.isType(chunk)) {
                assetFileRules(IHDR.notInSet(typeSet));
                typeSet.add(IHDR);
            } else if(PLTE.isType(chunk)) {
                assetFileRules(PLTE.notInSet(typeSet), isBeforeIDAT(typeSet));
                typeSet.add(PNGChunkType.PLTE);
            } else if(PNGChunkType.IDAT.isType(chunk)) {
                if(IDAT.inSet(typeSet)) {
                    if(!PNGChunkType.IDAT.isType(previous)) {
                        throwInvalidFile();
                    }
                }
                typeSet.add(PNGChunkType.IDAT);
            } else if(IEND.isType(chunk)) {
                assetFileRules(IEND.notInSet(typeSet));
                typeSet.add(PNGChunkType.IEND);
            } else if(cHRM.isType(chunk)) {
                assetFileRules(cHRM.notInSet(typeSet), isBeforePLTEandIDAT(typeSet));
                typeSet.add(PNGChunkType.cHRM);
            } else if(gAMA.isType(chunk)) {
                assetFileRules(gAMA.notInSet(typeSet), isBeforePLTEandIDAT(typeSet));
                typeSet.add(PNGChunkType.gAMA);
            } else if(iCCP.isType(chunk)) {
                assetFileRules(iCCP.notInSet(typeSet), isBeforePLTEandIDAT(typeSet));
                typeSet.add(iCCP);
            } else if(sBIT.isType(chunk)) {
                assetFileRules(sBIT.notInSet(typeSet), isBeforePLTEandIDAT(typeSet));
                typeSet.add(sBIT);
            } else if(sRGB.isType(chunk)) {
                assetFileRules(sRGB.notInSet(typeSet), isBeforePLTEandIDAT(typeSet));
                typeSet.add(sRGB);
            } else if(bKGD.isType(chunk)) {
                assetFileRules(bKGD.notInSet(typeSet), isAfterPLTEandBeforeIDAT(typeSet));
                typeSet.add(bKGD);
            } else if(hIST.isType(chunk)) {
                assetFileRules(hIST.notInSet(typeSet), isAfterPLTEandBeforeIDAT(typeSet));
                typeSet.add(hIST);
            } else if(tRNS.isType(chunk)) {
                assetFileRules(tRNS.notInSet(typeSet), isAfterPLTEandBeforeIDAT(typeSet));
                typeSet.add(tRNS);
            } else if(pHYs.isType(chunk)) {
                assetFileRules(pHYs.notInSet(typeSet), isBeforeIDAT(typeSet));
                typeSet.add(pHYs);
            } else if(sPLT.isType(chunk)) {
                assetFileRules(isBeforeIDAT(typeSet));
                typeSet.add(pHYs);
            } else if(tIME.isType(chunk)) {
                assetFileRules(tIME.notInSet(typeSet));
                typeSet.add(tIME);
            }
            previous = chunk;
        }
    }

    private void throwInvalidFile() throws IOException {
        throw new IOException("Invalid PNG file.");
    }

    private void assetFileRules(boolean... rules) throws IOException {
        for (boolean rule : rules) {
            if(!rule) throwInvalidFile();
        }
    }

    private void readIDHR() throws IOException {
        PNGChunk idhr = chunks.get(0);
        width = intFromBytes(idhr.data, 0);
        height = intFromBytes(idhr.data, 4);
        bitDepth = idhr.data[8];
        colorType = idhr.data[9];
        compressionMethod = idhr.data[10];
        filterMethod = idhr.data[11];
        interlaceMethod = idhr.data[12];
        pngColorType = PNGColorType.fromInt(colorType);
        if(pngColorType == null) {
            throwInvalidFile();
        }
        if(!pngColorType.isDepthSupported(bitDepth)) {
            throwInvalidFile();
        }
    }

    private List<PNGChunk> getTextChunks() {
        List<PNGChunk> textChunks = new ArrayList<>();
        for (PNGChunk chunk : chunks) {
            if(iTXt.isType(chunk) || tEXt.isType(chunk) || zTXt.isType(chunk)) {
                textChunks.add(chunk);
            }
        }
        return textChunks;
    }

    private List<PNGChunk> getIDATChunks() {
        List<PNGChunk> IDATChunks = new ArrayList<>();
        for (PNGChunk chunk : chunks) {
            if(IDAT.isType(chunk)) {
                IDATChunks.add(chunk);
            }
        }
        return IDATChunks;
    }

    public void printText() {
        List<PNGChunk> textChunks = getTextChunks();
        for (PNGChunk chunk : textChunks) {
            StringBuilder keyword = new StringBuilder();
            StringBuilder text = new StringBuilder();
            if(tEXt.isType(chunk)) {
                boolean isText = false;
                for (int datum : chunk.data) {
                    if(datum == 0) {
                        isText = true;
                    } else if(isText) {
                        text.append((char) datum);
                    } else {
                        keyword.append((char) datum);
                    }
                }
            }
            System.out.printf("%s : %s\n", keyword, text);
        }
    }

    public void printIDATData() {
        List<PNGChunk> idatChunks = getIDATChunks();
        for (PNGChunk idatChunk : idatChunks) {
            System.out.println(idatChunk.dataHex());
        }
    }

    private byte[] idatData() throws IOException {
        List<PNGChunk> idats = getIDATChunks();
        byte[][] data = new byte[idats.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = PNGChunk.IDATData(idats.get(i));
        }
        return PNGChunk.combineAll(data);
    }

    private byte[] uncompressIDATData() throws IOException, DataFormatException {
        byte[] dat = idatData();
        byte[] temp = new byte[dat.length * 4];
        try(ByteArrayInputStream bais = new ByteArrayInputStream(dat);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Inflater inflater = new Inflater();
            inflater.setInput(dat);
            while(!inflater.finished()) {
                int size = inflater.inflate(temp);
                baos.write(temp, 0, size);
            }
            return baos.toByteArray();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitDepth() {
        return bitDepth;
    }
}
