package com.amap.androidobackgroundlocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;

/**
 * @author hongming.whm
 * @date 2020-07-03 20:37
 */
public class Utils {
    public static final int NOTIFY_ID = 2001;
    private static final String NOTIFICATION_CHANNEL_NAME = "AMapBackgroundLocation";
    private static NotificationManager notificationManager = null;
    private static boolean isCreatedChannel = false;

    /**
     * 创建一个通知栏，API>=26时才有效
     * @param context
     * @return
     */
    public static Notification buildNotification(Context context) {
        try {
            Context mContext = context.getApplicationContext();
            Notification.Builder builder = null;
            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
                if (null == notificationManager) {
                    notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                }
                String channelId = mContext.getPackageName();
                if (!isCreatedChannel) {
                    NotificationChannel notificationChannel = new NotificationChannel(channelId,
                                                                                      NOTIFICATION_CHANNEL_NAME,
                                                                                      NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                    notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                    notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                    notificationManager.createNotificationChannel(notificationChannel);
                    isCreatedChannel = true;
                }
                builder = new Notification.Builder(mContext, channelId);
            } else {
                builder = new Notification.Builder(mContext);
            }

            builder.setSmallIcon(R.mipmap.ic_launcher)
                   .setContentTitle(getAppName(mContext))
                   .setContentText("正在后台运行")
                   .setWhen(System.currentTimeMillis());

            notification = builder.build();
            return notification;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取app的名称
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            appName =  context.getResources().getString(labelRes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return appName;
    }
}
