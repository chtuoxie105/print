package com.paycoo.outprintdevice.printutil;

import com.starmicronics.stario.StarPrinterStatus;

public interface SendCallback {
    void onStatus(boolean result, Result communicateResult);
    interface StatusCallback {
        void onStatus(int itemId, String ip, StarPrinterStatus result);
    }
    public enum Result {
        Success,
        ErrorOpenPort,
        ErrorPageEmpty,
        ErrorBeginCheckedBlock,
        ErrorWritePort,
        ErrorReadPort,
    }
}
