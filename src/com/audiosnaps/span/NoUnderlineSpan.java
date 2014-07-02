package com.audiosnaps.span;

import android.text.TextPaint;
import android.text.style.UnderlineSpan;

public class NoUnderlineSpan extends UnderlineSpan {
	
	@Override
	public void updateDrawState(TextPaint textPaint) {
		// set to false to remove underline
		textPaint.setUnderlineText(false);
	}

}
