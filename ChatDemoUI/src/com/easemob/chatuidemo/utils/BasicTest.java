package com.easemob.chatuidemo.utils;

import java.util.Arrays;
import java.util.List;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatConfig;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class BasicTest {
	public static final String TAG = "BasicTest";
	private static BasicTest instance;
	
	/**
	 * 获取单例类的实例
	 *
	 * @return
	 */
	public static BasicTest getInstance() {
		if (instance == null) {
			instance = new BasicTest();
		}
		return instance;
	}
	
    /**
     * 检查清单文件配置方法
     *
     * @param context
     */
    public void init(Context context) {
        checkPermission(context);
        checkMetaData(context);
        checkService(context);
    }
    
    /*
     * 登陆后检查配置方法
     */
    public void checkLoginLater(){
    	checkInited();
    	checkLogin();
    	checkoutSetappinited();
    }

    /**
     * 获取PackageInfo对象
     *
     * @param context
     * @return
     */
    private PackageInfo getPackageInfo(Context context, int p) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), p);
            return pi;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查权限配置情况
     *
     * @param context
     */
    public void checkPermission(Context context) {
        /**
         * 这里定义Demo默认请求的一些权限，用来检查开发时是否有漏掉配置
         * 这些权限也不是圈闭必须，如果不需要某些功能是可以去掉的，根据自己需求以及理解配置
         */
        String[] pArray = {
                "android.permission.VIBRATE",
                "android.permission.INTERNET",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.CHANGE_WIFI_STATE",
                "android.permission.WAKE_LOCK",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.GET_ACCOUNTS",
                "android.permission.USE_CREDENTIALS",
                "android.permission.MANAGE_ACCOUNTS",
                "android.permission.AUTHENTICATE_ACCOUNTS",
                "com.android.launcher.permission.READ_SETTINGS",
                "android.permission.BROADCAST_STICKY",
                "android.permission.WRITE_SETTINGS",
                "android.permission.READ_PROFILE",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_EXTERNAL_STORAGE"
        };
        logI("检查 Permission 配置情况");
        PackageInfo pi = getPackageInfo(context, PackageManager.GET_PERMISSIONS);
        // 获取权限列表
        String[] permissions = pi.requestedPermissions;
        List<String> pLists = Arrays.asList(permissions);
        logI("Demo 中请求了 %d 个权限，当前项目请求了 %d 个权限", pArray.length, permissions.length);
        // 输出 Demo 配置的权限，并判断当前项目是否有配置
        for (int i = 0; i < pArray.length; i++) {
            if (pLists.contains(pArray[i])) {
                logI("YES    %s", pArray[i]);
            } else {
                logI("NO     %s", pArray[i]);
            }
        }
    }

    /**
     * 对application的配置检查
     */
    public void checkApplication(Context context) {
        logI("检查 Application 配置情况");
        PackageInfo pi = getPackageInfo(context, PackageManager.GET_UNINSTALLED_PACKAGES);
        ApplicationInfo ai = pi.applicationInfo;
        String className = ai.className;
        if (className != null) {
            logI("Application 已配置，可以在 Application 的 onCreate 方法里进行 SDK 的初始化");
        } else {
            logE("没有配置 Application，如果有在 onCreate 做 SDK 的初始化操作会无效，请检查！");
        }
    }


    /**
     * 检查是否配置了appkey
     *
     * @param context
     */
    @SuppressLint("NewApi")
	public void checkMetaData(Context context) {
    	logI("检查appkey配置情况");
        boolean isAppkey = false;
        String appkey = "";
        PackageInfo pi = getPackageInfo(context, PackageManager.GET_META_DATA);
        ApplicationInfo ai = pi.applicationInfo;
        try {
            Bundle bundle = ai.metaData;
            appkey = bundle.getString("EASEMOB_APPKEY", null);
            if (appkey != null && !appkey.equals("")) {
                isAppkey = true;
            }
        } catch (NullPointerException e) {
            logE("没有查询到 MetaData 配置，继续检测是否在代码中设置了 Appkey");
        }
        if (EMChat.getInstance().getAppkey()!= null && !EMChat.getInstance().getAppkey().equals("")) {
            isAppkey = true;
            appkey = EMChatConfig.getInstance().APPKEY;
        }
        if (isAppkey) {
        	logI("appkey 已配置 - %s"+appkey);
        } else {
            logE("appkey 没有配置，请配置环信 SDK 初始化所需的 appkey");
        }
    }

    /**
     * 检查 Service 配置情况 主要是检查是否有配置 EMChatService
     *
     * @param context
     */
    public void checkService(Context context) {
        boolean isService = false;
        PackageInfo pi = getPackageInfo(context, PackageManager.GET_SERVICES);
        try {
            ServiceInfo[] services = pi.services;
            logI("配置了 %d 个服务", services.length);
            for (int i = 0; i < services.length; i++) {
                ServiceInfo si = services[i];
                logI("ServiceInfo %s", si.toString());
                if (si.name.equals("com.easemob.chat.EMChatService")) {
                    isService = true;  
                }
            }
            if (isService) {
                logI("EMChatService 已配置");
            } else {
                logE("EMChatService 没有配置，请在配置文件配置环信的 EMChatService");
            }
        } catch (NullPointerException e) {
            logE("没有查询到配置 Service 信息，请配置环信的 EMChatSerice");
        }
    }

  //检查环信sdk是否init
  	public void checkInited(){
  		boolean isSDKInited = EMChat.getInstance().isSDKInited();
  		if(!isSDKInited){
  			logE("sdk未inited");
  		}
  	}
  	
  	//检查是否登陆环信
  	public void checkLogin(){
  		boolean isLogined = EMChat.getInstance().isLoggedIn();
  		if(!isLogined){
  			logE("未登录环信服务器");
  		}
  	}
  	//检查是否调用setappinited()
  	public void checkoutSetappinited(){
  		boolean appiented = EMChat.getInstance().appInited;
  		if(!appiented){
  			logE("未调用EMChat.getInstance().setAppInited();可能会收不到消息透传");
  		}
  	}
  	//消息监听
  	public void checkoutListener(){
  		
  	}

    private void logE(String msg) {
        Log.e(TAG, msg);
    }
    private void logI(String msg, Object... args) {
        Log.i(TAG, "|   " + String.format(msg, args));
    }
    private void logI(String msg){
    	Log.i(TAG, msg);
    }
	


}
