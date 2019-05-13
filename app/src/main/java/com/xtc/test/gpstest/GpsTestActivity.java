package com.xtc.test.gpstest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xtc.log.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;

public class GpsTestActivity extends Activity implements View.OnClickListener {

    private static final String TAG = GpsTestActivity.class.getSimpleName();

    private TextView mTvGpsData;
    private TextView mTvWifiData;

    private LocationManager mLocationManager;
    private Location mLocation;
    private int mTemp = 0;
    private ArrayList<Integer> mIntegers = new ArrayList<>();
    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;

    private StringBuilder mStringBuilder;
    private SimpleDateFormat mDateFormat;
    private Button mBtnClick;

    private Timer mRecordTimer;
    private Timer mWifiTimer;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateShowGps(mLocation);
        }
    };
    private Button mBtnOut;
    private Button mBtnIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);// 设置日期格式
        mWifiScanReceiver = new WifiScanReceiver();
        initView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startGps();

        mRecordTimer = new Timer();
        mRecordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String nowTime = mDateFormat.format(new Date());// 获取当前的日期

                if (mStringBuilder == null) {
                    mStringBuilder = new StringBuilder();
                }

                LogUtil.d(TAG, "wifi信息：" + mStringBuilder.toString());

                mIntegers.add(mTemp);

                if (mIntegers.size() > 1) {
                    if (mIntegers.get(mIntegers.size() - 1).equals(mIntegers.get(mIntegers.size() - 2))) {
                        mLocation = null;
                    }
                }
                mHandler.sendMessage(new Message());

                //埋点数据
                LocationBehavior.gpsData(GpsTestActivity.this, nowTime, mLocation, mStringBuilder.toString(), LocationBehavior.TYPE_NORMAL);
            }
        }, 0, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        mLocationManager.addNmeaListener(mNmeaListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRecordTimer != null) {
            mRecordTimer.cancel();
        }
        if (mWifiTimer != null) {
            mWifiTimer.cancel();
        }

        mHandler.removeCallbacksAndMessages(null);
        closeGps(getApplicationContext(), 1);
        unregisterReceiver(mWifiScanReceiver);
//        mLocationManager.removeNmeaListener(mNmeaListener);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startGps() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mWifiTimer = new Timer();
        mWifiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mWifiManager != null) {
                    mWifiManager.startScan();
                }
            }
        }, 0, 5000);

        Log.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
        Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
        if (!isGpsAble(mLocationManager)) {
//            openGps(getApplicationContext(), Long.MAX_VALUE / 2, 1);
            setGpsModeEnable(this, true);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LogUtil.d(TAG, "onLocationChanged:" + location.toString());
                ++mTemp;

                mLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LogUtil.d(TAG, "onStatusChanged：" + provider + " status：" + status);
                switch (status) {
                    //GPS状态为可见时
                    case LocationProvider.AVAILABLE:
                        //Toast.makeText(getApplicationContext(),"当前GPS状态为可见状态", Toast.LENGTH_LONG).show();
                        LogUtil.d(TAG, "当前GPS状态为可见状态");
                        break;
                    //GPS状态为服务区外时
                    case LocationProvider.OUT_OF_SERVICE:
                        //Toast.makeText(getApplicationContext(),"当前GPS状态为服务区外状态", Toast.LENGTH_LONG).show();
                        LogUtil.d(TAG, "当前GPS状态为服务区外状态");
                        break;
                    //GPS状态为暂停服务时
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        //Toast.makeText(getApplicationContext(),"当前GPS状态为暂停服务状态", Toast.LENGTH_LONG).show();
                        LogUtil.d(TAG, "当前GPS状态为暂停服务状态");
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
        });
    }

    private void initView() {
        mTvGpsData = (TextView) findViewById(R.id.tv_gps_data);
        mTvWifiData = (TextView) findViewById(R.id.tv_wifi_data);
        mBtnClick = (Button) findViewById(R.id.btn_click);
        mBtnClick.setOnClickListener(this);
        mBtnOut = (Button) findViewById(R.id.btn_out);
        mBtnOut.setOnClickListener(this);
        mBtnIn = (Button) findViewById(R.id.btn_in);
        mBtnIn.setOnClickListener(this);
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
        context.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(contentResolver, Settings.Secure.LOCATION_MODE, mode);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
                Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }

    public void openGps(Context context, long timeMillis, int requestCode) {
        Intent intent = new Intent(GpsConfigContract.Action_Gps);
        intent.putExtra(GpsConfigContract.KeyString_packageName, context.getPackageName());
        intent.putExtra(GpsConfigContract.KeyInt_requestCode, requestCode);
        intent.putExtra(GpsConfigContract.KeyBool_isOpen, true);
        intent.putExtra(GpsConfigContract.KeyLong_remainTimeMillis, timeMillis);
        context.sendBroadcast(intent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "GPS状态：" + isGpsAble(mLocationManager));
                Toast.makeText(GpsTestActivity.this, "GPS状态:" + isGpsAble(mLocationManager), Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }

    public static void closeGps(Context context, int requestCode) {
        Intent intent = new Intent(GpsConfigContract.Action_Gps);
        intent.putExtra(GpsConfigContract.KeyBool_isOpen, false);
        intent.putExtra(GpsConfigContract.KeyString_packageName, context.getPackageName());
        intent.putExtra(GpsConfigContract.KeyInt_requestCode, requestCode);
        context.sendBroadcast(intent);
    }

    private GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long l, String s) {
            LogUtil.d(TAG, s);//报文的回调函数在这里，时间打印在打印类中

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_click:
                this.finish();
                break;
            case R.id.btn_out:
                String nowTime = mDateFormat.format(new Date());
                LocationBehavior.gpsData(GpsTestActivity.this, nowTime, mLocation, mStringBuilder.toString(), LocationBehavior.TYPE_OUT);
                Toast.makeText(this,"出门", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_in:
                String nowTime1 = mDateFormat.format(new Date());
                LocationBehavior.gpsData(GpsTestActivity.this, nowTime1, mLocation, mStringBuilder.toString(), LocationBehavior.TYPE_IN);
                Toast.makeText(this,"进门", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mTvWifiData.setText("");
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            if (scanResults == null) {
                return;
            }
            mStringBuilder = new StringBuilder();

            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult result = scanResults.get(i);
                if (result == null) {
                    continue;
                }

                if (TextUtils.isEmpty(result.SSID)) {
                    continue;
                }

                if (mStringBuilder.length() > 0) {
                    mStringBuilder.append("|");
                }
                mStringBuilder.append(result.SSID).append(",").append(result.BSSID).append(",");
                mStringBuilder.append(result.level);
            }

            mTvWifiData.setText(mStringBuilder.toString());
        }
    }
}
