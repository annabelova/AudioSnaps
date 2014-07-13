package com.audiosnap.library.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.audiosnaps.BaseActivity;

public class LocalLibraryUtil {
	
	public class LocalItem {
		public String photoPath = null;
		public String audioPath = null;
		
		public LocalItem(String photoPath, String audioPath) {
			this.photoPath = photoPath;
			this.audioPath = audioPath;
		}
		
		public Bitmap getImage() {
			if ( photoPath == null )
				return null;
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
			
			return bitmap;
		}
	}
	
	public ArrayList<LocalLibraryUtil.LocalItem> arrItems;
	
	public void load(Context context) {
		this.arrItems = new ArrayList<LocalLibraryUtil.LocalItem>();
		
		Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[]{
				BaseColumns._ID,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.ImageColumns.DATE_TAKEN};
		String order = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
		String selection =  MediaStore.Images.Media.DATA + " LIKE '%" +
				BaseActivity.STORAGE_PATH_PREFIX + "%" + BaseActivity.AS_FILE_SUFFIX + "'";

		final Cursor cursor = context.getContentResolver().query(imageUri, projection, selection, null, order);

		//int index = 0;
		if (cursor.moveToFirst()) {
			final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			//final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
			do {
				final String data = cursor.getString(dataColumn);
				
				LocalLibraryUtil.LocalItem item = new LocalLibraryUtil.LocalItem(data, null);
				this.arrItems.add(item);
				
			} while (cursor.moveToNext());
		}
		
		cursor.close();
	}
}
