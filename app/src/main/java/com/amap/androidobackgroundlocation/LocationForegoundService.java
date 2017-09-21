package com.amap.androidobackgroundlocation;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

/**
 * Created by hongming.wang on 2017/9/13.
 * 前台定位service
 */

public class LocationForegoundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Android O上才显示通知栏
        if(Build.VERSION.SDK_INT >= 26) {
            showNotify();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //显示通知栏
    public void showNotify(){
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("正在后台定位")
                .setContentText("定位进行中")
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        //调用这个方法把服务设置成前台服务
        startForeground(110, notification);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        LocationForegoundService getService() {
            return LocationForegoundService.this;
        }
    }
}
