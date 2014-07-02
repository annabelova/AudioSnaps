package com.audiosnaps.classes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontLoader {
	
	private static final String FONT = "Roboto";
	
	// Style
	public static final int NORMAL = 0;
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int BOLD_ITALIC = 3;
	public static final int BLACK = 4;
	public static final int BLACK_ITALIC = 5;
	public static final int BOLD_CONDENSED = 6;
	public static final int BOLD_CONDENSED_ITALIC = 7;
	public static final int LIGHT = 8;
	public static final int LIGHT_ITALIC = 9;
	public static final int MEDIUM = 10;
	public static final int MEDIUM_ITALIC = 11;
	public static final int THIN = 12;
	public static final int THIN_ITALIC = 13;
	public static final int REGULAR = 14;

    public static void setRobotoFont(TextView textView, Context context, int style) {
        
    	if (!textView.isInEditMode()) {
        	
        	String typeface = "fonts/" + FONT + "-";
        	
        	if(style == FontLoader.NORMAL){
        		typeface += "Normal";
        	}else if(style == FontLoader.BOLD){
        		typeface += "Bold";
        	}else if(style == FontLoader.ITALIC){
        		typeface += "Italic";
        	}else if(style == FontLoader.BOLD_ITALIC){
        		typeface += "BoldItalic.ttf";
        	}else if(style == FontLoader.BLACK){
        		typeface += "Black";
        	}else if(style == FontLoader.BLACK_ITALIC){
        		typeface += "BlackItalic";
        	}else if(style == FontLoader.BOLD_CONDENSED){
        		typeface += "BoldCondensed";
        	}else if(style == FontLoader.BOLD_CONDENSED_ITALIC){
        		typeface += "BoldCondensedItalic";
        	}else if(style == FontLoader.LIGHT){
        		typeface += "Light";
        	}else if(style == FontLoader.LIGHT_ITALIC){
        		typeface += "LightItalic";
        	}else if(style == FontLoader.MEDIUM){
        		typeface += "Medium";
        	}else if(style == FontLoader.MEDIUM_ITALIC){
        		typeface += "MediumItalic";
        	}else if(style == FontLoader.THIN){
        		typeface += "Thin";
        	}else if(style == FontLoader.THIN_ITALIC){
        		typeface += "ThinItalic";
        	}else {
        		typeface += "Regular";
        	}
        	
        	typeface += ".ttf";
        	
        	textView.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
            
        }
    }
	
    public static void setRobotoFont(View view, Context context, int style){
    	
    	if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++)
            {
            	if(view instanceof TextView){
            		setRobotoFont(((TextView)((ViewGroup)view).getChildAt(i)), context, style);
            	}
            }
        }
        else if (view instanceof TextView)
        {
        	setRobotoFont((TextView)view, context, style);
        }
    } 
}
