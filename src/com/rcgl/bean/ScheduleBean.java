package com.rcgl.bean;

import android.graphics.drawable.Drawable;

/**
 * 日程值对象（存放了日程的所有信息）
 * @author lims
 * @date 2015-03-23
 */
public class ScheduleBean {
	/** 数据的标号 */
    private int scheduleid;
    /** 用户id(发表日程的用户) */
	private int userid;
	/** 日程实行时间 */
	private String dotime;
	/** 日程内容 */
	private String content;
	/** 日程开放状态，0为私密，1为开放 */
	private int open;
	/** 是否日程提醒，0为不提醒，1为提醒 */
	private int alarm;
	/** 日程完成状态,0为未完成，1为完成 */
	private int status;
	/** 日程日期图片 */
	private int image;
	/** 日程发表时间 */
	private String time;
	
	public int getScheduleid() {
		return scheduleid;
	}
	public void setScheduleid(int scheduleid) {
		this.scheduleid = scheduleid;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getDotime() {
		return dotime;
	}
	public void setDotime(String dotime) {
		this.dotime = dotime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getOpen() {
		return open;
	}
	public void setOpen(int open) {
		this.open = open;
	}
	public int getAlarm() {
		return alarm;
	}
	public void setAlarm(int alarm) {
		this.alarm = alarm;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getImage() {
		return image;
	}
	public void setImage(int image) {
		this.image = image;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
