package com.example.aroundtheblock;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;


public class Photo {

  public static final int THUMB_MIN_SIZE = 100;
  public static final int THUMB_MAX_SIZE = 400;

  private String mUrl;
  private Location mLocation;
  private float mDistance;
  private int mSize;
  private boolean mVisible;

  private PhotoOverlay mOverlay;
  private int mWidth, mHeight;
  private double mXOffset, mYOffset;

  private Bitmap mBmp;


  public Photo(PhotoOverlay overlay, double lat, double lon, String url) {
    mLocation = new Location(LocationManager.NETWORK_PROVIDER);
    mLocation.setLatitude(lat);
    mLocation.setLongitude(lon);

    mOverlay = overlay;

    mUrl = url;

    mXOffset = 0;
    mYOffset = 0;

    mDistance = 0;
    mSize = 0;

    mWidth = mOverlay.mWidth;
    mHeight = mOverlay.mHeight;

    new PhotoTask(this).execute(mUrl);
  }

  public void draw(Canvas canvas) {
    //System.err.println("draw: " + mVisible);
    if (!mVisible)
      return;

    Paint paint = new Paint();
    paint.setColor(Color.WHITE);

    int left = (int)(mXOffset + mWidth/2.0 - mSize/2.0);
    int right = left + mSize;
    int top = (int)(mYOffset + mHeight/2.0 - mSize/2.0);
    int bottom = top + mSize;
    int border = 10;


    //Bitmap kangoo = BitmapFactory.decodeResource(mOverlay.getResources(),
        //R.drawable.kangoo);
    if (mBmp != null) {
      canvas.drawRect(new Rect(left-border, top-border-30, right+border, bottom+border), paint);
      paint.setColor(Color.BLACK);
      paint.setTypeface(Typeface.create("Cambria", Typeface.NORMAL));
      //paint.setStyle(Paint.Style.FILL_AND_STROKE);
      //paint.setStrokeWidth(5);
      paint.setTextSize(30);
      canvas.drawText((int)mDistance + "m", left+(mSize/2)-40, top-border, paint);
      //paint.setStyle(Paint.Style.FILL);
      //paint.setColor(Color.rgb(225,40,90));
      //paint.setStrokeWidth(1);
      //canvas.drawText((int)mDistance + "m", left+(mSize/2)-40, top-border, paint);
      canvas.drawBitmap(mBmp, (int)(mXOffset + mWidth/2.0 - mSize/2.0),
          (int)(mYOffset + mHeight/2.0 - mSize/2.0), null);
    }
  }

  public void setBmp(Bitmap bmp) {
    Bitmap tmp = Bitmap.createScaledBitmap(bmp, mSize, mSize, false);
    mBmp = tmp;
  }

  public void update(Camera camera, float[] R, Location m) {
    //Location p = new Location(m);
    //p.setLatitude(p.getLatitude()-5);
    updateVisibility(camera, R, m, mLocation);
  }

  private void updateVisibility(Camera camera, float[] R, Location m, Location p) {
    float fov = camera.getParameters().getHorizontalViewAngle();

    mDistance = m.distanceTo(p);
    mSize = (int)((2000 - mDistance)*(THUMB_MAX_SIZE-THUMB_MIN_SIZE)/2000 + 100);
    mSize = Math.max(mSize, 100);
    mSize = Math.min(mSize, 400);

    // apply reverse rotation to vector p-m
    //Point4f pm = new Point4f((float)m.getLongitude(), 0, (float)m.getLatitude(), 1);
    //Point4f pp = new Point4f((float)p.getLongitude(), 0, (float)p.getLatitude(), 1);

    // x = longitude, y = latitude
    Point4f pm = new Point4f((float)m.getLongitude(), (float)m.getLatitude(), 0, 1);
    Point4f pp = new Point4f((float)p.getLongitude(), (float)p.getLatitude(), 0, 1);
    pp.sub(pm);
    Vector4f v = new Vector4f(pp);
    Matrix4f rot = new Matrix4f(R);
    //System.err.println(rot);
    rot.invert();
    Vector4f outV = new Vector4f();
    rot.transform(v, outV);

    //outV.normalize();
    //System.err.println("pm: " + pm);
    //System.err.println("pp: " + pp);
    //System.err.println(outV);

    // x = longitude, y = latitude, z = altitude
    Vector4f N = new Vector4f(0, 1, 0, 0);
    Vector4f PV = new Vector4f(outV);
    PV.z = 0;
    PV.normalize();
    double theta = Math.acos(PV.dot(N));
    theta = theta*1.46; // magic number

    Vector3f N3 = new Vector3f(0, 1, 0);
    Vector3f outV3 = new Vector3f(PV.x, PV.y, PV.z);
    N3.cross(N3, outV3);
    if (N3.z > 0) // negative angle
      theta = -theta;

    double thetaDeg = Math.toDegrees(theta);

    double xOffset = Math.tan(theta)*((mWidth/2.0)/Math.tan(Math.toRadians(fov/2.0)));

    mXOffset = xOffset;
    //if (N3.z < 0)
      //mXOffset = -xOffset;

    //System.err.println("theta: " + thetaDeg);
    //System.err.println("xOffset: " + mXOffset);

    double vertFov = mHeight/mWidth * fov;
    // x = longitude, y = latitude, z = altitude
    N = new Vector4f(0, 1, 0, 0);
    PV = new Vector4f(outV);
    PV.x = 0;
    PV.normalize();
    double vertTheta = Math.acos(PV.dot(N));
    //vertTheta = vertTheta*1.46; // magic number

    N3 = new Vector3f(0, 1, 0);
    outV3 = new Vector3f(PV.x, PV.y, PV.z);
    N3.cross(N3, outV3);
    if (N3.x > 0) // negative angle
      vertTheta = -vertTheta;

    //System.err.println(mWidth + " " + mHeight);
    double vertThetaDeg = Math.toDegrees(vertTheta);

    double yOffset = Math.tan(vertTheta)*((mHeight/2.0)/Math.tan(Math.toRadians(vertFov/2.0)));

    mYOffset = yOffset;

    if (thetaDeg > -fov/2-10 && thetaDeg < fov/2+10 &&
        vertThetaDeg > -vertFov/2-10 && vertThetaDeg < vertFov/2+10)
    {
      //System.err.println("visible");
      mVisible = true;
    } else {

      mVisible = false;
    }
  }

}
