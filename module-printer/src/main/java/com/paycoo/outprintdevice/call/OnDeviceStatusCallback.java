package com.paycoo.outprintdevice.call;


public interface OnDeviceStatusCallback {
    void onItemConnectStateChange(String connectIp,boolean isConnect);
}
