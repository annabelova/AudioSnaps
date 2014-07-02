package com.audiosnaps.classes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import com.audiosnaps.log.MyLog;

import com.apache.commons.codec.DecoderException;
import com.apache.commons.codec.binary.Base64;
import com.audiosnap.library.BigFileReader;
import com.audiosnap.library.KMPMatch;
import com.audiosnap.library.util.ArrayUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.ex.NoAudioException;

public class AudioSnapsFileCache {

	private final static String TAG = "GestionAudioSnaps";

	public static final String JPEG_FILE_PREFIX = "IMG_";
	public static final String JPEG_FILE_SUFFIX = ".jpg";
	public static String PHOTO_ALBUM_NAME = "AudioSnaps";
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	// Constructor
	public AudioSnapsFileCache() {
	}

	// Comprueba si el AudioSnap est� en la SD cacheado
	public boolean compruebaAudioSnapCacheado(String id) {

		// Comprobamos si hay SD montada, si no se fuerza descarga
		if (isMediaMounted()) {

			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + id);
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Comprobamos si existe file: " + file.toString());

			if (file.exists()) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "File exists in the SD, cacheamos");
				return true;
			} else {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "File doesn't exists in the SD, descargamos archivo");
				return false;
			}
		} else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "No hay SD Card, forzamos descarga archivo");
			return false;
		}
	}

	// Comprueba si el Audio está en la SD cacheado
	public boolean compruebaAudioCacheado(String id) {

		// Comprobamos si hay SD montada, si no se fuerza descarga
		if (isMediaMounted()) {

			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + "audio" + id);
			if(BaseActivity.DEBUG) MyLog.d(TAG, "Comprobamos si existe file: " + file.toString());

			if (file.exists()) {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "File exists in the SD, cacheamos");
				return true;
			} else {
				if(BaseActivity.DEBUG) MyLog.d(TAG, "File doesn't exists in the SD, creamos archivo");
				return false;
			}
		} else {
			if(BaseActivity.DEBUG) MyLog.d(TAG, "No hay SD Card, creamos archivo");
			return false;
		}
	}

	public File extraeAudio(File file, String id) throws UnsupportedEncodingException, NoAudioException, DecoderException {

		byte[] data_ = BigFileReader.read2array(file);
		
		//if(BaseActivity.DEBUG) MyLog.v(TAG, "cadena0: " + encoded + " " + BigFileReader.read2array(file).length);

		int start = KMPMatch.indexOf(data_, hexStringToByteArray("927c"));
		
		if (start > 500) throw new NoAudioException();

		byte[] data2_ = ArrayUtil.copyOfRange(data_, start + 2 + 50, data_.length);

		//if(BaseActivity.DEBUG) MyLog.v(TAG, "cadena1: " + encoded);

		int stop = KMPMatch.indexOf(data2_, hexStringToByteArray("ff"));

		StringBuilder m4a = new StringBuilder();

		m4a.append(new String(ArrayUtil.copyOfRange(data2_, 0, stop), "US-ASCII"));

		//if(BaseActivity.DEBUG) MyLog.v(TAG, "base64: " + m4a.toString() + ", length: " + m4a.toString().length());
		
		while (m4a.length() % 4 != 0) m4a.append("=");

		//if(BaseActivity.DEBUG) MyLog.v(TAG, "base64: " + m4a.toString() + ", length: " + m4a.toString().length());

		System.gc();

		return writeAudioFromBytes(Base64.decodeBase64(m4a.toString()), id);

	}

	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	// Comprueba la existencia de SD montada
	public boolean isMediaMounted() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return true;
		} else {
			return false;
		}
	}

	// Write File in SD Card
	public void writeFileFromBytes(byte[] data, String id) {
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + id;
			FileOutputStream out = new FileOutputStream(path);
			out.write(data);
			out.close();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File saved in the SD Card: " + id);
		} catch (Exception e) {
			e.printStackTrace();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File not saved in the SD Card, error");
		}
	}

	// Write File in SD Card
	private File writeAudioFromBytes(byte[] data, String id) {
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.CACHE_AUDIOSNAPS_FILES + "audio" + id;
			File file = new File(path);
			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File saved in the SD Card: " + id);

			return file;
		} catch (Exception e) {
			e.printStackTrace();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File not saved in the SD Card, error");
		}
		return null;
	}

	// Load bytes[] from Filein the SDCard
	public byte[] loadBytesFromFile(File file) {
		byte[] data = null;
		try {
			InputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] byteArray = new byte[1024];
			int bytesRead = 0;

			while ((bytesRead = inputStream.read(byteArray)) != -1) {
				byteArrayOutputStream.write(byteArray, 0, bytesRead);
			}
			inputStream.close();
			data = byteArrayOutputStream.toByteArray();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File loaded from SD Card: " + file.toString());
		} catch (Exception e) {
			e.printStackTrace();
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File not loaded from SD Card, error");
		}
		return data;
	}


	// Add picture to gallery to be uploaded later
	@SuppressLint("SimpleDateFormat")
	public String addPhotoToGallery(File file, Context context) {
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
			File albumFile = getAlbumDir();
			File imageFile = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumFile);
			moveFile(file.getAbsolutePath(), imageFile.getAbsolutePath());
			if(BaseActivity.DEBUG) MyLog.d(TAG, "File saved on gallery: " + imageFile.getAbsolutePath());
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			return imageFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	// Move File to File
	private void moveFile(String inputFile, String outputPath) {

		InputStream in = null;
		OutputStream out = null;
		try {

			in = new FileInputStream(inputFile);
			out = new FileOutputStream(outputPath);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			out.flush();
			out.close();
			out = null;
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Creates and gets Album directory
	public File getAlbumDir() {
		File storageDir = null;

		if (isMediaMounted()) {

			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();

			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						if(BaseActivity.DEBUG) MyLog.d(TAG, "failed to create directory");
						return null;
					}
				}
			}

		} else {
			if(BaseActivity.DEBUG) MyLog.v(TAG, "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	public String getAlbumName() {
		return PHOTO_ALBUM_NAME;
	}
	
	// Creates and gets Tmp directory
		public File getTmpDir() {
			
			File storageDir = null;

			if (isMediaMounted()) {

				storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.TMP_AUDIOSNAPS_FILES);

				if (storageDir != null) {
					if (!storageDir.mkdirs()) {
						if (!storageDir.exists()) {
							if(BaseActivity.DEBUG) MyLog.d(TAG, "failed to create directory");
							return null;
						}
					}
				}

			} else {
				if(BaseActivity.DEBUG) MyLog.v(TAG, "External storage is not mounted READ/WRITE.");
			}

			return storageDir;
		}
}
