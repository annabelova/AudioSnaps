package com.audiosnap.library;

import java.io.IOException;

import com.audiosnap.library.util.BitmapUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

public class BitmapLoader {

	private static int getScale(int originalWidth, int originalHeight, final int requiredWidth, final int requiredHeight) {
		// a scale of 1 means the original dimensions
		// of the image are maintained
		int scale = 1;

		// calculate scale only if the height or width of
		// the image exceeds the required value.
		if ((originalWidth > requiredWidth) || (originalHeight > requiredHeight)) {
			// calculate scale with respect to
			// the smaller dimension
			if (originalWidth < originalHeight)
				scale = Math.round((float) originalWidth / requiredWidth);
			else
				scale = Math.round((float) originalHeight / requiredHeight);

		}

		return scale;
	}

	private static BitmapFactory.Options getOptions(String filePath, int requiredWidth, int requiredHeight) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		// setting inJustDecodeBounds to true
		// ensures that we are able to measure
		// the dimensions of the image,without
		// actually allocating it memory
		options.inJustDecodeBounds = true;

		// decode the file for measurement
		BitmapFactory.decodeFile(filePath, options);

		// obtain the inSampleSize for loading a
		// scaled down version of the image.
		// options.outWidth and options.outHeight
		// are the measured dimensions of the
		// original image
		options.inSampleSize = getScale(options.outWidth, options.outHeight, requiredWidth, requiredHeight);

		// set inJustDecodeBounds to false again
		// so that we can now actually allocate the
		// bitmap some memory
		options.inJustDecodeBounds = false;

		return options;

	}

	public static Bitmap loadBitmap(String filePath, int requiredWidth, int requiredHeight) {

		BitmapFactory.Options options = getOptions(filePath, requiredWidth, requiredHeight);

		return BitmapFactory.decodeFile(filePath, options);
	}

	private static int getScale(int originalWidth, int originalHeight, final int requiredSmallSide) {
		// a scale of 1 means the original dimensions
		// of the image are maintained
		int scale = 1;

		// calculate scale only if the height or width of
		// the image exceeds the required value.
		if ((originalWidth > requiredSmallSide) || (originalHeight > requiredSmallSide)) {
			// calculate scale with respect to
			// the smaller dimension
			if (originalWidth < originalHeight)
				scale = Math.round((float) originalWidth / requiredSmallSide);
			else
				scale = Math.round((float) originalHeight / requiredSmallSide);

		}

		return scale;
	}

	private static BitmapFactory.Options getOptions(String filePath, int requiredSmallSide) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		// setting inJustDecodeBounds to true
		// ensures that we are able to measure
		// the dimensions of the image,without
		// actually allocating it memory
		options.inJustDecodeBounds = true;

		// decode the file for measurement
		BitmapFactory.decodeFile(filePath, options);

		// obtain the inSampleSize for loading a
		// scaled down version of the image.
		// options.outWidth and options.outHeight
		// are the measured dimensions of the
		// original image
		options.inSampleSize = getScale(options.outWidth, options.outHeight, requiredSmallSide);

		// set inJustDecodeBounds to false again
		// so that we can now actually allocate the
		// bitmap some memory
		options.inJustDecodeBounds = false;

		return options;

	}

	public static Bitmap loadBitmap(String filePath, int requiredSmallSide) {

		BitmapFactory.Options options = getOptions(filePath, requiredSmallSide);

		int rotation = 1;
		
		try {
			ExifInterface exif = new ExifInterface(filePath);
			rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// suponemos que el out mantiene las proporciones con la foto original
		return BitmapUtil.manageBitmapRotatio(options.outWidth, options.outHeight, BitmapFactory.decodeFile(filePath, options), rotation);
	}

}
