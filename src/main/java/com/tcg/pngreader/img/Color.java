package com.tcg.pngreader.img;

public class Color {

    public final int r;
    public final int g;
    public final int b;
    public final int a;

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public String toString() {
        return String.format("rgba(%d, %d, %d, %d)", this.r, this.g, this.b, this.a);
    }

}
