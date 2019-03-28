package com.tcg.pngreader;

import java.nio.ByteBuffer;
import java.util.Set;

class PNGChunk {

    enum PNGChunkType {
        IHDR(0x49_48_44_52),
        PLTE(0x50_4C_54_45),
        IDAT(0x49_44_41_54),
        IEND(0x49_45_4E_44),
        tRNS(0x74_52_4E_53),
        gAMA(0x67_41_4D_41),
        cHRM(0x63_48_52_4D),
        sRGB(0x73_52_47_42),
        iCCP(0x69_43_43_50),
        tEXt(0x74_45_58_74),
        zTXt(0x7A_54_58_74),
        iTXt(0x69_54_58_74),
        bKGD(0x62_4B_47_44),
        pHYs(0x70_48_59_73),
        sBIT(0x73_42_49_54),
        sPLT(0x73_50_4C_54),
        hIST(0x68_49_53_54),
        tIME(0x74_49_4D_45),
        ;
        public final int type;

        PNGChunkType(int type) {
            this.type = type;
        }

        boolean isType(PNGChunk chunk) {
            return this.type == chunk.type;
        }

        boolean inSet(Set<PNGChunkType> set) {
            return set.contains(this);
        }

        boolean notInSet(Set<PNGChunkType> set) {
            return !inSet(set);
        }

        public static boolean isBefore(Set<PNGChunkType> set, PNGChunkType type) {
            return !type.inSet(set);
        }

        public static boolean isAfter(Set<PNGChunkType> set, PNGChunkType type) {
            return type.inSet(set);
        }

        public static boolean isBeforePLTEandIDAT(Set<PNGChunkType> set) {
            return isBefore(set, PLTE) && isBefore(set, IDAT);
        }

        public static boolean isAfterPLTEandBeforeIDAT(Set<PNGChunkType> set) {
            return isAfter(set, PLTE) && isBefore(set, IDAT);
        }

        public static boolean isBeforeIDAT(Set<PNGChunkType> set) {
            return isBefore(set, PLTE);
        }

    }
    private final int length;
    private final int type;
    final byte[] data;
    private final int crc;

    PNGChunk(byte[] bytes, int offset) {
        length = intFromBytes(bytes, offset);
        type = intFromBytes(bytes, offset + 4);
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            int srcIndex = offset + 8 + i;
            data[i] = bytes[srcIndex];
        }
        crc = intFromBytes(bytes, offset + 8 + length);
    }

    int size() {
        return length + 12;
    }

    static int intFromBytes(byte[] bytes, int start) {
        if (start + 3 >= bytes.length || start < 0) {
            throw new IllegalArgumentException("There must be at least 4 bytes left");
        }
        byte b0 = bytes[start];
        byte b1 = bytes[start + 1];
        byte b2 = bytes[start + 2];
        byte b3 = bytes[start + 3];
        return (b0 << 24) | ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
    }

    private String typeName() {
        StringBuilder name = new StringBuilder();
        byte[] typeBytes = ByteBuffer.allocate(4).putInt(this.type).array();
        for (int typeByte : typeBytes) {
            name.append((char) typeByte);
        }
        return name.toString();
    }

    @Override
    public String toString() {
        return String.format("%s: %d bytes, checksum = %X", typeName(), this.length, crc);
    }
}
