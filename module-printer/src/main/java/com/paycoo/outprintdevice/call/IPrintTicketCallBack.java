package com.paycoo.outprintdevice.call;

public interface IPrintTicketCallBack {


    void onSuccess();

    void onFail();

    void connectFail();

    /**
     * 添加的网络打印地址全部呈关闭状态
     */
    void onConnectAddressAllClose();

    /**
     * 没有添加网络打印连接地址
     */
    void onNoAddConnectAddress();
    /**
     * 缺纸
     */
    void onPageEmpty();

    /**
     * 打印前检查脱机状态(未打印...检查打印机(也可能没有纸打印)）
     */
    void onPrintBeginCheckedOffline();

    /**
     * 未知异常
     */
    void onPrintErrorUnKnown();

    /**
     * 本次操作结束，(无论成功-失败-或者其他异常)
     */
    void onPrintFinish();
}
