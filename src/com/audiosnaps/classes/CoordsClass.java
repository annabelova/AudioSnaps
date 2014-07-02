package com.audiosnaps.classes;

import android.graphics.PointF;
import android.graphics.RectF;
import com.audiosnaps.log.MyLog;

import com.audiosnaps.BaseActivity;


public class CoordsClass {
	
	private String logTag = "CoordinatesTranslator";
	
	private int translationWidth, translationHeight, horizontalThreshold, verticalThreshold;
	
	public CoordsClass(int stampWidth, int stampHeight, int stampMargin, int heightMargin){
		
		// translation 
		translationWidth = (BaseActivity.screenWidth + stampWidth) / 2 + stampMargin;
		translationHeight = (BaseActivity.screenHeight + stampHeight) / 2;

		// screen margins
		horizontalThreshold = (BaseActivity.screenWidth - stampWidth) / 2 - stampMargin;
		verticalThreshold = (BaseActivity.screenHeight - stampHeight) / 2 - heightMargin/2;
	
	}
	
	public PointF getTranslationPoint(RectF rect){
		
		PointF point = new PointF();

		point.x = rect.right - translationWidth;
		point.y = rect.bottom - translationHeight;

		if ((int) point.x > horizontalThreshold) point.x = horizontalThreshold;
		if(BaseActivity.DEBUG) MyLog.i(logTag, "x: " + point.x + ", threshold: " + horizontalThreshold);
		
		if ((int) point.y > verticalThreshold) point.y = verticalThreshold;
		if(BaseActivity.DEBUG) MyLog.i(logTag, "y: " + point.y + ", threshold: " + verticalThreshold);
		
		return point;
		
	}
	
	public PointF getTouchPoint(float x, float y, RectF rect) {
		return new PointF(x * rect.width() + rect.left, y * rect.height() + rect.top);
	}
	
	public int getHorizontalThreshold(){
		return horizontalThreshold;
	}
	
	public int getVerticalThreshold(){
		return verticalThreshold;
	}
	
	public int getTranslationWidth(){
		return translationWidth;
	}
	
	public int getTranslationHeight(){
		return translationHeight;
	}
	
}
