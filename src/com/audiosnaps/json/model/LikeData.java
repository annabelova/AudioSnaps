package com.audiosnaps.json.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LikeData implements Parcelable {

	public int total_likes;
	public int facebook_likes;
	public int twitter_likes;
	public int local_likes;
	public boolean has_liked;
	
	public LikeData(int total_likes, int facebook_likes, int twitter_likes, int local_likes, boolean has_liked){
		this.total_likes = total_likes;
		this.facebook_likes = facebook_likes;
		this.twitter_likes = twitter_likes;
		this.local_likes = local_likes;
		this.has_liked = has_liked;
	}
	
	private LikeData(Parcel in){
		this.total_likes = in.readInt();
		this.facebook_likes = in.readInt();
		this.twitter_likes = in.readInt();
		this.local_likes = in.readInt();
		this.has_liked = in.readByte() != 0;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(total_likes);
		dest.writeInt(facebook_likes);
		dest.writeInt(twitter_likes);
		dest.writeInt(local_likes);
		dest.writeByte((byte) (has_liked ? 1:0));
	}
	
	public static final Parcelable.Creator<LikeData> CREATOR = new Parcelable.Creator<LikeData>() {
        
		 public LikeData createFromParcel(Parcel in) {
            return new LikeData(in); 
        }

        public LikeData[] newArray(int size) {
            return new LikeData[size];
        }
        
    };
	
}
