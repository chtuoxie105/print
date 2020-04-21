package com.paycoo.outprintdevice.call;

public interface INorMalPrintListener {
    void onPrintSuccess();
    void onPrintFail(String errorMsg);
}
