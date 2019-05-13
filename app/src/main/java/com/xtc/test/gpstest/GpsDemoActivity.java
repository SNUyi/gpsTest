package com.xtc.test.gpstest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GpsDemoActivity extends AppCompatActivity {

    private static final String TAG = GpsDemoActivity.class.getSimpleName();
    private LocationManager lm;
    private TextView tv_show;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_demo);
        tv_show = (TextView) findViewById(R.id.tv_show);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "当前GPS状态：" + isGpsAble(lm));
        if (!isGpsAble(lm)) {
            Toast.makeText(GpsDemoActivity.this, "请打开GPS~", Toast.LENGTH_SHORT).show();
            openGPS2();
        }
        //从GPS获取最近的定位信息
        Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateShow(lc);
        //设置间隔1秒获得一次GPS定位信息
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新定位
                updateShow(location);
                Log.d(TAG, location.toString());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged：" + provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS LocationProvider可用时，更新定位
                updateShow(lm.getLastKnownLocation(provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                updateShow(null);
            }
        });
    }


    //定义一个更新显示的方法
    private void updateShow(Location location) {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("当前的位置信息：\n");
            sb.append("精度：" + location.getLongitude() + "\n");
            sb.append("纬度：" + location.getLatitude() + "\n");
            sb.append("高度：" + location.getAltitude() + "\n");
            sb.append("速度：" + location.getSpeed() + "\n");
            sb.append("方向：" + location.getBearing() + "\n");
            sb.append("定位精度：" + location.getAccuracy() + "\n");
            tv_show.setText(sb.toString());
        } else tv_show.setText("");
    }


    private boolean isGpsAble(LocationManager lm) {
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ? true : false;
    }


    //打开设置页面让用户自己设置
    private void openGPS2() {
        openGps(getApplicationContext(), Long.MAX_VALUE / 2, 1);

    }

    public static void openGps(Context context, long timeMillis, int requestCode) {
        Intent intent = new Intent(GpsConfigContract.Action_Gps);
        intent.putExtra(GpsConfigContract.KeyString_packageName, context.getPackageName());
        intent.putExtra(GpsConfigContract.KeyInt_requestCode, requestCode);
        intent.putExtra(GpsConfigContract.KeyBool_isOpen, true);
        intent.putExtra(GpsConfigContract.KeyLong_remainTimeMillis, timeMillis);
        context.sendBroadcast(intent);
    }
}
