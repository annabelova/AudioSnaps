package com.audiosnaps.share;

import android.content.Context;
import android.content.Intent;


public class Whatsapp {

	private static String root = "http://audiosnaps.com/k/";
	
	public static void share(Context context, String picHash) {

		Intent waIntent = new Intent(Intent.ACTION_SEND);
	   
		waIntent.setType("text/plain");
		
	    waIntent.setPackage("com.whatsapp");
	    
	    waIntent.putExtra(Intent.EXTRA_TEXT, root + picHash);
	    context.startActivity(Intent.createChooser(waIntent, "Share with"));
	    
	}
	
}
