package com.afollestad.aimage.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Handles writing/reading images to and from the external disk cache.
 *
 * @author Aidan Follestad
 */
public class DiskCache {

    public DiskCache(Context context) {
        CACHE_DIR = context.getExternalCacheDir();
    }

    private static File CACHE_DIR;

    public void put(String key, Bitmap image) throws Exception {
        try {
            FileOutputStream os = new FileOutputStream(new File(CACHE_DIR, key));
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (Exception e) {
            throw e;
        }
    }

    public Bitmap get(String key) throws Exception {
        File fi = new File(CACHE_DIR, key);
        if(!fi.exists()) {
            throw new Exception("File '" + fi.getAbsolutePath() + "' not found.");
        }
        return BitmapFactory.decodeFile(fi.getAbsolutePath());
    }

    public void setCacheDirectory(File dir) {
        CACHE_DIR = dir;
    }
}
