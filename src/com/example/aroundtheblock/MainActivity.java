package com.example.aroundtheblock;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

public class MainActivity extends Activity {
  private Camera mCamera;
  private CameraPreview mPreview;
  private PhotoOverlay mOverlay;

  private LocationManager mLocationManager;
  private SensorManager mSensorManager;
  private Sensor mOrientation;
  private Sensor mRotation;

  private Location mLoc;
  private float[] mRot;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    //Remove title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    //Remove notification bar
    //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_main);

    // Create an instance of Camera
    mCamera = getCameraInstance();
    System.err.println("getCameraInstance: " + mCamera);

    // Create our Preview view and set it as the content of our activity.
    //mPreview = (CameraPreview) findViewById(R.id.camera_preview);

    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_frame_layout);

    mPreview = new CameraPreview(this, mCamera);
    //preview.addView(mPreview, 0);

    mOverlay = new PhotoOverlay(this, mCamera);
    //preview.addView(mOverlay, 1);

    setContentView(mPreview);
    addContentView(mOverlay, new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));

    mLocationManager = (LocationManager) this
        .getSystemService(Context.LOCATION_SERVICE);
    mSensorManager = (SensorManager) this
        .getSystemService(Context.SENSOR_SERVICE);


    mLoc = new Location(LocationManager.NETWORK_PROVIDER);

    registerLocationSensor();
    registerOrientationSensor();
    registerRotationSensor();

  }

  private void registerLocationSensor() {

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
      public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        if (location.distanceTo(mLoc) > 100)
        {
          System.err.println(location.getLatitude() + ", "
              + location.getLongitude());
          mLoc = location;
          String url = String.format("http://aroundtheblock.kanaflash.com/ar_photos?latitude=%f&longitude=%f",
              mLoc.getLatitude(), mLoc.getLongitude());
          new RequestTask(mOverlay).execute(url);
        }
      }

      public void onStatusChanged(String provider, int status,
          Bundle extras) {
      }

      public void onProviderEnabled(String provider) {
      }

      public void onProviderDisabled(String provider) {
      }
    };

    mLocationManager.requestLocationUpdates(
        LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
  }

  private void registerOrientationSensor() {
    mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    if (mOrientation != null) {
      SensorEventListener listener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent event) {
          //System.err.format("azimuth: %f, pitch: %f, roll: %f\n",
              //event.values[0], event.values[1], event.values[2]);
        }

      };
      mSensorManager.registerListener(listener, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }
  }

  private void registerRotationSensor() {
    mSensorManager = (SensorManager) this
        .getSystemService(Context.SENSOR_SERVICE);
    mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    if (mRotation != null) {
      SensorEventListener listener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent event) {
          float[] R = new float[16];
          float[] outR = new float[16];
          SensorManager.getRotationMatrixFromVector(R, event.values);
          SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
          mRot = outR;

          //Location p = new Location(mLoc);
          //p.setLatitude(mLoc.getLatitude() + 5);
          //p.setLongitude(mLoc.getLongitude() + 5);
          mOverlay.update(mRot, mLoc);

          //System.err.format("rotation:\n%f %f %f\n%f %f %f\n%f %f %f\n",
              //outR[0], outR[1], outR[2], outR[3], outR[4], outR[5], outR[6], outR[7], outR[8]);
        }

      };
      mSensorManager.registerListener(listener, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
    }
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
