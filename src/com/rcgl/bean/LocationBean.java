package com.rcgl.bean;

/**
 * 附近的人定位
 * @author lims
 * @date 2015-05-05
 */
public class LocationBean {
    /** 用户ID */
	private int userid;
	/** 用户名 */
	private String username;
	/** 用户头像*/
	private String photo;
	/** 用户昵称*/
	private String nickname;
	/** 描述 */
	private String describe;
	/** 距离 */
	private String distance;
	/** 好友关系 */
	private int isfriend;
	/** 时间 */
	private String time;
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public int getIsfriend() {
		return isfriend;
	}
	public void setIsfriend(int isfriend) {
		this.isfriend = isfriend;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
