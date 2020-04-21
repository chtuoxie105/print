package com.paycoo.outprintdevice.call;

public class NorMalPrintListener implements IPrintTicketCallBack, INorMalPrintListener {

    @Override
    public void onSuccess() {
        onPrintSuccess();
    }

    @Override
    public void onFail() {
        onPrintFail("打印失败");
    }

    @Override
    public void connectFail() {
        onPrintFail("打印机连接失败");
    }

    @Override
    public void onConnectAddressAllClose() {
        onPrintFail("打印机连接地址不可用");
    }

    @Override
    public void onNoAddConnectAddress() {
        onPrintFail("打印机连接失败");
    }

    @Override
    public void onPageEmpty() {
        onPrintFail("打印机缺纸");
    }

    @Override
    public void onPrintBeginCheckedOffline() {
        onPrintFail("请打印机是否处于工作状态或者缺纸");
    }

    @Override
    public void onPrintErrorUnKnown() {
        onPrintFail("打印机未知错误");
    }

    @Override
    public void onPrintFinish() {
    }


    @Override
    public void onPrintSuccess() {
        
    }

    @Override
    public void onPrintFail(String errorMsg) {

    }
}
