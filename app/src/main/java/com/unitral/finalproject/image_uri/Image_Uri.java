package com.unitral.finalproject.image_uri;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public class Image_Uri {
    private static File file = null;
   public  static Bitmap imageBitmap = null;

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        Image_Uri.file = file;

    }

//    public static Bitmap getImageUri() {
//       // assert imageUri != null;
//        return imageBitmap;
//    }
//    public static void setImageUri(Bitmap imageBitmap) {
//
//        this.imageBitmap=imageBitmap;
//    }

}
