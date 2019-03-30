package com.tcg.pngreader;

import java.io.File;
import java.io.IOException;

public class PNGReader {

    public static void main(String[] args) throws IOException {

        File f = new File(args[0]);
        PNGFile pngFile = new PNGFile(f.toPath());
        pngFile.printText();
        pngFile.printIDATData();
    }

}
