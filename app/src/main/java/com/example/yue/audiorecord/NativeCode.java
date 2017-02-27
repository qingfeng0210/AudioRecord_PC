package com.example.yue.audiorecord;

/**
 * Created by qingf on 2017/2/17.
 */

public class NativeCode {
    static {
        System.loadLibrary("native-lib");
    }
    public static native String test();

}
