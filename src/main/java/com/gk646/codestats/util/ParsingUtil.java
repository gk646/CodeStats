/*
 * MIT License
 *
 * Copyright (c) 2023 gk646
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gk646.codestats.util;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ParsingUtil {

    private ParsingUtil() {
    }

    public static @NotNull String getFileExtension(@NotNull String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
    public static int parseLargeNonUTFFile(Path path) throws IOException {
        int lines = 0;
        BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
        byte[] buffer = new byte[262144];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                if (buffer[i] == 10) {
                    lines++;
                }
            }
        }
        bis.close();
        return lines;
    }

    public static int parseSmallNonUTFFile(Path path) throws IOException {
        int lines = 0;
        for (var b : Files.readAllBytes(path)) {
            if (b == 10) {
                lines++;
            }
        }
        return lines;
    }

    public static Charset getCharsetFallback(String charsetName, Charset fallbackCharset) {
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException e) {
            return fallbackCharset;
        }
    }
}
