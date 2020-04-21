package com.paycoo.outprintdevice.printutil;

import android.content.Context;
import android.text.TextUtils;
import com.paycoo.cashier.basis.BaseApplication;
import com.paycoo.cashier.basis.utils.SharedPreferenceUtils;
import com.paycoo.outprintdevice.bean.PrintDeviceBean;
import com.paycoo.outprintdevice.control.PrintConstans;

import java.util.ArrayList;
import java.util.List;

/**
 * 打印设备管理器
 */
public class PrintDeviceDataManager {
    private static final String ACTION_DEVICE_DATA_INFO_FILE_NAME = "ACTION_DEVICE_DATA_INFO_FILE_NAME";
    private static final String ACTION_DEVICE_DATA_INFO_FILE_KEY = "ACTION_DEVICE_DATA_INFO_FILE_KEY";


    public static void savePrintDevideData(Context c, PrintDeviceBean printBean) {
        List<PrintDeviceBean> l = getPrintDevideData(c);
        if (l == null) {
            l = new ArrayList<>();
        }
        l.add(printBean);
        SharedPreferenceUtils.saveInfo(c, l, ACTION_DEVICE_DATA_INFO_FILE_NAME, ACTION_DEVICE_DATA_INFO_FILE_KEY);
    }

    public static void savePrintDevideData(Context c, List<PrintDeviceBean> list) {
        SharedPreferenceUtils.saveInfo(c, list, ACTION_DEVICE_DATA_INFO_FILE_NAME, ACTION_DEVICE_DATA_INFO_FILE_KEY);
    }

    public static List<PrintDeviceBean> getPrintDevideData(Context c) {
        return (List<PrintDeviceBean>) SharedPreferenceUtils.getInfo(c, ACTION_DEVICE_DATA_INFO_FILE_NAME, ACTION_DEVICE_DATA_INFO_FILE_KEY);
    }

    public static void cleanSaveDeviceList(Context c) {
        SharedPreferenceUtils.saveInfo(c, new ArrayList<PrintDeviceBean>(), ACTION_DEVICE_DATA_INFO_FILE_NAME, ACTION_DEVICE_DATA_INFO_FILE_KEY);
    }

    /**
     * 获取可用的IP连接地址
     *
     * @return
     */
    public static PrintDeviceBean getConnectDeviceData() {
        PrintDeviceBean result = new PrintDeviceBean();
        List<PrintDeviceBean> list = getPrintDevideData(BaseApplication.INSTANCE.init());

        if (list == null || list.size() == 0) {
            return result;
        }
        for (int i = 0; i < list.size(); i++) {
            PrintDeviceBean bean = list.get(i);
            if (bean.isOpen()) {
                result = bean;
                break;
            }
        }
        if (!TextUtils.isEmpty(result.getHttpIp())) {
            if (TextUtils.isEmpty(result.getHttpPort())) {
                result.setHttpPort(String.valueOf(PrintConstans.CONNECT_PORT));
            }
        }
        return result;
    }
}
