package com.xtc.test.gpstest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;


import com.xtc.log.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * 系统信息获取
 * <p/>
 * Created by hzj on 2016/5/5.
 */
public class SystemInfo {

    private static final String TAG = "gps_test_SystemInfo";

    /**
     * Location access disabled.
     */
    public static final int LOCATION_MODE_OFF = 0;

    /**
     * Best-effort location computation allowed.
     */
    public static final int LOCATION_MODE_HIGH_ACCURACY = 3;

    private static final String MODE_CHANGING_ACTION =
            "com.android.settings.location.MODE_CHANGING";

    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";


    public static final int NETWORK_TYPE_INVALID = 0;//未知网络
    public static final int NETWORK_TYPE_MOBILE = 1;//移动网络
    public static final int NETWORK_TYPE_WIFI = 2;//wifi网络

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public static boolean isOPenGPS(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void setGPSModeEnable(Context context, boolean enable) {
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
    }

    /**
     * 获取网络状态，wifi,mobile.
     *
     * @param context 上下文
     * @return int 网络状态 *{@link #NETWORK_TYPE_INVALID},{@link #NETWORK_TYPE_MOBILE}*
     * {@link #NETWORK_TYPE_WIFI}
     */
    public static int getNetType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            LogUtil.d(TAG, "networkType：" + type);
            if (type.equalsIgnoreCase("WIFI")) {
                return NETWORK_TYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return NETWORK_TYPE_MOBILE;
            } else {
                return NETWORK_TYPE_INVALID;
            }
        } else {
            LogUtil.d(TAG, "networkInfo is null or disconnected.");
            return NETWORK_TYPE_INVALID;
        }
    }

    /**
     * 使用WIFI时，获取本机IP地址
     */
    public static String getWIFILocalIpAdress(Context mContext) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return formatIpAddress(ipAddress);
    }

    private static String formatIpAddress(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * 使用无线网络时，获取本机IP地址
     */
    public static String getMobileLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses();
                     enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    /**
     * 获取IMEI号
     * 需要权限 <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    public static String getImei(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * 是否为cdma
     */
    public static boolean isCDMA(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return TelephonyManager.PHONE_TYPE_CDMA == telephonyManager.getPhoneType();
    }

    /**
     * 无线网络类型
     */
    public static String getNetworkTypeName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method method = telephonyManager.getClass().getDeclaredMethod("getNetworkTypeName");
            return (String) method.invoke(telephonyManager);
        } catch (NoSuchMethodException e) {
            LogUtil.e(TAG, e);
        } catch (InvocationTargetException e) {
            LogUtil.e(TAG, e);
        } catch (IllegalAccessException e) {
            LogUtil.e(TAG, e);
        }
        return "UNKNOWN";
    }

    /**
     * 获取SIM卡的IMSI码
     * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志，
     * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
     * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
     * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
     * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
     */
    public static String getImsi(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telManager.getSubscriberId();
//        if (imsi != null) {
//            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
////         因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
//                //中国移动
//                LogUtil.i("SystemInfo","中国移动");
//            } else if (imsi.startsWith("46001")) {
//                //中国联通
//                LogUtil.i("SystemInfo","中国联通");
//            } else if (imsi.startsWith("46003")) {
//                //中国电信
//                LogUtil.i("SystemInfo","中国电信");
//            }
//        }
    }

    /**
     * 获取手机基站信息
     */
    public static String getGSMCellLocationInfo(Context context, int strength) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int[] operator = getNetworkOperator(mTelephonyManager);
        int mcc = operator[0];
        int mnc = operator[1];

