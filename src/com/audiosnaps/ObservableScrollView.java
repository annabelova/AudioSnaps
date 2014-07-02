package com.audiosnaps;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {

	private ObservableScrollViewListener changeListener=null;
	private static String TAG="ObservableScrollView";
	
	public ObservableScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ObservableScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	
	
	
	public ObservableScrollViewListener getChangeListener() {
		return changeListener;
	}

	public void setChangeListener(ObservableScrollViewListener changeListener) {
		this.changeListener = changeListener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		if(changeListener!=null)
			changeListener.onScrollChanged(this, l, t, oldl, oldt);
		
		super.onScrollChanged(l, t, oldl, oldt);
	}
	

	
}
