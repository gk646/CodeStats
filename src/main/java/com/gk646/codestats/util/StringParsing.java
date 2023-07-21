package com.gk646.codestats.util;

public class StringParsing {
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) || (dotIndex == fileName.length()-1) ? "" : fileName.substring(dotIndex + 1);
    }
}
