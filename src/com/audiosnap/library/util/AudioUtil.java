package com.audiosnap.library.util;

import android.content.Context;
import android.media.AudioManager;

public class AudioUtil {

	public static void mute(Context context, boolean on){
		
		AudioManager amanager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		amanager.setStreamMute(AudioManager.STREAM_DTMF,on);
		amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION,on);
		amanager.setStreamMute(AudioManager.STREAM_RING,on);
		amanager.setStreamMute(AudioManager.STREAM_SYSTEM,on);
		amanager.setStreamMute(AudioManager.STREAM_ALARM,on);
		amanager.setStreamMute(AudioManager.STREAM_MUSIC,on);
	}
	
}
