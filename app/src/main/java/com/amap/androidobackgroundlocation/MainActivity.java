package com.amap.androidobackgroundlocation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 高精度定位模式功能演示
 *
 * @创建时间： 2015年11月24日 下午5:22:42
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: Hight_Accuracy_Activity.java
 * @类型名称: Hight_Accuracy_Activity
 */
public class MainActivity extends CheckPermissionsActivity
		implements
        OnCheckedChangeListener,
        OnClickListener {
	private RadioGroup rgLocationMode;
	private EditText etInterval;
	private EditText etHttpTimeout;
	private CheckBox cbOnceLocation;
	private CheckBox cbAddress;
	private CheckBox cbGpsFirst;
	private CheckBox cbCacheAble;
	private CheckBox cbOnceLastest;
	private CheckBox cbSensorAble;
	private TextView tvResult;
	private Button btLocation;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	Intent serviceIntent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.title_location);


		serviceIntent = new Intent();
		serviceIntent.setClass(this,LocationForegoundService.class);

		initView();
		
		//初始化定位
		initLocation();
	}
	
	//初始化控件
	private void initView(){
		rgLocationMode = (RadioGroup) findViewById(R.id.rg_locationMode);
		
		etInterval = (EditText) findViewById(R.id.et_interval);
		etHttpTimeout = (EditText) findViewById(R.id.et_httpTimeout);
		
		cbOnceLocation = (CheckBox)findViewById(R.id.cb_onceLocation);
		cbGpsFirst = (CheckBox) findViewById(R.id.cb_gpsFirst);
		cbAddress = (CheckBox) findViewById(R.id.cb_needAddress);
		cbCacheAble = (CheckBox) findViewById(R.id.cb_cacheAble);
		cbOnceLastest = (CheckBox) findViewById(R.id.cb_onceLastest);
		cbSensorAble = (CheckBox)findViewById(R.id.cb_sensorAble);

		tvResult = (TextView) findViewById(R.id.tv_result);
		btLocation = (Button) findViewById(R.id.bt_location);
		
		rgLocationMode.setOnCheckedChangeListener(this);
		btLocation.setOnClickListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//如果已经开始定位了，显示通知栏
		if(isSartLocation) {
			if (null != serviceIntent) {
				startService(serviceIntent);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//如果要一直显示可以不执行
		if(null != serviceIntent){
			stopService(serviceIntent);
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyLocation();

		//如果要一直显示可以不执行
		if(null != serviceIntent){
			stopService(serviceIntent);
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (null == locationOption) {
			locationOption = new AMapLocationClientOption();
		}
		switch (checkedId) {
			case R.id.rb_batterySaving :
				locationOption.setLocationMode(AMapLocationMode.Battery_Saving);
				break;
			case R.id.rb_deviceSensors :
				locationOption.setLocationMode(AMapLocationMode.Device_Sensors);
				break;
			case R.id.rb_hightAccuracy :
				locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
				break;
			default :
				break;
		}

	}

	/**
	 * 设置控件的可用状态
	 */
	private void setViewEnable(boolean isEnable) {
		for(int i=0; i<rgLocationMode.getChildCount(); i++){
			rgLocationMode.getChildAt(i).setEnabled(isEnable);
		}
		etInterval.setEnabled(isEnable);
		etHttpTimeout.setEnabled(isEnable);
		cbOnceLocation.setEnabled(isEnable);
		cbGpsFirst.setEnabled(isEnable);
		cbAddress.setEnabled(isEnable);
		cbCacheAble.setEnabled(isEnable);
		cbOnceLastest.setEnabled(isEnable);
		cbSensorAble.setEnabled(isEnable);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bt_location) {
			if (btLocation.getText().equals(
					getResources().getString(R.string.startLocation))) {
				setViewEnable(false);
				btLocation.setText(getResources().getString(
						R.string.stopLocation));
				tvResult.setText("正在定位...");
				startLocation();
			} else {
				setViewEnable(true);
				btLocation.setText(getResources().getString(
						R.string.startLocation));
				stopLocation();
				tvResult.setText("定位停止");
			}
		}
	}
	
	/**
	 * 初始化定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation(){
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = getDefaultOption();
		//设置定位参数
		locationClient.setLocationOption(locationOption);
		// 设置定位监听
		locationClient.setLocationListener(locationListener);
	}
	
	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption(){
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
		return mOption;
	}
	
	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			if (null != location) {

				StringBuffer sb = new StringBuffer();
				//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if(location.getErrorCode() == 0){
					sb.append("定位成功" + "\n");
					sb.append("定位类型: " + location.getLocationType() + "\n");
					sb.append("经    度    : " + location.getLongitude() + "\n");
					sb.append("纬    度    : " + location.getLatitude() + "\n");
					sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
					sb.append("提供者    : " + location.getProvider() + "\n");

					sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
					sb.append("角    度    : " + location.getBearing() + "\n");
					// 获取当前提供定位服务的卫星个数
					sb.append("星    数    : " + location.getSatellites() + "\n");
					sb.append("国    家    : " + location.getCountry() + "\n");
					sb.append("省            : " + location.getProvince() + "\n");
					sb.append("市            : " + location.getCity() + "\n");
					sb.append("城市编码 : " + location.getCityCode() + "\n");
					sb.append("区            : " + location.getDistrict() + "\n");
					sb.append("区域 码   : " + location.getAdCode() + "\n");
					sb.append("地    址    : " + location.getAddress() + "\n");
					sb.append("兴趣点    : " + location.getPoiName() + "\n");
					//定位完成的时间
					sb.append("定位时间: " + formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
				} else {
					//定位失败
					sb.append("定位失败" + "\n");
					sb.append("错误码:" + location.getErrorCode() + "\n");
					sb.append("错误信息:" + location.getErrorInfo() + "\n");
					sb.append("错误描述:" + location.getLocationDetail() + "\n");
				}
				sb.append("***定位质量报告***").append("\n");
				sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
				sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
				sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
				sb.append("****************").append("\n");
				//定位之后的回调时间
				sb.append("回调时间: " + formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

				//解析定位结果，
				String result = sb.toString();
				tvResult.setText(result);
			} else {
				tvResult.setText("定位失败，loc is null");
			}
		}
	};


	/**
	 * 获取GPS状态的字符串
	 * @param statusCode GPS状态码
	 * @return
	 */
	private String getGPSStatusString(int statusCode){
		String str = "";
		switch (statusCode){
			case AMapLocationQualityReport.GPS_STATUS_OK:
				str = "GPS状态正常";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
				str = "手机中没有GPS Provider，无法进行GPS定位";
				break;
			case AMapLocationQualityReport.GPS_STATUS_OFF:
				str = "GPS关闭，建议开启GPS，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
				str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
				str = "没有GPS定位权限，建议开启gps定位权限";
				break;
		}
		return str;
	}
	// 根据控件的选择，重新设置定位参数
	private void resetOption() {
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(cbAddress.isChecked());
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(cbGpsFirst.isChecked());
		// 设置是否开启缓存
		locationOption.setLocationCacheEnable(cbCacheAble.isChecked());
		// 设置是否单次定位
		locationOption.setOnceLocation(cbOnceLocation.isChecked());
		//设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
		locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
		//设置是否使用传感器
		locationOption.setSensorEnable(cbSensorAble.isChecked());
		//设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		String strInterval = etInterval.getText().toString();
		if (!TextUtils.isEmpty(strInterval)) {
			try{
				// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
				locationOption.setInterval(Long.valueOf(strInterval));
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
		
		String strTimeout = etHttpTimeout.getText().toString();
		if(!TextUtils.isEmpty(strTimeout)){
			try{
				// 设置网络请求超时时间
			     locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}

	boolean isSartLocation = false;
	/**
	 * 开始定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void startLocation(){
		//根据控件的选择，重新设置定位参数
		resetOption();
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();
		isSartLocation = true;
	}
	
	/**
	 * 停止定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void stopLocation(){
		// 停止定位
		locationClient.stopLocation();
		isSartLocation = false;
	}
	
	/**
	 * 销毁定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void destroyLocation(){
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
		isSartLocation = false;
	}

	private static SimpleDateFormat sdf = null;
	public  static String formatUTC(long l, String strPattern) {
		if (TextUtils.isEmpty(strPattern)) {
			strPattern = "yyyy-MM-dd HH:mm:ss";
		}
		if (sdf == null) {
			try {
				sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
			} catch (Throwable e) {
			}
		} else {
			sdf.applyPattern(strPattern);
		}
		return sdf == null ? "NULL" : sdf.format(l);
	}

}
