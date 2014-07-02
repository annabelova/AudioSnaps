package com.audiosnaps.log;

import android.util.Log;

public class MyLog{
	
    private static boolean deactivated = true;
    
    public final static void v(String tag, String message){
        if(deactivated) return;
        Log.v(tag, message);
    }
    
    public final static void d(String tag, String message){
        if(deactivated) return;
        Log.d(tag, message);
    }
    
    public final static void i(String tag, String message){
        if(deactivated) return;
        Log.i(tag, message);
    }
    
    public final static void w(String tag, String message){
        if(deactivated) return;
        Log.w(tag, message);
    }
    
    public final static void e(String tag, String message){
        if(deactivated) return;
        Log.e(tag, message);
    }
    
    //Same for w and e if neccessary..
}
