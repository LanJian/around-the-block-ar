package com.example.aroundtheblock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class PhotoOverlay extends SurfaceView implements SurfaceHolder.Callback {

  private PhotoOverlayThread mThread;
  private int x;

  public PhotoOverlay(Context context) {
    super(context);
    System.err.println("photOverlay");
    getHolder().addCallback(this);
    mThread = new PhotoOverlayThread(getHolder(), this);
    setFocusable(true);
  }


  @Override
  public void onDraw(Canvas canvas) {

    Paint paint = new Paint();


    Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
        R.drawable.kangoo);
    canvas.drawColor(Color.BLACK);
    canvas.drawBitmap(kangoo, x, 10, null);
    x++;

  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {

  }

  public void surfaceCreated(SurfaceHolder holder) {

    mThread.setRunning(true);
    mThread.start();
  }

  public void surfaceDestroyed(SurfaceHolder holder) {

    boolean retry = true;
    mThread.setRunning(false);
    while (retry) {
      try {
        mThread.join();
        retry = false;
      } catch (InterruptedException e) {
        // we will try it again and again...
      }
    }
  }
}