//        MCC，Mobile Country Code，移动国家代码（中国的为460）；
//        MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
//        LAC，Location Area Code，位置区域码；
//        CID，Cell Identity，基站编号；
//        BSSS，Base station signal strength，基站信号强度。

        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        if (cellLocation == null) {
            return null;
        }

        int lac;
        int cellId;
        try {
            if (cellLocation instanceof GsmCellLocation) {
                // 中国移动和中国联通获取LAC、CID的方式
                GsmCellLocation location = (GsmCellLocation) cellLocation;
                lac = location.getLac();
                cellId = location.getCid();
            } else if (cellLocation instanceof CdmaCellLocation) {
                // 中国电信获取LAC、CID的方式
                CdmaCellLocation location = (CdmaCellLocation) cellLocation;
                lac = location.getNetworkId();
                cellId = location.getBaseStationId();
            } else {
                return null;
            }
        } catch (Exception e) {
            LogUtil.e("SystemInfo",e);
            return null;
        }

        LogUtil.i("SystemInfo","strength = " + strength);
        if (strength > 0) {
            strength = strength * 2 - 113;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mcc).append(",")
                .append(mnc).append(",")
                .append(lac).append(",")
                .append(cellId).append(",").append(strength);
        return sb.toString();
    }

    /**
     * 邻区基站信息
     */
    public static String getNeighboringCellInfo(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // 获取邻区基站信息

        List<CellInfo> infos = mTelephonyManager.getAllCellInfo();
        if (infos == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (CellInfo info : infos) { // 根据邻区总数进行循环
            if (info != null) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                try {
                    getBDCellInfo(info, sb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private static StringBuilder getBDCellInfo(CellInfo var1, StringBuilder sb) {
        if (var1 instanceof CellInfoGsm) {
            CellIdentityGsm var5 = ((CellInfoGsm) var1).getCellIdentity();
            sb.append(getValidValue(var5.getMcc())).append(",")
                    .append(getValidValue(var5.getMnc())).append(",")
                    .append(getValidValue(var5.getLac())).append(",")
                    .append(getValidValue(var5.getCid()));

        } else if (var1 instanceof CellInfoCdma) {
            CellIdentityCdma var7 = ((CellInfoCdma) var1).getCellIdentity();
            sb.append("").append(",")
                    .append(getValidValue(var7.getSystemId())).append(",")
                    .append(getValidValue(var7.getNetworkId())).append(",")
                    .append(getValidValue(var7.getBasestationId()));

        } else if (var1 instanceof CellInfoLte) {
            CellIdentityLte var8 = ((CellInfoLte) var1).getCellIdentity();
            sb.append(getValidValue(var8.getMcc())).append(",")
                    .append(getValidValue(var8.getMnc())).append(",")
                    .append(getValidValue(var8.getTac())).append(",")
                    .append(getValidValue(var8.getCi()));
        } else if (var1 instanceof CellInfoWcdma) {
            CellIdentityWcdma var9 = ((CellInfoWcdma) var1).getCellIdentity();
            sb.append(getValidValue(var9.getMcc())).append(",")
                    .append(getValidValue(var9.getMnc())).append(",")
                    .append(getValidValue(var9.getLac())).append(",")
                    .append(getValidValue(var9.getCid()));
        }

        return sb;
    }

    private static int getValidValue(int var1) {
        return var1 == 2147483647 ? -1 : var1;
    }

    private static int[] getNetworkOperator(TelephonyManager mTelephonyManager) {
        // 返回值MCC + MNC
        String operator = mTelephonyManager.getNetworkOperator();
        int mcc = 460;
        int mnc = 0;
        if (!TextUtils.isEmpty(operator) && operator.length() > 3) {
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3));
        }
        int[] result = new int[2];
        result[0] = mcc;
        result[1] = mnc;
        return result;
    }

    /**
     * 获取当前连接的wifi信息
     */
    public static String getMacInfo(Context mContext) {
        StringBuilder sb = new StringBuilder();
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            sb.append(wifiInfo.getBSSID()).append(",")
                    .append(wifiInfo.getRssi()).append(",")
                    .append(wifiInfo.getSSID());
        }
        return sb.toString();
    }

    /**
     * 获取wifi信息
     */
    public static String getNeighboringWifiInfo(WifiManager mWifiManager, boolean isNeedSsid) {
        List<ScanResult> results = getWifiScanResults(mWifiManager);
        if (results == null) {
            LogUtil.d(TAG, "wifi扫描结果为空");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int size = results.size();

        if (size <= 0) {
            LogUtil.d(TAG, "扫描结果size为空");
            return null;
        }

        LogUtil.d(TAG, "wifi扫描结果，size：" + size);
        for (int i = 0; i < size; i++) {
            ScanResult result = results.get(i);
            if (result == null) {
                continue;
            }

            if (TextUtils.isEmpty(result.SSID)) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(result.BSSID).append(",").append(result.level).append(",");
            if (isNeedSsid) {
                sb.append(result.SSID);
            } else {
                sb.append("1");
            }
        }
        return sb.toString();
    }

    /**
     * 获取wifi信息(只含mac(bssid))
     * @return
     */
    public static String getNeighboringSimpleWiFiInfo(WifiManager mWifiManager) {
        List<ScanResult> results = getWifiScanResults(mWifiManager);
        if (results == null) {
            LogUtil.d(TAG, "wifi扫描结果为空");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int size = results.size();
        LogUtil.d(TAG, "wifi扫描结果，size：" + size);
        for (int i = 0; i < size; i++) {
            ScanResult result = results.get(i);
            if (result == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(result.BSSID);
        }
        return sb.toString();
    }

    /**
     * 获取wifi列表
     */
    private static List<ScanResult> getWifiScanResults(WifiManager mWifiManager) {
        List<ScanResult> results = mWifiManager.getScanResults(); //得到扫描结果

        LogUtil.d(TAG, "getWifiScanResults = " + results);

        //再次扫描
//        if (results == null) {
//            mWifiManager.startScan();
//            results = mWifiManager.getScanResults();
//        } else if (results.size() <= 15) {
//            mWifiManager.startScan();
//            List<ScanResult> onceResults = mWifiManager.getScanResults();
//            if (onceResults != null && onceResults.size() > 0) {
//                results.addAll(onceResults);
//            }
//        }

        if (results == null || results.size() <= 0) {
            return null;
        }

        Collections.sort(results, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                if (lhs.level == rhs.level) {
                    return 0;
                }
                return lhs.level > rhs.level ? -1 : 1;// 信号强度排序
            }
        });
        return results;
    }

    /**
     * 判断sim卡是否可用
     */
    public static boolean isEnableSim(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = mTelephonyManager.getSimOperator();
        LogUtil.d(TAG, "operator= " + operator);
        return !TextUtils.isEmpty(operator);
    }
}
