package com.audiosnaps.gmc;

public class GMCNotification {

	private String notificationId, ids, objectId, picOwnerId, userReceivesId, openUrl, template;

	public GMCNotification(String notificationId, String ids, String objectId, String picOwnerId, String userReceivesId, String openUrl, String template) {
		super();
		this.notificationId = notificationId;
		this.ids = ids;
		this.objectId = objectId;
		this.picOwnerId = picOwnerId;
		this.userReceivesId = userReceivesId;
		this.openUrl = openUrl;
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getPicOwnerId() {
		return picOwnerId;
	}

	public void setPicOwnerId(String picOwnerId) {
		this.picOwnerId = picOwnerId;
	}

	public String getUserReceivesId() {
		return userReceivesId;
	}

	public void setUserReceivesId(String userReceivesId) {
		this.userReceivesId = userReceivesId;
	}

	public String getOpenUrl() {
		return openUrl;
	}

	public void setOpenUrl(String openUrl) {
		this.openUrl = openUrl;
	}

}
