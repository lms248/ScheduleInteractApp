package com.rcgl.util;

/**
 * 管理向服务端http请求的地址
 * @author Lims
 * @date 2015-03-02
 */
public class ServerUrlUtil {
	
	/** 服务器最底层的url */
	public static String SERVER_BASCE_URL = "http://58.67.151.217:20004";//172.16.10.122:8080
	
	/** 登录地址 */
	public static String Server_Login_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/LoginServlet";
	
	/** 注册地址 */
	public static String Server_Register_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/RegisterServlet";
	
	/** 个人资料管理地址 */
	public static String Server_UserInfoManage_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/UserInfoManage";
	
	/** 修改密码地址 */
	public static String Server_ChangePW_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/ChangePWServlet";
	
	/** 修改头像 */
	public static String Server_UpdatePhoto_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/UpdatePhotoServlet";
	
	/** 获取用户地址 */
	public static String Server_GetUser_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetUserListToClient";
	
	/** 发送消息地址 */
	public static String Server_SendMessage_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/MessageServlet";
	
	/** 获取日程列表地址 */
	public static String Server_GetScheduleList_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetScheduleListToClient";
	
	/** 获取好友日程列表地址 */
	public static String Server_GetFriendScheduleList_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetFriendScheduleListToClient";
	
	/** 获取好友列表地址 */
	public static String Server_GetFriendList_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetFriendListToClient";
	
	/** 获取消息列表地址 */
	public static String Server_GetMessageList_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetMessageListToClient";
	
	/** 添加好友地址 */
	public static String Server_AddFriend_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/AddFriend";
	
	/** 添加日程地址 */
	public static String Server_AddSchedule_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/AddSchedule";
	
	/** 修改日程地址 */
	public static String Server_UpdateSchedule_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/UpdateSchedule";
	
	/** 删除日程地址 */
	public static String Server_DeleteSchedule_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/DeleteSchedule";
	
	/** 日程管理地址 */
	public static String Server_ScheduleManage_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/ScheduleManage";
	
	/** 添加活动地址 */
	public static String Server_AddActivities_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/AddActivities";
	
	/** 获取活动列表地址 */
	public static String Server_GetActivitiesList_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetActivitiesListToClient";
	
	/** 获取活动列表地址 */
	public static String Server_GetActivitiesData_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/GetActivitiesDataToClient";
	
	/** 活动管理地址 */
	public static String Server_ActivitiesManage_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/ActivitiesManage";
	
	/** 附近的人定位地址 */
	public static String Server_NearbyServlet_URL = "http://58.67.151.217:20004/rcgl/RCGLServer/NearbyServlet";
	
}
