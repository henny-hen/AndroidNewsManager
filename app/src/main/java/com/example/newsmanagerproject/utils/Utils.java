package com.example.newsmanagerproject.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Utils {

    public static String imgToBase64String(Bitmap image) {
        if (image == null) return "";
        
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
        int quality = 100;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);
    }

    public static Bitmap base64StringToImg(String input) {
        if (input == null || input.isEmpty()) return null;
        
        try {
            byte[] decodedBytes = Base64.decode(input, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeImage(Bitmap img) {
        return imgToBase64String(img);
    }

    public static Bitmap createScaledImage(Bitmap src, int w, int h) {
        if (src == null) return null;
        
        int finalw = w;
        int finalh = h;
        double factor;
        
        if (src.getWidth() > src.getHeight()) {
            factor = ((double) src.getHeight() / (double) src.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((double) src.getWidth() / (double) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        return Bitmap.createScaledBitmap(src, finalw, finalh, true);
    }

    public static String createScaledStrImage(String strSrc, int w, int h) {
        if (strSrc == null || strSrc.isEmpty()) return "";
        
        Bitmap src = base64StringToImg(strSrc);
        if (src == null) return strSrc;
        
        int finalw = w;
        int finalh = h;
        double factor;
        
        if (src.getWidth() > src.getHeight()) {
            factor = ((double) src.getHeight() / (double) src.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((double) src.getWidth() / (double) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        Bitmap resizedImg = Bitmap.createScaledBitmap(src, finalw, finalh, true);
        return imgToBase64String(resizedImg);
    }
}