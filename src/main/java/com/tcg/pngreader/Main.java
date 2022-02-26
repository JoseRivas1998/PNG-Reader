package com.tcg.pngreader;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        try {
            PingDecoder.decode(new File("C:\\Users\\JoseR\\Pictures\\simplepng.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
