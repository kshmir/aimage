package com.afollestad.aimage;

import android.graphics.Bitmap;

public interface ImageListener {

    public abstract void onImageReceived(String source, Bitmap bitmap);
}
