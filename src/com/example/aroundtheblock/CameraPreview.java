package com.example.aroundtheblock;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private static final String TAG = "CameraPreview";

  private SurfaceHolder mHolder;
  private Camera mCamera;
  private Activity mActivity;

  public CameraPreview(Activity activity, Camera camera) {
    super(activity);
    mCamera = camera;
    mActivity = activity;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0
    //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, now tell the camera where to draw the preview.
    try {
      System.err.println("holder: " + holder);
      System.err.println("mCamera: " + mCamera);
      mCamera.setPreviewDisplay(holder);
      mCamera.startPreview();
    } catch (IOException e) {
      Log.d(TAG, "Error setting camera preview: " + e.getMessage());
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
    mCamera.release();
  }

  public void setCameraDisplayOrientation() {
    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(0, info);
    int rotation = mActivity.getWindowManager().getDefaultDisplay()
      .getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0: degrees = 0; break;
      case Surface.ROTATION_90: degrees = 90; break;
      case Surface.ROTATION_180: degrees = 180; break;
      case Surface.ROTATION_270: degrees = 270; break;
    }

    int result;
    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;  // compensate the mirror
    } else {  // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    mCamera.setDisplayOrientation(result);
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.
    System.err.println("surfaceChanged");
    System.err.println(w + " " + h);

    if (mHolder.getSurface() == null){
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try {
      mCamera.stopPreview();
    } catch (Exception e){
      // ignore: tried to stop a non-existent preview
    }

    // set preview size and make any resize, rotate or
    // reformatting changes here
    Camera.Parameters parameters = mCamera.getParameters();
    List<Size> size = parameters.getSupportedPreviewSizes();
    parameters.setPreviewSize(size.get(0).width, size.get(0).height);
    mCamera.setParameters(parameters);
    setCameraDisplayOrientation();

    // start preview with new settings
    try {
      mCamera.setPreviewDisplay(mHolder);
      mCamera.startPreview();

    } catch (Exception e){
      Log.d(TAG, "Error starting camera preview: " + e.getMessage());
    }
  }
}
