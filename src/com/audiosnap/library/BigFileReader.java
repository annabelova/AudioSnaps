package com.audiosnap.library;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.apache.commons.codec.binary.Hex;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.log.MyLog;

/**
 * BigFileReader contains two methods to read a file from a file system.
 * Two methods are used to illustrate optimized reading abilities
 * 
 * @author Serguei Eremenko sergeremenko@yahoo.com
 * @version 1.0
 */

public class BigFileReader {
	
	private static final String TAG = "BigFileReader";
	
	/**
	 * Default constuctor
	 */
	public BigFileReader() {
	}

	// Load bytes[] from Filein the SDCard
	public static byte[] read2array(File file) {
		
		if(BaseActivity.DEBUG) MyLog.v(TAG, TAG);
		
		byte[] data = null;
		try {
			InputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] byteArray = new byte[1024];
			int bytesRead = 0;

			while ((bytesRead = inputStream.read(byteArray)) != -1) {
				//if(BaseActivity.DEBUG) MyLog.v(TAG, new String(byteArray));
				byteArrayOutputStream.write(byteArray, 0, bytesRead);
			}
			
			data = byteArrayOutputStream.toByteArray();
			inputStream.close();
			byteArrayOutputStream.close();
			byteArray = null;
			//System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	// Load bytes[] from Filein the SDCard
		public static String read2hex(File file) {
			
			StringBuilder sb = new StringBuilder();
			
			try {
				InputStream inputStream = new FileInputStream(file);
				byte[] byteArray = new byte[1024];

				while (inputStream.read(byteArray) != -1) {
					sb.append(new String(Hex.encodeHex(byteArray)));
				}
				
				inputStream.close();
				byteArray = null;
				//System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sb.toString();
		}

	/**
	 * Reads a file storing intermediate data into an array.
	 * 
	 * @param file
	 *            the file to be read
	 * @return a file data
	 */
//	public static byte[] read2array(String file) throws Exception {
//		InputStream in = null;
//		byte[] out = new byte[0];
//		byte[] buf = null;
//		try {
//			in = new BufferedInputStream(new FileInputStream(file));
//			// the length of a buffer can vary
//			int bufLen = 20000 * 1024;
//			buf = new byte[bufLen];
//			byte[] tmp = null;
//			int len = 0;
//			while ((len = in.read(buf, 0, bufLen)) != -1) {
//				// extend array
//				tmp = new byte[out.length + len];
//				// copy data
//				System.arraycopy(out, 0, tmp, 0, out.length);
//				System.arraycopy(buf, 0, tmp, out.length, len);
//				out = tmp;
//				tmp = null;
//			}
//		} finally {
//			// always close the stream
//			if (in != null)
//				try {
//					in.close();
//					//buf = null;
//					//System.gc();
//				} catch (Exception e) {
//				}
//		}
//		return out;
//	}


	/**
	 * Creates a big file with given name
	 * 
	 * @param file
	 *            the file name
	 */
	public void createData(String file) throws Exception {
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		byte[] b = new byte[] { 0xC, 0xA, 0xF, 0xE, 0xB, 0xA, 0xB, 0xE };
		int c = 100000;
		for (int i = 0; i < c; i++) {
			os.write(b);
			os.flush();
		}
	}

}
