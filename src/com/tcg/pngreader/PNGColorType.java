package com.tcg.pngreader;

enum PNGColorType {
    Greyscale(0, 1, 2, 4, 8, 16),
    RGBTriple(2, 8, 16),
    PalletIndex(3, 1, 2, 4, 8),
    GrayScaleAlpha(4, 8, 16),
    RGBAlpha(6, 8, 16)
    ;
    final int type;
    final int[] supportedDepths;

    PNGColorType(int type, int... supportedDepths) {
        this.type = type;
        this.supportedDepths = supportedDepths;
    }

    static PNGColorType fromInt(int i) {
        for (PNGColorType value : PNGColorType.values()) {
            if(value.type == i) return value;
        }
        return null;
    }

    boolean isDepthSupported(int bitDepth) {
        for (int supportedDepth : supportedDepths) {
            if(supportedDepth == bitDepth) return true;
        }
        return false;
    }

}
