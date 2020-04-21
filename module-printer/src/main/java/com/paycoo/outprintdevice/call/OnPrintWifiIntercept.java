package com.paycoo.outprintdevice.call;

/**
 * 打印得信息打印前拦截更改器
 */
public interface OnPrintWifiIntercept {
    Object onPrintMsgInfo(int curPrintCount, Object object);
}
