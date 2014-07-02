package com.audiosnap.library;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.log.MyLog;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

	private static final String TAG = "CameraPreview";

	private static final String SAMSUNG_GALAXY_S2 = "GT-I9100";
	private static final String SAMSUNG_GALAXY_S4 = "jflte";

	private static final double ASPECT_WIDTH = 3.0;
	private static final double ASPECT_HEIGHT = 4.0;

	 private static final double ASPECT_RATIO = ASPECT_WIDTH / ASPECT_HEIGHT;

	private final static String tag = "CameraPreview";

	private static final int PICTURE_SIZE_MAX_WIDTH = 768;
    private static final int PREVIEW_SIZE_MAX_WIDTH = BaseActivity.screenWidth;

	private SurfaceHolder mHolder;
	public Camera mCamera;
	private int mCameraId;
	private boolean inPreview;

	/*
	 * 0: auto 1: on 2: off
	 */

	public CameraPreview(Activity activity, int camera, int flash) {
		super(activity);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		// Find the total number of cameras available
		int numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == getCameraId(camera)) {
				mCameraId = i;
			}
		}

			mCamera = Camera.open(mCameraId);
		}
		else
			mCamera = Camera.open();
		
		setCamera(mCamera);

		Parameters params = mCamera.getParameters();
		params.setFlashMode(getFlashMode(flash));
		mCamera.setParameters(params);

	}

	private int getCameraId(int camera){
		if(camera == BaseActivity.BACK_CAMERA) 
			return CameraInfo.CAMERA_FACING_BACK; 
		else 
			return CameraInfo.CAMERA_FACING_FRONT;
	}

	private String getFlashMode(int flash){
		if(flash == BaseActivity.FLASH_ON) 
			return Parameters.FLASH_MODE_ON;
		else if(flash == BaseActivity.FLASH_AUTO)
			return Parameters.FLASH_MODE_AUTO;
		else return Parameters.FLASH_MODE_OFF;
	}

	private void setCamera(Camera camera) {

		if (mCamera != null) {
			// get camera parameters
			Parameters params = mCamera.getParameters();

			Size bestPreviewSize = determineBestPreviewSize(params);
	        Size bestPictureSize = determineBestPictureSize(params);

	        params.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
	        params.setPictureSize(bestPictureSize.width, bestPictureSize.height);

			// fix camera parameters
			params.setPictureFormat(ImageFormat.JPEG);
			// params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			params.setJpegThumbnailSize(0, 0);
			params.setJpegQuality(80);
			params.removeGpsData();
			params.setZoom(0);
 
			//if(BaseActivity.DEBUG) MyLog.e("focus areas", params.getMaxNumFocusAreas() + "");

			// set focus to auto
			List<String> focusModes = params.getSupportedFocusModes();
//			if (focusModes.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {
//				params.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
//				if(BaseActivity.DEBUG) MyLog.e("focus mode", "mode edof");
//			}else 
			/*if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) 
					&& !android.os.Build.DEVICE.startsWith(SAMSUNG_GALAXY_S2)
					&& !android.os.Build.DEVICE.startsWith(SAMSUNG_GALAXY_S4)) { 
				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				if(BaseActivity.DEBUG) MyLog.e("focus mode", "mode continous picture");
			} else*/ if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				//useAutfocusImpl = true;
				if(BaseActivity.DEBUG) MyLog.e("focus mode", "mode auto");
			}

			// Action mode take pictures of fast moving objects
			// List<String> sceneModes = params.getSupportedSceneModes();
			// if (sceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION))
			// params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
			// else
			// params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

			// set Camera parameters
			mCamera.setParameters(params);

			mCamera.setDisplayOrientation(90);

		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.

		mCamera.setDisplayOrientation(90);

		try {
			mCamera.setPreviewDisplay(holder);
			inPreview = true;
		} catch (IOException e) {
			//if (useAutfocusImpl)
			//	mAutoFocusHandler.removeCallbacks(mAutoFocusRunnable);
			mCamera.release();
			mCamera = null;
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stop();
	}

	public void stop() {
		if (null == mCamera) {
			return;
		}
		mCamera.stopPreview();
		//if (useAutfocusImpl)
		//	mAutoFocusHandler.removeCallbacks(mAutoFocusRunnable);
		mCamera.release();
		mCamera = null;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		mCamera.setParameters(parameters);
		mCamera.startPreview();

		// Launch autofocus mode
		//if (useAutfocusImpl)
		//	mAutoFocusHandler.postDelayed(mAutoFocusRunnable, AUTOFOCUS_FREQUENCY);
	}

	protected void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	public void flip() {

		// swap the id of the camera to be used
		if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}

		launch();
	}

	public void flash(int flash) {
		Parameters params = mCamera.getParameters();
		params.setFlashMode(getFlashMode(flash));
		mCamera.setParameters(params);
	}

	private void launch() {

		if (inPreview) mCamera.stopPreview();

		// NB: if you don't release the current camera before switching, you app
		// will crash
		mCamera.release();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(mCameraId);
		}
		else
		{
			mCamera = Camera.open();
		}
		setCamera(mCamera);

		try {
			// this step is critical or preview on new camera will no know where
			// to render to
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		mCamera.startPreview();

		// Launch autofocus mode
		//if (useAutfocusImpl)
		//	mAutoFocusHandler.postDelayed(mAutoFocusRunnable, AUTOFOCUS_FREQUENCY);
	}

	/**
     * Measure the view and its content to determine the measured width and the
     * measured height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width > height * ASPECT_RATIO) {
             width = (int) (height * ASPECT_RATIO + .5);
        } else {
            height = (int) (width / ASPECT_RATIO + .5);
        }

        setMeasuredDimension(width, height);
    }

	private Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
    }

    protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;

        for (Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / ASPECT_HEIGHT) == (currentSize.height / ASPECT_WIDTH);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) return sizes.get(0);

        return bestSize;
    }

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if(BaseActivity.DEBUG) MyLog.d(tag, "onAutoFocus()");
	}

}