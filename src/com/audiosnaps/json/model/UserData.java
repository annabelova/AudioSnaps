package com.audiosnaps.json.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable {

	public String user_id;
	public String user_name;
	public String picture_url;
	
	public UserData(String user_id, String user_name, String picture_url){
		this.user_id = user_id;
		this.user_name = user_name;
		this.picture_url = picture_url;
	}
	
	private UserData(Parcel in){
		this.user_id = in.readString();
		this.user_name = in.readString();
		this.picture_url = in.readString();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user_id);
		dest.writeString(user_name);
		dest.writeString(picture_url);
	}
	
	public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>() {
        
		 public UserData createFromParcel(Parcel in) {
           return new UserData(in); 
       }

       public UserData[] newArray(int size) {
           return new UserData[size];
       }
       
   };
}
