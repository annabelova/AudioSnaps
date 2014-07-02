package com.audiosnaps.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class AudioPlayer {

	// Media player
	private MediaPlayer mp;

	// audio file
	private File file;
	private FileInputStream fileInputStream;
	private OnCompletionListener listener;
	private Runnable runOnCompletion;

	public AudioPlayer() {

		this.listener = new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				try {
					mp.release();
					mp = null;
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	public AudioPlayer(final Runnable runOnCompletion) {
		
		this.runOnCompletion = runOnCompletion;
		
		this.listener = new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				try {
					mp.release();
					mp = null;
					fileInputStream.close();
					(new Thread(runOnCompletion)).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
	}

	public void setFileSource(File file) {
		this.file = file;
	}

	/**
	 * Plays audio once (in case the image is an AudioSnap)
	 */
	public void play() {

		// set up MediaPlayer
		mp = new MediaPlayer();

		mp.setOnCompletionListener(listener);

		try {
			fileInputStream = new FileInputStream(file);
			mp.setDataSource(fileInputStream.getFD());
			mp.prepare();
			mp.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void stop() {
		try {
			if (mp != null) {
				if (mp.isPlaying()) {
					mp.stop();
					mp.release();
					if(runOnCompletion != null) (new Thread(runOnCompletion)).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public boolean isPlaying() {
//		if (mp != null) {
//			return mp.isPlaying();
//		} else {
//			return false;
//		}
//
//	}

}
