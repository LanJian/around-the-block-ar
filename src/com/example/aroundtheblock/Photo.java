package com.example.aroundtheblock;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;


class Photo {
  private String mUrl;
  private Location mLocation;
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

    mWidth = mOverlay.mWidth;
    mHeight = mOverlay.mHeight;

    new PhotoTask(this).execute(mUrl);
  }

  public void draw(Canvas canvas) {
    //System.err.println("draw: " + mVisible);
    if (!mVisible)
      return;

    Paint paint = new Paint();


    //Bitmap kangoo = BitmapFactory.decodeResource(mOverlay.getResources(),
        //R.drawable.kangoo);
    if (mBmp != null)
      canvas.drawBitmap(mBmp, (int)(mXOffset + mWidth/2.0),
          (int)(mYOffset + mHeight/2.0), null);
  }

  public void setBmp(Bitmap bmp) {
    mBmp = bmp;
  }

  public void update(Camera camera, float[] R, Location m) {
    //Location p = new Location(m);
    //p.setLatitude(p.getLatitude()-5);
    updateVisibility(camera, R, m, mLocation);
  }

  private void updateVisibility(Camera camera, float[] R, Location m, Location p) {
    float fov = camera.getParameters().getHorizontalViewAngle();

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

    if (thetaDeg > -fov/2 && thetaDeg < fov/2 &&
        vertThetaDeg > -vertFov/2 && vertThetaDeg < vertFov/2)
    {
      //System.err.println("visible");
      mVisible = true;
    } else {

      mVisible = false;
    }
  }

}
