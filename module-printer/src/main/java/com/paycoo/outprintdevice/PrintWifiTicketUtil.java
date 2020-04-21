package com.paycoo.outprintdevice;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.paycoo.cashier.basis.BaseApplication;
import com.paycoo.cashier.basis.trade.TradeConstants;
import com.paycoo.cashier.basis.utils.AppUtils;
import com.paycoo.outprintdevice.bean.BillStatisticPrintBean;
import com.paycoo.outprintdevice.bean.PrintDeviceBean;
import com.paycoo.outprintdevice.bean.UserPayNormalBean;
import com.paycoo.outprintdevice.call.IPrintTicketCallBack;
import com.paycoo.outprintdevice.call.OnDeviceStatusCallback;
import com.paycoo.outprintdevice.call.OnPrintWifiIntercept;
import com.paycoo.outprintdevice.control.BillStatisticBuilder;
import com.paycoo.outprintdevice.control.PrintConstans;
import com.paycoo.outprintdevice.control.UserPayNormalBuild;
import com.paycoo.outprintdevice.printutil.*;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.ICommandBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 网络打印机+
 * 1: 打印IP地址，可以指定，默认获取打印地址列表中第一个打印开关打开状态设备的IP
 * 2: 当传入得打印次数大于1时,第二次打印会在第一次得回调结束调用下次得打印
 * 3、端口默认为9100；也可传入
 * 4、如果主动传入了IP和端口号，页面退出前完请调用  cleanPrintCallBackListener() 方法
 */

public class PrintWifiTicketUtil {
    //打印回调监听
    private IPrintTicketCallBack printTciketCallBack;
    //打印前信息拦截器
    private OnPrintWifiIntercept onPrintWifiIntercept;
    private Queue<byte[]> mQueuePrintCount = new LinkedList<>();
    private boolean isPrint = false;
    private String mHttpConnectIp = "", mHttpConnectPort = "";
    private Context mContext;

    public static PrintWifiTicketUtil getInstance() {
        return PrintInstance.instance;
    }

    private static class PrintInstance {
        private static PrintWifiTicketUtil instance = new PrintWifiTicketUtil();
    }


