package com.moji.utils;


import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class ZipUtils {
    public void zip(File from, File to) throws IOException {
        FileOutputStream out = new FileOutputStream(to);
        ZipOutputStream zOut = new ZipOutputStream(out);
        zOut.setLevel(9);
        try {
            zipFile("", from, zOut);
        } finally {
            zOut.close();
        }
    }

    private void zipFile(String path, File input, ZipOutputStream zOut) throws IOException {
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            if (files != null) for (File f : files) {
                zipFile(path + input.getName() + (f.isDirectory() ? "/" : ""), f, zOut);
            }
        } else {
            ZipEntry zipEntry = new ZipEntry(path + (path.length() > 0 ? "/" : "") + input.getName());
            zOut.putNextEntry(zipEntry);
            try {
                IOUtils.copy(new BufferedInputStream(new FileInputStream(input)), zOut);
            } finally {
                zOut.close();
            }
        }
    }

}
