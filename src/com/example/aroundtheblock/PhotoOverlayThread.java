
package com.example.aroundtheblock;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class PhotoOverlayThread extends Thread {
  private SurfaceHolder mSurfaceHolder;
  private PhotoOverlay mOverlay;
  private boolean mRun = false;

  public PhotoOverlayThread(SurfaceHolder surfaceHolder, PhotoOverlay overlay) {
    mSurfaceHolder = surfaceHolder;
    mOverlay = overlay;
  }

  public void setRunning(boolean run) {
    mRun = run;
  }

  @Override
    public void run() {
      Canvas c;
      while (mRun) {
        c = null;
        try {
          c = mSurfaceHolder.lockCanvas(null);
          synchronized (mSurfaceHolder) {
            mOverlay.onDraw(c);
          }
        } finally {
          // do this in a finally so that if an exception is thrown
          // during the above, we don't leave the Surface in an
          // inconsistent state
          if (c != null) {
            mSurfaceHolder.unlockCanvasAndPost(c);
          }
        }
      }
    }
}
