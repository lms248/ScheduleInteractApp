package com.rcgl.bean;

/**
 * 活动值对象（存放了活动的所有信息）
 * @author lims
 * @date 2015-03-23
 */
public class UserBean {
    /** 用户ID */
	private int userid;
	/** 用户名 */
	private String username;
	/** 密码*/
	private String password;
	/** 用户头像*/
	private String photo;
	/** 用户昵称*/
	private String nickname;
	/** 图片 */
	private int image;
	/** 描述 */
	private String describe;
	/** 好友 */
	private String friend;
	/** 是否显示选择框 */
	private Boolean CheckBoxVisibility;
	/** 注册时间 */
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
	public int getImage() {
		return image;
	}
	public void setImage(int image) {
		this.image = image;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getFriend() {
		return friend;
	}
	public void setFriend(String friend) {
		this.friend = friend;
	}
	public Boolean getCheckBoxVisibility() {
		return CheckBoxVisibility;
	}
	public void setCheckBoxVisibility(Boolean checkBoxVisibility) {
		CheckBoxVisibility = checkBoxVisibility;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
