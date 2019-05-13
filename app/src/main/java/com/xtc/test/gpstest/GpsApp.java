package com.xtc.test.gpstest;

import android.app.Application;

import com.xtc.bigdata.collector.BehaviorCollector;
import com.xtc.bigdata.collector.config.BehaviorConfig;
import com.xtc.bigdata.common.constants.Constants;
import com.xtc.common.bigdata.BehaviorUtil;
import com.xtc.log.LogConfig;
import com.xtc.log.crash.CrashHandler;
import com.xtc.log.crash.CrashListener;
import com.xtc.utils.storage.FolderManager;

/**
 * Created by sunlipeng on 2019/4/11.
 */
public class GpsApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FolderManager.getInstance().init("ibwatch", "gpstest");//初始化文件夹
        initLog();
        initBigData();
    }

    private void initLog() {
        LogConfig.builder()
                .isPrintConsole(true)
                .saveLog(true)
                .module("ibwatch") // 手表型号，手表应用调用。
                .appName("com.xtc.test.gpstest") // 手表应用名，手表应用调用。
                .build(LogConfig.Builder.BuildMode.Android);
    }

    /**
     * 大数据初始化
     */
    private void initBigData() {
        BehaviorConfig config = new BehaviorCollector.Builder(this)
                .setHostAppId("com.xtc.i3launcher")
                .setDeviceType(Constants.WATCH)
                .openActivityDurationTrack(true)
                .setDebugMode(BuildConfig.DEBUG)
                .build();
        BehaviorUtil.init(config);
    }
}
