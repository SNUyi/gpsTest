package com.xtc.test.gpstest;

/**
 * Created by huangyichao on 2018/9/22.
 */
public interface GpsConfigContract {
    String Action_Gps = "xtc.action.Config_Gps";

    /**
     * 注意不能传 int 类型，要转成 long
     */
    String KeyLong_remainTimeMillis = "remainTimeMillis";

    /** 多个应用请求时，用于区别是否需要关闭 gps，不能为空 */
    String KeyString_packageName = "packageName";
    /** 同一应用请求时，用于区别是否需要关闭 gps，不能为 -1 */
    String KeyInt_requestCode = "requestCode";
    int requestCode_Illegal = 0;

    /**
     * type 不设置， 即为默认。
     * isOpen 若为 true，必须有 {@link GpsConfigContract#KeyLong_remainTimeMillis}。
     */
    String KeyInt_type = "type";
    int type_Default = 0;

    /**
     * 打开或关闭 gps，extra 必须带有 {@link GpsConfigContract#KeyInt_type}
     */
    String KeyBool_isOpen = "isOpen";

}

