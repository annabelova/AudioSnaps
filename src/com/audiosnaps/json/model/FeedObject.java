package com.audiosnaps.json.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedObject implements Parcelable {

	public String type;
	public String url;
	public String pic_hash;
	public String pic_date;
	public boolean is_public;
	public String timestamp;
	public Caption caption;
	public CommentData comment_data;
	public LikeData like_data;
	public UserData user_data;
	public UserData owner_data;
	public int feedMode;
	public boolean first_picture;
	public boolean no_photos;
	
	// Constructor
    public FeedObject(String type, String url, String pic_hash, String pic_date, boolean is_public, String timestamp, Caption caption, CommentData comment_data, LikeData like_data, UserData user_data, UserData owner_data, int feedMode, boolean first_picture, boolean no_photos){
        this.type = type;
        this.url = url;
        this.pic_hash = pic_hash;
        this.pic_date = pic_date;
        this.is_public = is_public;
        this.timestamp = timestamp;
        this.caption = caption;
        this.comment_data = comment_data;
        this.like_data = like_data;
        this.user_data = user_data;
        this.owner_data = owner_data;
        this.feedMode = feedMode;
        this.first_picture = first_picture;
        this.no_photos = no_photos;
   }
	
    // Parcelling part
    private FeedObject(Parcel in){
    	this.type = in.readString();
        this.url = in.readString();
        this.pic_hash = in.readString();
        this.pic_date = in.readString();
        this.is_public = in.readByte() != 0;
        this.timestamp = in.readString();
        this.caption = in.readParcelable(Caption.class.getClassLoader());
        this.comment_data = in.readParcelable(CommentData.class.getClassLoader());
        this.like_data = in.readParcelable(LikeData.class.getClassLoader());
        this.user_data = in.readParcelable(UserData.class.getClassLoader());
        this.owner_data = in.readParcelable(UserData.class.getClassLoader());
        this.feedMode = in.readInt();
        this.first_picture = in.readByte() != 0;
        this.no_photos = in.readByte() != 0;
    }
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(type);
		parcel.writeString(url);
		parcel.writeString(pic_hash);
		parcel.writeString(pic_date);
		parcel.writeByte((byte) (is_public ? 1 : 0));
		parcel.writeString(timestamp);
		parcel.writeParcelable(caption, flags);
		parcel.writeParcelable(comment_data, flags);
		parcel.writeParcelable(like_data, flags);
		parcel.writeParcelable(user_data, flags);
		parcel.writeParcelable(owner_data, flags);
		parcel.writeInt(feedMode);
		parcel.writeByte((byte) (first_picture ? 1 : 0));
		parcel.writeByte((byte) (no_photos ? 1 : 0));
		
	}
	
	 public static final Parcelable.Creator<FeedObject> CREATOR = new Parcelable.Creator<FeedObject>() {
         
		 public FeedObject createFromParcel(Parcel in) {
             return new FeedObject(in); 
         }

         public FeedObject[] newArray(int size) {
             return new FeedObject[size];
         }
         
     };
}
