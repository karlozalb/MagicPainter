package com.projectclean.magicpainterforkids.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Carlos Albaladejo Pérez on 14/03/2016.
 */
public class BitmapUtils {

    public static String getUniqueFilename(){
        File mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd-hhmmss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String currentDate = dateFormatter.format(today);

        String fullFilename = mediaPath.getAbsolutePath()+"/MagicPainter-"+currentDate+".jpg";

        return fullFilename;
    }

    public static String saveBitmapToMediaDirectory(Activity pcontext,Bitmap psource,String fullFilename){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fullFilename);
            psource.compress(Bitmap.CompressFormat.JPEG, 50, out); // bmp is your Bitmap instance

            MediaScannerConnection.scanFile(pcontext,
                    new String[]{fullFilename}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fullFilename;
    }

    public static Bitmap merge(Bitmap pbackground,Bitmap pmain){
        Bitmap bmOverlay = Bitmap.createBitmap(pbackground.getWidth(), pbackground.getHeight(), pbackground.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(pbackground, new Matrix(), null);
        canvas.drawBitmap(pmain, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
