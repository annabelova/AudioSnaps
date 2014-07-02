package com.audiosnaps.json.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentData implements Parcelable {

	public int total_comments;

	public CommentData(int total_comments){
		this.total_comments = total_comments;
	}
	
	// Parcelling part
    private CommentData(Parcel in){
    	this.total_comments = in.readInt();
    }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(total_comments);
	}
	
	public static final Parcelable.Creator<CommentData> CREATOR = new Parcelable.Creator<CommentData>() {
        
		 public CommentData createFromParcel(Parcel in) {
            return new CommentData(in); 
        }

        public CommentData[] newArray(int size) {
            return new CommentData[size];
        }
        
    };
	
}
