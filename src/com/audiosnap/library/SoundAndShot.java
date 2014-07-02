package com.audiosnap.library;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.os.Environment;

import com.audiosnap.library.util.IOUtil;
import com.audiosnap.library.util.PCMUtil;
import com.audiosnaps.BaseActivity;

public class SoundAndShot {
	
	private String audioPath = null;
	private String jpegPath = null;
	
	public SoundAndShot(File file){
		
		try {
			
			jpegPath = file.getAbsolutePath();
			
			byte[] sequence = "RIFF".getBytes("ASCII");
			
			long start = System.currentTimeMillis();
			
			System.out.println("path: " + file.getAbsolutePath());
			
		    //byte[] data = BigFileReader.read2array(file.getAbsolutePath());
		    byte[] data = BigFileReader.read2array(file);
		    
		    long stop = System.currentTimeMillis();
	        System.out.println("read" + (stop - start) + "ms");
		    
	        start = System.currentTimeMillis();
			int pos = KMPMatch.indexOf(data, sequence);
			stop = System.currentTimeMillis();
	        System.out.println("find " + (stop - start) + "ms");
			
	        System.out.println("pos: " + pos);
	        
	        if(pos != -1){
				
				data = Arrays.copyOfRange(data, pos, data.length);
				
				System.out.println("RIFF");
				
				start = System.currentTimeMillis();
				
				String localFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + BaseActivity.TMP_AUDIOSNAPS_FILES;
				File localFiles = new File(localFilesPath);
		        if(!localFiles.exists()) localFiles.mkdirs();
		        
		        try {
		        	audioPath = File.createTempFile("tmp", ".pcm", localFiles).getAbsolutePath();
		    	} catch (IOException e) {
		    		throw new RuntimeException("Couldn't create tmp file", e);
		    	}
		        
		        stop = System.currentTimeMillis();
		        System.out.println("tmp " + (stop - start) + "ms");
		        
		        start = System.currentTimeMillis();
		        
		        // extract pcm
		        WaveHeader wave = new WaveHeader();
		        int HEADER_LENGTH = wave.read(new ByteArrayInputStream(data));
		        data = Arrays.copyOfRange(data, HEADER_LENGTH, data.length);
		        IOUtil.writeShortArrayToFile(
		        		PCMUtil.shorts(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)),
		        		new File(audioPath)
		        		);
		        stop = System.currentTimeMillis();
		        System.out.println("pcm " + (stop - start) + "ms");
			}
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getAudioPath(){
		return audioPath;
	}
	
	public String getPicturePath(){
		return jpegPath;
	}
	
	public boolean is(){
		return (audioPath != null);
	}
	
}
