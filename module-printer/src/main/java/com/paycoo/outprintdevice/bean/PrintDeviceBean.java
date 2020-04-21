package com.paycoo.outprintdevice.bean;

import java.io.Serializable;

public class PrintDeviceBean implements Serializable {
    public String httpIp = "";
    public String httpPort= "";
    public boolean deviceConnectState = false;
    public boolean isOpen = true;


    public String getHttpIp() {
        return httpIp;
    }

    public void setHttpIp(String httpIp) {
        this.httpIp = httpIp;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isDeviceConnectState() {
        return deviceConnectState;
    }

    public void setDeviceConnectState(boolean deviceConnectState) {
        this.deviceConnectState = deviceConnectState;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public String toString() {
        return "OutPrintDeviceBean{" +
                "httpIp='" + httpIp + '\'' +
                ", httpPort='" + httpPort + '\'' +
                ", deviceConnectState=" + deviceConnectState +
                ", isOpen=" + isOpen +
                '}';
    }
}