    private PrintWifiTicketUtil() {
        mContext = BaseApplication.INSTANCE.getAppLocation();
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setPrintTicketCallBack(IPrintTicketCallBack printTciketCallBack) {
        this.printTciketCallBack = printTciketCallBack;
    }

    public void setmHttpConnectIp(String mHttpConnectIp) {
        this.mHttpConnectIp = mHttpConnectIp;
    }

    public void setmHttpConnectIp(String mHttpConnectIp, String port) {
        this.mHttpConnectIp = mHttpConnectIp;
        mHttpConnectPort = port;
    }

    /**
     * 清除回调通知(onDestroy())
     */
    public void cleanPrintCallBackListener() {
        printTciketCallBack = null;
        onPrintWifiIntercept = null;
        mHttpConnectIp = "";
        mHttpConnectPort = "";

    }

    private final SendCallback mCallback = new SendCallback() {
        @Override
        public void onStatus(boolean result, Result communicateResult) {
            isPrint = false;
            String msg;
            switch (communicateResult) {
                case Success:
                    msg = "Success!";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onSuccess();
                    }
                    //多次打印
                    if (mQueuePrintCount.size() > 0) {
                        send(mQueuePrintCount.poll());
                    }
                    break;
                case ErrorOpenPort:
                    msg = "Fail to openPort";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.connectFail();
                    }
                    break;
                case ErrorBeginCheckedBlock:
                    msg = "Printer is offline (beginCheckedBlock) check printer";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onPrintBeginCheckedOffline();
                    }
                    break;
                case ErrorReadPort:
                    msg = "Read port error (readPort)";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onFail();
                    }
                    break;
                case ErrorWritePort:
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onFail();
                    }
                    msg = "Write port error (writePort)";
                    break;
                case ErrorPageEmpty:
                    msg = "Receipt paper is empty";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onPageEmpty();
                    }
                    break;
                default:
                    msg = "Unknown error";
                    if (printTciketCallBack != null) {
                        printTciketCallBack.onPrintErrorUnKnown();
                    }
                    break;
            }
            if (printTciketCallBack != null) {
                printTciketCallBack.onPrintFinish();
            }
        }
    };

    /**
     * 测试总计单
     */
    public void _testPrintUserPayData() {
        UserPayNormalBuild build = new UserPayNormalBuild();
        build.addTopTitle("签购单")
                .addMerChantName("时间小酒店")
                .addMerChantNumber("s78253697512569")
                .addTerMinalNumber("s78253697512569")
                .addTransactiontype(TradeConstants.TRADE_TYPE_CONSUME)
                .addPayType(TradeConstants.PAY_MODE_ID_WX)
                .addPayTypeText("支付宝")
                .addTradeTypeText("消费")
                .addCardOrganization("天府银行")
                .addAcquireeOrganization("天府银行")
                .addCardNumber("52692****3685")
                .addTermOfValidity("2019/12")
                .addBatchNumber("000059")
                .addVouncherNo("000059")
                .addTransactionReferenceNumber("0000590122022020202022")
                .addPayTime("2019/7/10 15:52:25")
                .addPayMoney("2088.36")
                .addPrintAgainHint(false)
                .addTransactionNumber("0000590122022020202022")
                .addBusinessAccount("000059012202202055")
                .addQrCodeContent("http://sdas45asd5a4da5d4a5da4s5");
        UserPayNormalBean bean = build.build();
        printUserPayData(bean);
    }

    /**
     * 测试消费-退货-撤销消费单子
     */
    public void _testPrintUserStatisticBill() {
        BillStatisticBuilder builder = new BillStatisticBuilder();
        builder.addBillPayItemInfoLineMessageData("合计", "3", "58.25")
                .addBillPayItemInfoLineMessageData("消费", "3", "58.25")
                .addBillPayItemInfoLineMessageData("退款", "3", "58.25")
                .addBillInfoTypeFinishData("支付宝")
                .addBillPayItemInfoLineMessageData("合计", "3", "58.25")
                .addBillPayItemInfoLineMessageData("消费", "3", "58.25")
                .addBillPayItemInfoLineMessageData("退款", "3", "58.25")
                .addBillInfoTypeFinishData("银联卡")
                .addPayCount("8")
                .addBatchNumber("78122558552")
                .addPayMoney("258.25")
                .addSettleAccountTime("2019-07-10 15:51:52")
                .addStartTime("2019-07-10 15:51:52")
                .addEndTime("2019-07-10 15:51:52")
                .addEnCode("f5f5f8v8")
                .addRefundTransactionInfo("银联卡退款", "2019-07-10 15:51:52", "2555.36"
                        , "退款成功", "sh56325s6s5a6a0s2sd6a5d3saasds5")
                .addPayTransactionInfo("银联卡支付", "2019-07-10 15:51:52", "2555.36"
                        , "2558.36", "sh56325s6s5a6a0s2sd6a5d3saasds5");
        BillStatisticPrintBean bean = builder.build();
        printUserStatisticBill(bean);
    }


    /**
     * 打印测试
     */
    public void testPrint() {
        ICommandBuilder builder = StartLineUtil.getCommandBuilder();
        builder.appendCharacterSpace(0);
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.append(("\n" + "Hellow World" + "\n").getBytes(StartLineUtil.getEncoding()));
        builder.appendLineFeed(4);
        builder.appendCutPaper(ICommandBuilder.CutPaperAction.FullCut);
        send(builder.getCommands());
    }

    /**
     * 打印消费者:消费--退货--消费撤销
     * 默认打印一次
     *
     * @param bean
     */

    public void printUserPayData(UserPayNormalBean bean) {
        printUserPayData(bean, 1);
    }

    public void printUserPayData(UserPayNormalBean bean, int printCount) {
        if (onPrintWifiIntercept != null) {
            bean = (UserPayNormalBean) onPrintWifiIntercept.onPrintMsgInfo(printCount, bean);
        }
        ICommandBuilder builder = StartLineUtil.getCommandBuilder();
        builder.appendCharacterSpace(0);
        if (bean.topLogoImg != null) {
            builder.appendBitmapWithAlignment(bean.topLogoImg, false, ICommandBuilder.AlignmentPosition.Center);
            builder.appendCharacterSpace(0);
            builder.append(getPrintByteData(StartLineUtil.line));
        }
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendMultipleHeight(getPrintByteData(bean.topTitle), 2);
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
        builder.append(getPrintByteData(StartLineUtil.line));
        if (!TextUtils.isEmpty(bean.merChantName)) {
            builder.append(getPrintByteData("商户名：" + bean.merChantName));
        }
        if (!TextUtils.isEmpty(bean.storeName)) {
            builder.append(getPrintByteData("门店名：" + bean.storeName));
        }
        if (TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_BANK) ||
                TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_VISA)) {
            builder.append(getPrintByteData("商户号：" + bean.merChantNumber));
            builder.append(getPrintByteData("门店号：" + bean.storeNo));
        }
        if (!TextUtils.isEmpty(bean.terminalNumber)) {
            builder.append(getPrintByteData("终端号：" + bean.terminalNumber));
        }
        builder.append(getPrintByteData(StartLineUtil.line));
        builder.append(getPrintByteData("交易时间：" + bean.payTime));
        builder.append(getPrintByteData("交易类型："));
        builder.appendMultipleHeight(getPrintByteData(bean.tradeTypeText + "/" + bean.payTypeText), 2);
        builder.append(getPrintByteData("交易金额："));
        builder.appendMultipleHeight(getPrintByteData("RMB " + bean.receivableMoney), 2);
        String platformMoney = bean.platformMoney;
        if (!TextUtils.isEmpty(platformMoney) && Double.valueOf(platformMoney) > 0) {
            builder.append(getPrintByteData("优惠金额：RMB  " + platformMoney));
        }
        String payMoney = bean.payMoney;
        if (!TextUtils.isEmpty(payMoney) && Double.valueOf(payMoney) > 0) {
            builder.append(getPrintByteData("实付金额：RMB  " + payMoney));
        }
        if (TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_BANK) ||
                TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_VISA)) {
            builder.append(getPrintByteData("卡号："));
            builder.appendMultipleHeight(getPrintByteData(bean.cardNumber), 2);
            builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);

            if (!TextUtils.isEmpty(bean.cardLssuer)) {
                builder.append(getPrintByteData("发卡行：" + bean.cardLssuer));
            }

            if (!TextUtils.isEmpty(bean.acquirer)) {
                builder.append(getPrintByteData("收单行：" + bean.acquirer));
            }

            builder.append(getPrintByteData(" 批次号：" + bean.batchNumber + "凭证号：" + bean.vouncherNo));
            if (!TextUtils.isEmpty(bean.authCode)) {
                builder.append(getPrintByteData("授权码：" + bean.authCode));
            }
            if (!TextUtils.isEmpty(bean.transactionReferenceNumber)) {
                builder.append(getPrintByteData("参考号：" + bean.transactionReferenceNumber));
            }
            //小号字体
            builder.appendCharacterSpace(0);
            builder.append(getPrintByteData(StartLineUtil.line));
            //小号字体
            builder.appendCharacterSpace(0);
            builder.append(getPrintByteData("交易号：" + bean.payNumber));
            if (!TextUtils.isEmpty(bean.orig_trans_no)) {
                builder.appendCharacterSpace(0);
                builder.append(getPrintByteData("原交易号：" + bean.orig_trans_no));
            }
            if (!TextUtils.isEmpty(bean.out_order_no)) {
                builder.appendCharacterSpace(0);
                builder.append(getPrintByteData("外部订单号：" + bean.out_order_no));
            }
        } else {
            builder.append(getPrintByteData("付款账户：" + bean.businessAccount));
            builder.append(getPrintByteData("交易号："));
            builder.append(getPrintByteData(bean.payNumber));
            if (!TextUtils.isEmpty(bean.transactionReferenceNumber)) {
                builder.append(getPrintByteData("参考号：" + bean.transactionReferenceNumber));
            }
            if (!TextUtils.isEmpty(bean.orig_trans_no)) {
                builder.append(getPrintByteData("原交易号：" + bean.orig_trans_no));
            }
            if (!TextUtils.isEmpty(bean.out_order_no)) {
                builder.append(getPrintByteData("外部订单号：" + bean.out_order_no));
            }
        }
        if (!TextUtils.isEmpty(bean.description)) {
            builder.append(getPrintByteData("备注："));
            builder.append(getPrintByteData(bean.description));
        }
        builder.appendCharacterSpace(0);
        builder.append(getPrintByteData(StartLineUtil.line));

        if (bean.printSign) {
            if (bean.signBitmap == null) {
                if (Float.valueOf(bean.payMoney) >= 1000) {
                    builder.append(getPrintByteData(PrintConstans.PAY_MONEY_MORE_THAN_1000 + "\n\n"));
                } else {
                    if (TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_BANK) ||
                            TextUtils.equals(bean.payType, TradeConstants.PAY_MODE_ID_VISA)) {
                        builder.append(getPrintByteData(PrintConstans.PAY_MONEY_NO_MORE_THAN_1000));
                    } else {
                        builder.append(getPrintByteData(PrintConstans.PAY_MONEY_MORE_THAN_1000 + "\n\n"));
                    }
                }
            } else {
                builder.append(getPrintByteData(PrintConstans.PAY_MONEY_MORE_THAN_1000));
                builder.appendBitmapWithAlignment(bean.signBitmap, false, ICommandBuilder.AlignmentPosition.Center);
            }
            builder.appendCharacterSpace(0);
            builder.append(getPrintByteData(StartLineUtil.line));
        }

        builder.appendCharacterSpace(0);
        builder.append(getPrintByteData(PrintConstans.CONFIRM_NO_ERROR_INTO_BUSINESS_ACCOUNT));

        String deviceInfo = bean.storeNo;
        if (!TextUtils.isEmpty(bean.deviceEn)) {
            deviceInfo += TextUtils.isEmpty(deviceInfo) ? bean.deviceEn : "_" + bean.deviceEn;
        }
        if (!TextUtils.isEmpty(bean.merChantNumber)) {
            deviceInfo += "(" + bean.merChantNumber + ")";
        }
        if (!TextUtils.isEmpty(deviceInfo)) {
            builder.appendCharacterSpace(0);
            builder.append(getPrintByteData(deviceInfo));
        }
        String branchName = bean.branchName;

        String deviceInfoMsg = TextUtils.isEmpty(branchName) ? "csp//" + AppUtils.getAppVersion() : "csp/" + branchName + "/" + AppUtils.getAppVersion();
        builder.appendCharacterSpace(0);
        builder.append(getPrintByteData(deviceInfoMsg));
        builder.append(getPrintByteData(StartLineUtil.line));
        builder.append(getPrintByteData(bean.savePage));
        if (bean.isPrintQrCode) {
            if (!TextUtils.isEmpty(bean.qrCodeContent)) {
                builder.appendQrCodeWithAlignment(bean.qrCodeContent.getBytes(StartLineUtil.getEncoding()),
                        ICommandBuilder.QrCodeModel.No2, ICommandBuilder.QrCodeLevel.L, 8, ICommandBuilder.AlignmentPosition.Center);
                //小号字体
                builder.appendCharacterSpace(0);
                builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
                builder.append(getPrintByteData("\n" + PrintConstans.PLAEASE_USE_POS_SCAN_QR_CODE_HINT));
            }
        }
        //小号字体
        builder.appendCharacterSpace(0);
        builder.append(getPrintByteData(StartLineUtil.line));
        int curpage = bean.printCutPage < 4 ? 4 : bean.printCutPage;
        builder.appendLineFeed(curpage);
        builder.appendCutPaper(ICommandBuilder.CutPaperAction.FullCut);
        mQueuePrintCount.clear();
        send(builder.getCommands());
    }


    /**
     * 打印消费者统计账单
     * 默认一次
     *
     * @param bean
     */
    public void printUserStatisticBill(BillStatisticPrintBean bean) {
        printUserStatisticBill(bean, 1);
    }

    public void printUserStatisticBill(BillStatisticPrintBean bean, int printCount) {
        if (onPrintWifiIntercept != null) {
            bean = (BillStatisticPrintBean) onPrintWifiIntercept.onPrintMsgInfo(printCount, bean);
        }
        ICommandBuilder builder = StartLineUtil.getCommandBuilder();
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
        builder.appendMultipleHeight(getPrintByteData("结算总计单"), 2);
        builder.appendCharacterSpace(0);
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
        builder.append(getPrintByteData(StartLineUtil.line));
        //=============支付总金额信息========
        List<BillStatisticPrintBean.BillInfoType> billTypeList = bean.listBillInfo;
        for (int i = 0; i < billTypeList.size(); i++) {
            BillStatisticPrintBean.BillInfoType billInFoTypeBean = billTypeList.get(i);
//            builder.append(StartLineUtil.getThreeAlignLeftTextAndCenterTextAndRightTextStr(billInFoTypeBean.getPayName()
//                    , "笔数", "金额").getBytes(StartLineUtil.getEncoding()));
            List<BillStatisticPrintBean.BillPayItem> billInfoList = billInFoTypeBean.getBillPayItemList();
            for (int j = 0; j < billInfoList.size(); j++) {
                BillStatisticPrintBean.BillPayItem billPayBean = billInfoList.get(j);
                builder.append(StartLineUtil.getThreeAlignLeftTextAndCenterCharAndRightCharStr(billPayBean.itemLeftTitleText
                        , billPayBean.itemCenterPayCountText, billPayBean.itemRightPayMoneyText).getBytes(StartLineUtil.getEncoding()));
            }
            builder.append(getPrintByteData(StartLineUtil.line));
        }
        //=============总计信息===========
        builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
        builder.appendMultipleHeight(getPrintByteData("总计"), 2);
        BillStatisticPrintBean.BillInfo billInfoBean = bean.billInfo;
        builder.append(getPrintByteData("交易笔数：" + billInfoBean.transactionCount));
        builder.append(getPrintByteData("结算批次号：" + billInfoBean.batchNumber));
        builder.append(getPrintByteData("结算金额："));
        builder.append(getPrintByteData(billInfoBean.billMoney));
        builder.append(getPrintByteData("结算时间：" + billInfoBean.settleAccountsTime));
        builder.append(getPrintByteData("开始时间：" + billInfoBean.startTime));
        builder.append(getPrintByteData("结束时间：" + billInfoBean.endTime));
        builder.append(getPrintByteData("设备EN号：" + billInfoBean.enCode));
        builder.appendCharacterSpace(0);
        builder.append(getPrintByteData("注：以上统计仅供参考，资金以实际到账为准"));

        builder.appendLineFeed(3);
        //===========.=交易明细============
        List<BillStatisticPrintBean.TransactionInfo> transactionInfoList = bean.listTransaction;
        if (transactionInfoList != null && transactionInfoList.size() > 0) {
            builder.appendAlignment(ICommandBuilder.AlignmentPosition.Center);
            builder.appendMultipleHeight(getPrintByteData("交易明细"), 2);
            builder.appendCharacterSpace(0);
            builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
            builder.append((StartLineUtil.line + "\n").getBytes(StartLineUtil.getEncoding()));

            for (int i = 0; i < transactionInfoList.size(); i++) {
                BillStatisticPrintBean.TransactionInfo transactionInfoBean = transactionInfoList.get(i);
                builder.append(StartLineUtil.getTwoAlignLeftTextAndRightCharStr(transactionInfoBean.typeName, transactionInfoBean.typeTime)
                        .getBytes(StartLineUtil.getEncoding()));
                if (!TextUtils.isEmpty(transactionInfoBean.state)) {
                    //打印退款
                    builder.append(StartLineUtil.getTwoAlignLeftTextAndRightCharStr("退款金额", transactionInfoBean.refundMoney)
                            .getBytes(StartLineUtil.getEncoding()));
                    builder.append(StartLineUtil.getTwoAlignLeftTextAndRightTextStr("退款状态", transactionInfoBean.state)
                            .getBytes(StartLineUtil.getEncoding()));
                } else {
                    //打印支付
                    builder.append(StartLineUtil.getTwoAlignLeftTextAndRightCharStr("商户应收"
                            , transactionInfoBean.merChantGetMoney).getBytes(StartLineUtil.getEncoding()));
                    builder.append(StartLineUtil.getTwoAlignLeftTextAndRightCharStr("用户实付"
                            , transactionInfoBean.consumerPayMoney).getBytes(StartLineUtil.getEncoding()));
                }

                builder.append(getPrintByteData("交易号：" + transactionInfoBean.orderNumber));
                builder.appendCharacterSpace(0);
                builder.appendAlignment(ICommandBuilder.AlignmentPosition.Left);
                builder.append(getPrintByteData(StartLineUtil.line));
            }
            builder.appendLineFeed(4);
        }

        builder.appendCutPaper(ICommandBuilder.CutPaperAction.FullCut);
        mQueuePrintCount.clear();
        send(builder.getCommands());
    }

    private byte[] getPrintByteData(String printMsg) {
        return StartLineUtil.get1LinePrintTextByteArray(printMsg);
    }

    private String getPayTypeFromType(int type, int payType) {
        switch (type) {
            case PrintConstans.TRANSACTION_TYPE_CONSUM:
                return "消费";
            case PrintConstans.TRANSACTION_TYPE_RETURN_GOODS:
                if (payType == PrintConstans.PAY_TYPE_BANK_CARD) {
                    return "退货";
                } else {
                    return "退款";
                }
            default:
                return "消费撤销";
        }
    }

    private void send(byte[] data) {
        PrintDeviceBean bean = getHttpConnectDataIp();
        String ip = bean.getHttpIp();
        if (!TextUtils.isEmpty(ip)) {
            if (!ip.startsWith("TCP")) {
                ip = "TCP:" + ip;
            }
            isPrint = true;
            String port = "";
            if (!TextUtils.isEmpty(mHttpConnectPort)) {
                port = mHttpConnectPort;
            } else {
                port = bean.getHttpPort();
                if (TextUtils.isEmpty(port)) {
                    port = String.valueOf(PrintConstans.CONNECT_PORT);
                }
            }
            if (mContext == null) {
                Log.e("11", "mContext  is null,connect fail");
                return;
            }
            SendCommandThread mPrintThread = new SendCommandThread(this, data, ip, port,
                    PrintConstans.CONNECT_TIME_OUT, mContext, mCallback);
            mPrintThread.start();
        }
    }

    private PrintDeviceBean getHttpConnectDataIp() {
        PrintDeviceBean result = new PrintDeviceBean();
        if (!TextUtils.isEmpty(mHttpConnectIp)) {
            result.setHttpIp(mHttpConnectIp);
            result.setHttpPort(mHttpConnectPort);
            return result;
        }

        List<PrintDeviceBean> list = PrintDeviceDataManager.getPrintDevideData(mContext);

        if (list == null || list.size() == 0) {
            if (printTciketCallBack != null) {
                printTciketCallBack.onNoAddConnectAddress();
            }
            return result;
        }
        for (int i = 0; i < list.size(); i++) {
            PrintDeviceBean bean = list.get(i);
            if (bean.isOpen()) {
                result = bean;
                break;
            }
        }
        if (printTciketCallBack != null) {
            if (TextUtils.isEmpty(result.getHttpIp())) {
                printTciketCallBack.onConnectAddressAllClose();
            }
        }
        return result;
    }

    /**
     * 检查打印机连接状态
     *
     * @param ip
     * @param port
     * @param l
     */
    public void onCheckDeviceStatus(String ip, String port, OnDeviceStatusCallback l) {
        if (!ip.startsWith("TCP")) {
            ip = "TCP:" + ip;
        }
        String connectPort = TextUtils.isEmpty(port) ? String.valueOf(PrintConstans.CONNECT_PORT) : port;
        StateCallBack callBack = new StateCallBack(l);
        RetrieveStatusThread thread = new RetrieveStatusThread(this, 0,
                ip, connectPort, PrintConstans.CONNECT_TIME_OUT,
                BaseApplication.INSTANCE.getAppLocation(), callBack);
        thread.start();
    }

    class StateCallBack implements SendCallback.StatusCallback {
        private OnDeviceStatusCallback listener;

        public StateCallBack(OnDeviceStatusCallback l) {
            listener = l;
        }

        @Override
        public void onStatus(int itemId, String ip, StarPrinterStatus result) {
            boolean isConnectState = result != null && result.rawLength != 0 && !result.offline;
            if (null != listener) {
                listener.onItemConnectStateChange(ip, isConnectState);
            }
        }
    }
}
