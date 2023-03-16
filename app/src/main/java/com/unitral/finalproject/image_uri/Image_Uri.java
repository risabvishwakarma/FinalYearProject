package com.unitral.finalproject.image_uri;

import android.net.Uri;

import java.io.File;

public class Image_Uri {
    private static File file = null;
    private static final Uri imageUri = null;

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        Image_Uri.file = file;
    }

    public static Uri getImageUri() {
        assert file != null;
        return Uri.fromFile(file);
    }
}
