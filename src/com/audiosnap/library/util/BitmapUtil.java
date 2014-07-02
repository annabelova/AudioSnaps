package com.audiosnap.library.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.log.MyLog;

public class BitmapUtil {

	private static final String TAG = "BitmapUtil";

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int dp,  Context context) {
        
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = getPixels(dp, context);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
	
	public static int getPixels(int dp, Context context){

         final float scale = context.getResources().getDisplayMetrics().density;
         int px = (int) (dp * scale + 0.5f);

        return px;

    }
	
	public static File saveBitmap(Bitmap bitmap, File file) throws IOException{
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		
		//write the bytes in file
		FileOutputStream fo = new FileOutputStream(file);
		fo.write(bytes.toByteArray());

		// remember close de FileOutput
		fo.close();
		
		return file;
	}
	
	public static Bitmap clip(Bitmap bitmap, int x, int y){
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		if(h > w) return Bitmap.createBitmap(bitmap, 0, (h-w*x/y)/2, w, w*x/y);
		else return Bitmap.createBitmap(bitmap, (w - h*x/y) / 2, 0, h*x/y, h);
	}
	
	/* ROTATE BITMAP CORRECTLY * */
	public static Bitmap manageBitmapRotatio(int photoW, int photoH, Bitmap bitMap, int rotation) {

		/**********************************
		 * EXIF ORIENTATIONS 0: UNDEFINED 1: NORMAL 2: FLIP HORIZONTAL 3: ROTATE
		 * 180 4: FLIP VERTICAL 5: TRANSPOSE 6: ROTATE 90 7: TRANSVERSE 8:
		 * ROTATE 270
		 **********************************/

		Bitmap rotateBitmap = bitMap;

		switch (rotation) {
		case ExifInterface.ORIENTATION_UNDEFINED:
			break;
		case ExifInterface.ORIENTATION_NORMAL:
			break;
		case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			if (photoW > photoH) {
				Matrix matrix = new Matrix();
				matrix.setRotate(180);
				rotateBitmap = Bitmap.createBitmap(bitMap, 0, 0, bitMap.getWidth(), bitMap.getHeight(), matrix, false);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Bitmap resized and rotated size: " + rotateBitmap.getWidth() + ", " + rotateBitmap.getHeight());
			}
			break;
		case ExifInterface.ORIENTATION_FLIP_VERTICAL:
			break;
		case ExifInterface.ORIENTATION_TRANSPOSE:
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			if (photoW > photoH) {
				Matrix matrix = new Matrix();
				matrix.setRotate(90);
				rotateBitmap = Bitmap.createBitmap(bitMap, 0, 0, bitMap.getWidth(), bitMap.getHeight(), matrix, false);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Bitmap resized and rotated size: " + rotateBitmap.getWidth() + ", " + rotateBitmap.getHeight());
			}
			break;
		case ExifInterface.ORIENTATION_TRANSVERSE:
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			if (photoW > photoH) {
				Matrix matrix = new Matrix();
				matrix.setRotate(270);
				rotateBitmap = Bitmap.createBitmap(bitMap, 0, 0, bitMap.getWidth(), bitMap.getHeight(), matrix, false);
				if(BaseActivity.DEBUG) MyLog.d(TAG, "Bitmap resized and rotated size: " + rotateBitmap.getWidth() + ", " + rotateBitmap.getHeight());
			}
			break;

		default:
			break;
		}

		return rotateBitmap;
	}
}
