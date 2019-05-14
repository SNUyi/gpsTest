package com.xtc.test.gpstest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xtc.log.LogUtil;

import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;

public class GpsTestActivity extends Activity implements View.OnClickListener {

    private static final String TAG = GpsTestActivity.class.getSimpleName();

    private TextView mTvGpsData;

    private LocationManager mLocationManager;
    //    @SuppressLint("HandlerLeak")
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            updateShowGps(mLocation);
//        }
//    };
    private Button mBtnStart;
    private Button mBtnEnd;
    private RadioGroup mRadioGroup;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//                LogUtil.d(TAG, "onLocationChanged:" + location.toString());

            updateShowGps(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//                LogUtil.d(TAG, "onStatusChanged：" + provider + " status：" + status);
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    //Toast.makeText(getApplicationContext(),"当前GPS状态为可见状态", Toast.LENGTH_LONG).show();
//                        LogUtil.d(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    //Toast.makeText(getApplicationContext(),"当前GPS状态为服务区外状态", Toast.LENGTH_LONG).show();
//                        LogUtil.d(TAG, "当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //Toast.makeText(getApplicationContext(),"当前GPS状态为暂停服务状态", Toast.LENGTH_LONG).show();
//                        LogUtil.d(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            LogUtil.d(TAG, "onProviderEnabled：" + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            LogUtil.d(TAG, "onProviderDisabled：" + provider);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LogUtil.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
        Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
//        startGps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        mHandler.removeCallbacksAndMessages(null);
        setGpsModeEnable(this, false);
//        closeGps(getApplicationContext(), 1);
    }

    private void startGps() {

        if (!isGpsAble(mLocationManager)) {
            setGpsModeEnable(this, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
                    Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        } else {
            LogUtil.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
            Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
        }
    }

    private void record(long intervalTime) {
        if (!isGpsAble(mLocationManager)) {
            Toast.makeText(this, "先打开GPS", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "间隔时间：" + intervalTime/1000 + "秒", Toast.LENGTH_SHORT).show();
        LogUtil.d(TAG, "记录的间隔时间：" + intervalTime);
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalTime, 0, mLocationListener);
    }

    private void initView() {
        mTvGpsData = (TextView) findViewById(R.id.tv_gps_data);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
        mBtnEnd = (Button) findViewById(R.id.btn_end);
        mBtnEnd.setOnClickListener(this);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnClickListener(this);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_1s:
                        record(1000);
                        break;
                    case R.id.btn_2s:
                        record(2000);
                        break;
                    case R.id.btn_5s:
                        record(5000);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void updateShowGps(Location location) {
        if (location != null) {
            StringBuilder sb = new StringBuilder();

            sb.append("经度：" + location.getLongitude() + "\n");
            sb.append("纬度：" + location.getLatitude() + "\n");
            sb.append("高度：" + location.getAltitude() + "\n");
            sb.append("速度：" + location.getSpeed() + "\n");
            sb.append("方向：" + location.getBearing() + "\n");
            sb.append("定位精度：" + location.getAccuracy() + "\n");


            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("经度：" + location.getLongitude() + " ");
            stringBuilder.append("纬度：" + location.getLatitude() + " ");
            stringBuilder.append("高度：" + location.getAltitude() + " ");
            stringBuilder.append("速度：" + location.getSpeed() + " ");
            stringBuilder.append("方向：" + location.getBearing() + " ");
            stringBuilder.append("定位精度：" + location.getAccuracy());

            LogUtil.d(TAG, "当前位置：" + stringBuilder.toString());
            mTvGpsData.setText(sb.toString());
        } else {
            LogUtil.d(TAG, "当前位置：null");
            mTvGpsData.setText("");
        }
    }

    private boolean isGpsAble(LocationManager lm) {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ? true : false;
    }

    private void setGpsModeEnable(Context context, boolean enable) {
        ContentResolver contentResolver = context.getContentResolver();
        int currentMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE,
                LOCATION_MODE_OFF);
        int mode;
        if (enable) {
            mode = LOCATION_MODE_HIGH_ACCURACY;
        } else {
            mode = LOCATION_MODE_OFF;
        }
        LogUtil.d(TAG, "setGpsModeEnable() called with enable=" + enable + ", currentMode=" + currentMode + ", newMode=" + mode);
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        intent.putExtra("CURRENT_MODE", currentMode);
        intent.putExtra("NEW_MODE", mode);
        context.sendBroadcast(intent, Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(contentResolver, Settings.Secure.LOCATION_MODE, mode);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_start:
                startGps();
                break;
            case R.id.btn_end:
                if (mLocationManager != null) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                setGpsModeEnable(GpsTestActivity.this, false);
                Toast.makeText(GpsTestActivity.this, "关闭GPS", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
