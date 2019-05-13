package com.xtc.test.gpstest;

import android.content.Context;
import android.location.Location;

import com.xtc.common.bigdata.BehaviorUtil;

import java.util.HashMap;

/**
 * Created by sunlipeng on 2019/4/11.
 */
public class LocationBehavior {

    private static final String X_TEST_GPS = "pre_gps_location_test";

    public static final int TYPE_IN = 1;
    public static final int TYPE_OUT = 2;
    public static final int TYPE_NORMAL = 3;


    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ALTITUDE = "altitude";
    private static final String SPEED = "speed";
    private static final String BEARING = "bearing";
    private static final String ACCURACY = "accuracy";

    private static final String TYPE = "type";

    private static final String WIFI = "wifi";
    private static final String TIME = "time";

    public static void gpsData(Context context, String time, Location location, String wifi, int type) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(TIME, time);

        if (location != null) {
            hashMap.put(LONGITUDE, String.valueOf(location.getLongitude()));
            hashMap.put(LATITUDE, String.valueOf(location.getLatitude()));
            hashMap.put(ALTITUDE, String.valueOf(location.getAltitude()));
            hashMap.put(SPEED, String.valueOf(location.getSpeed()));
            hashMap.put(BEARING, String.valueOf(location.getBearing()));
            hashMap.put(ACCURACY, String.valueOf(location.getAccuracy()));
        } else {
            hashMap.put(LONGITUDE, "");
            hashMap.put(LATITUDE, "");
            hashMap.put(ALTITUDE, "");
            hashMap.put(SPEED, "");
            hashMap.put(BEARING, "");
            hashMap.put(ACCURACY, "");
        }

        hashMap.put(WIFI, wifi);

        if(type == TYPE_IN){
            hashMap.put(TYPE, "in");
        }else if(type == TYPE_OUT){
            hashMap.put(TYPE, "out");
        }else{
            hashMap.put(TYPE, "null");
        }

        BehaviorUtil.customEvent(context, X_TEST_GPS, hashMap);
    }

}
