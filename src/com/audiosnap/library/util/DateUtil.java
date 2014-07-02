package com.audiosnap.library.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.audiosnaps.R;

import android.content.Context;
import com.audiosnaps.log.MyLog;

public class DateUtil {
	
	public static String formatTimeAgo(String date, Context context) throws ParseException{
		
		SimpleDateFormat  inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		inputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		Date now = new Date();
		Date start = inputFormat.parse(date);
		
		// Date difference in millis to seconds
		long diff = (now.getTime() - start.getTime()) / 1000;
		
		long sec = diff;
	    diff /= 60;
	    long min = diff;
	    diff /= 60;
	    long hour = diff;
	    diff /= 24;
	    long day = diff;
	    
		if (sec<20){
	        if (sec == 1) return context.getResources().getString(R.string.SECONDS_AGO).replace("%lld", Long.toString(sec));
	        else return context.getResources().getString(R.string.FEW_SECONDS_AGO);
	    }else if (sec<60){
	    	return context.getResources().getString(R.string.SECONDS_AGO).replace("%lld", Long.toString(sec));
	    }else if (min<60){
	        if (min<2) return context.getResources().getString(R.string.MINUTE_AGO).replace("%lld", Long.toString(min));
	        else return context.getResources().getString(R.string.MINUTES_AGO).replace("%lld", Long.toString(min));
	    }else if (hour<48){
	        if (hour<2) return context.getResources().getString(R.string.HOUR_AGO).replace("%lld", Long.toString(hour));
	        else return context.getResources().getString(R.string.HOURS_AGO).replace("%lld", Long.toString(hour));
	    }else if (day<15){
	        if (day<2) return context.getResources().getString(R.string.DAY_AGO).replace("%lld", Long.toString(day));
	        else return context.getResources().getString(R.string.DAYS_AGO).replace("%lld", Long.toString(day));
	    }else{
	        return context.getResources().getString(R.string.ON_DATE).replace("%@", date.substring(0, date.lastIndexOf(":", date.length())));
	    }
	}
}
