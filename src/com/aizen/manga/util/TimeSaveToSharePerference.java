package com.aizen.manga.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 时间刷新的保存
 * @author user
 *
 */
public class TimeSaveToSharePerference {

	//偏好设置的名字
	private static final String REFRESH_TIME = "refresh_time";
	
	/**
	 * 判断是否要超过指定时间，超过则刷新
	 * @param min
	 * @param refreshName
	 * @param context
	 * @return
	 */
	public static boolean isRefresh(int min,String refreshName,Context context){
		
		if(System.currentTimeMillis() - getRefreshTime(refreshName, context) > min*60*1000){
			
			return true;
		}
		return false;
	}
	
	/**
	 * 存储当前时间
	 * @param time
	 * @param refreshName
	 * @param context
	 */
	public static void setRefreshTime(String refreshName,Context context){
		
		SharedPreferences sp = context.getSharedPreferences(REFRESH_TIME, Context.MODE_PRIVATE);
		sp.edit().putLong(refreshName, System.currentTimeMillis()).commit();
	}
	
	/**
	 * 获得上次更新的时间
	 * @param refreshName
	 * @param context
	 * @return
	 */
	private static long getRefreshTime(String refreshName,Context context){
		SharedPreferences sp = context.getSharedPreferences(REFRESH_TIME, Context.MODE_PRIVATE);
		return sp.getLong(refreshName, 0);
	}
}
