package com.example.aroundtheblock;

import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


public class PhotoTask extends AsyncTask<String, String, Bitmap> {
  public static final int THUMB_SIZE = 200;

  private Photo mPhoto;

  public PhotoTask(Photo p) {
    mPhoto = p;
  }

  protected Bitmap doInBackground(String... uri) {
    Bitmap bmp = null;
    while (bmp == null) {
      try {
        URL url = new URL(uri[0]);
        Bitmap tmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        bmp = Bitmap.createScaledBitmap(tmp, THUMB_SIZE, THUMB_SIZE, false);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return bmp;
  }

  protected void onPostExecute(Bitmap result) {
    super.onPostExecute(result);

    System.err.println("loaded image: " + result);
    mPhoto.setBmp(result);
  }
}
