package com.audiosnap.library.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ViewUtil {
	
	private int screenWidth;
	private int screenHeight;
	
	/**
	 * Util for resizing views based on screen percentages
	 * 
	 * @param context
	 */
	
	public ViewUtil(Context context){
		
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		this.screenHeight = metrics.heightPixels;
		this.screenWidth = metrics.widthPixels;
	}
	
	public void resize(View view, float width, float height){
		if(height > 0) view.getLayoutParams().height = Float.valueOf(this.screenHeight * height).intValue();
		if(width > 0) view.getLayoutParams().width = Float.valueOf(this.screenWidth * width).intValue();
	}
	
	public void setRelativeLayoutMargins(View view, float left, float top, float right, float bottom){
		
		RelativeLayout.LayoutParams relativeParams = ((RelativeLayout.LayoutParams)view.getLayoutParams());
		
		relativeParams.setMargins(
				Float.valueOf(this.screenWidth * left).intValue(),  
				Float.valueOf(this.screenWidth * top).intValue(), 
				Float.valueOf(this.screenWidth * right).intValue(), 
				Float.valueOf(this.screenWidth * bottom).intValue() );
		
		view.setLayoutParams(relativeParams);
	}
	
	public void setLinearLayoutMargins(View view, float left, float top, float right, float bottom){
		
		LinearLayout.LayoutParams relativeParams = ((LinearLayout.LayoutParams)view.getLayoutParams());
		
		relativeParams.setMargins(
				Float.valueOf(this.screenWidth * left).intValue(),  
				Float.valueOf(this.screenWidth * top).intValue(), 
				Float.valueOf(this.screenWidth * right).intValue(), 
				Float.valueOf(this.screenWidth * bottom).intValue() );
		
		view.setLayoutParams(relativeParams);
	}
	
	public void resizeAdaptingToScreen(View view){
		
	}
	
}
