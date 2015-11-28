package com.goyourfly.gdownloader.utils;

/**
 * Created by gaoyufei on 15/6/19.
 */
public class ByteUtils {
    public static float getKb(long b) {
        return ((float) b) / (1024);
    }

    public static float getMb(long b) {
        return ((float) b) / (1024 * 1024);
    }
}
