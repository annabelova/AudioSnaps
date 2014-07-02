package com.audiosnaps.json.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Caption implements Parcelable {

	public String str;
	public String rich_text;
	
	public Caption(String str, String rich_text){
		this.str = str;
		this.rich_text = rich_text;
	}
	
	// Parcelling part
    private Caption(Parcel in){
    	this.str = in.readString();
    	this.rich_text = in.readString();
    }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(str);
		dest.writeString(rich_text);
	}
	
	public static final Parcelable.Creator<Caption> CREATOR = new Parcelable.Creator<Caption>() {
        
		 public Caption createFromParcel(Parcel in) {
           return new Caption(in); 
       }

       public Caption[] newArray(int size) {
           return new Caption[size];
       }
       
   };
	
}
