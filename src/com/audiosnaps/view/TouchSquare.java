package com.audiosnaps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

public class TouchSquare extends View {

	private static final float SQUARE_WIDTH = 40;
	private static final float SQUARE_HEIGHT = 40;
	private static final float PAINT_WIDTH = 8;
	
	private Paint paint;
	private PointF center;
	
	public TouchSquare(Context context, PointF point){
		super(context);
		
		// init paint
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    paint.setColor(Color.RED);
	    paint.setStrokeWidth(PAINT_WIDTH);
	    
	    center = point;
	}
	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		canvas.drawLine(center.x - SQUARE_WIDTH/2, center.y + SQUARE_HEIGHT/2, center.x + SQUARE_WIDTH/2, center.y + SQUARE_HEIGHT/2, paint);
		    canvas.drawLine(center.x - SQUARE_WIDTH/2, center.y - SQUARE_HEIGHT/2, center.x - SQUARE_WIDTH/2, center.y + SQUARE_HEIGHT/2, paint);
		    canvas.drawLine(center.x + SQUARE_WIDTH/2, center.y - SQUARE_HEIGHT/2, center.x + SQUARE_WIDTH/2, center.y + SQUARE_HEIGHT/2, paint);
		    canvas.drawLine(center.x - SQUARE_WIDTH/2, center.y - SQUARE_HEIGHT/2, center.x + SQUARE_WIDTH/2, center.y - SQUARE_HEIGHT/2, paint);
	}
	
}
