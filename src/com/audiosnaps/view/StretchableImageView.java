package com.audiosnaps.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class StretchableImageView extends ImageView {

	public StretchableImageView(Context context) {
		super(context);
	}
	
	public StretchableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public StretchableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	
		if(this.getBackground() != null){
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = width * this.getBackground().getIntrinsicHeight() / this.getBackground().getIntrinsicWidth();
	    	setMeasuredDimension(width, height);
		}
		
	}

}
