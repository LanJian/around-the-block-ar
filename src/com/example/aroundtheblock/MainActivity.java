package com.example.aroundtheblock;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
  private Camera mCamera;
  private CameraPreview mPreview;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    //Remove title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    //Remove notification bar
    //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Create an instance of Camera
    mCamera = getCameraInstance();
    System.err.println("getCameraInstance: " + mCamera);

    // Create our Preview view and set it as the content of our activity.
    mPreview = new CameraPreview(this, mCamera);

    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    PhotoOverlay overlay = new PhotoOverlay(this);
    preview.addView(overlay);
  }

  //@Override
  //public boolean onCreateOptionsMenu(Menu menu) {
    //getMenuInflater().inflate(R.menu.activity_main, menu);
    //return true;
  //}

  /** A safe way to get an instance of the Camera object. */
  public static Camera getCameraInstance(){
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }
}
