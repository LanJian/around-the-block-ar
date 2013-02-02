package com.example.aroundtheblock;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.view.Display;
import android.view.View;

public class PhotoOverlay extends View {


  private PhotoOverlayThread mThread;
  private Camera mCamera;
  private Activity mActivity;
  private double mXOffset, mYOffset;

  private List<Photo> mPhotos;

  public Bitmap toDraw;

  private int x;
  private boolean b;

  public int mWidth, mHeight;

  public PhotoOverlay(Activity activity, Camera camera) {
    super(activity);
    mActivity = activity;
    mCamera = camera;

    Display display = mActivity.getWindowManager().getDefaultDisplay(); 
    mWidth = display.getWidth();
    mHeight = display.getHeight();

    mXOffset = 0;
    mYOffset = 0;

    mPhotos = new ArrayList<Photo>();


    //getHolder().addCallback(this);
    //mThread = new PhotoOverlayThread(getHolder(), this);
    setFocusable(true);
  }




  @Override
  public void onDraw(Canvas canvas) {
    //System.err.println("draw: " + mPhotos.size());
    super.onDraw(canvas); 

    for (Photo photo: mPhotos) {
      photo.draw(canvas);
    }




    //canvas.drawColor(Color.BLACK);
    //System.err.println("xOffset: " + (int)(mXOffset + mWidth/2.0));
    //
    //Paint paint = new Paint(); 
    //paint.setStyle(Paint.Style.FILL); 
    //paint.setColor(Color.WHITE); 
    //canvas.drawText("Test Text", 50, 50, paint); 


  }


  public void update(float[] R, Location m) {
    for (Photo photo: mPhotos) {
      photo.update(mCamera, R, m);
    }

    invalidate();
  }

  public void clearPhotos() {
    mPhotos.clear();
  }

  public void addPhoto(Photo p) {
    mPhotos.add(p);
  }

}
