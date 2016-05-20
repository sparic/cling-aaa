package org.fourthline.cling.demo.android.light;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by Ray.Fu on 2016/5/18.
 */
public class FileEvent {
    public final Bitmap file;

    public FileEvent(Bitmap file) {
        this.file = file;
    }

    public Bitmap getFile() {
        return file;
    }
}
