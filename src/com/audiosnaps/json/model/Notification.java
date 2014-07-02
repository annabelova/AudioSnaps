package com.audiosnaps.json.model;

public class Notification {

	public int type;
	public String notification_id;
	public String[] notification_id_array;
	public int quantity;
	public String pic_hash;
	public String thumbnail;
	public String pic_owner;
	public String text;
	public String date;
	public String timestamp;
	public boolean was_sent;
	public boolean was_seen;
	public Maker maker;
	public Receiver receiver;
	public Subject subject;
	public boolean open_url;
	
}
