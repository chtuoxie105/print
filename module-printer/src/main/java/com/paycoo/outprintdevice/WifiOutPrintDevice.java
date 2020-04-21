package com.paycoo.outprintdevice;

import android.text.TextUtils;
import com.paycoo.cashier.basis.data.*;
import com.paycoo.cashier.basis.device.listener.DevicePrinterListener;
import com.paycoo.cashier.basis.device.listener.OnOutPrintDeviceConnectStatusListener;
import com.paycoo.cashier.basis.device.outprint.AbstractOutPrintDevice;
import com.paycoo.cashier.basis.storage.TradeTypePluginManager;
import com.paycoo.cashier.basis.trade.PrintStringUtil;
import com.paycoo.cashier.basis.trade.TradeConstants;
import com.paycoo.cashier.basis.utils.AmountUtil;
import com.paycoo.cashier.basis.utils.BankUtil;
import com.paycoo.cashier.basis.utils.BitmapUtil;
import com.paycoo.cashier.basis.utils.PrintQRCodeUtils;
import com.paycoo.outprintdevice.bean.BillStatisticPrintBean;
import com.paycoo.outprintdevice.bean.UserPayNormalBean;
import com.paycoo.outprintdevice.call.*;
import com.paycoo.outprintdevice.control.BillStatisticBuilder;
import com.paycoo.outprintdevice.control.UserPayNormalBuild;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 网路IP打印机
 */
public class WifiOutPrintDevice extends AbstractOutPrintDevice {
    private PrinterData<BaseData> mData = null;
    private int printerCouple = 1;

    @Override
    public void onCheckPrintConnectStatus(@NotNull final OutPrintDeviceBean device, @NotNull final OnOutPrintDeviceConnectStatusListener l) {
        PrintWifiTicketUtil.getInstance().onCheckDeviceStatus(device.getHttpIp(), device.getHttpPort(), new OnDeviceStatusCallback() {
            @Override
            public void onItemConnectStateChange(String connectIp, boolean isConnect) {
                l.onConnectStatusCheckResult(device, isConnect);
            }
        });
    }

    @Override
    public void print(@NotNull OutPrintDeviceBean device, @NotNull PrinterData<BaseData> data, @NotNull final DevicePrinterListener listenerDevice) {
        if (TextUtils.isEmpty(device.getHttpIp())) {
            listenerDevice.onPrintFail("请配置打印机IP连接地址");
            return;
        }
        mData = data;
        printerCouple = 1;
        PrintWifiTicketUtil.getInstance().setPrintTicketCallBack(new NorMalPrintListener() {
            @Override
            public void onPrintSuccess() {
                if (printerCouple < mData.getPrinter_couple()) {
                    if (mData.getPrinter_type() == PrinterData.PrintType.TRADE_TYPE) {
                        TradeRecordInfo info = (TradeRecordInfo) mData.getData();
                        if (info.isNeedWaitPrint()) {
                            listenerDevice.onPrintTradeMerChantSuccess();
                        } else {
                            startPrintTradeConsume();
                        }
                    }
                } else {
                    mData = null;
                    listenerDevice.onPrintSuccess();
                    PrintWifiTicketUtil.getInstance().cleanPrintCallBackListener();
                }
            }

            @Override
            public void onPrintFail(String errorMsg) {
                listenerDevice.onPrintFail(errorMsg);
            }
        });
        PrintWifiTicketUtil.getInstance().setmHttpConnectIp(device.getHttpIp(), device.getHttpPort());
        if (data.getPrinter_type() == PrinterData.PrintType.TRADE_TYPE) {
            PrintWifiTicketUtil.getInstance().printUserPayData(creatPrintTradeQrCodeData(data, printerCouple));
        } else {
            PrintWifiTicketUtil.getInstance().printUserStatisticBill(creatPrintSettltData());
        }
    }

    @Override
    public void startPrintTradeConsume() {
        ++printerCouple;
        if (null != mData) {
            PrintWifiTicketUtil.getInstance().printUserPayData(creatPrintTradeQrCodeData(mData, printerCouple));
        }
    }

    @Override
    public void printTest(@NotNull OutPrintDeviceBean device, @NotNull String data, @NotNull final DevicePrinterListener listenerDevice) {
        PrintWifiTicketUtil.getInstance().setPrintTicketCallBack(new NorMalPrintListener() {
            @Override
            public void onPrintSuccess() {
                if (printerCouple < mData.getPrinter_couple()) {
                    listenerDevice.onPrintTradeMerChantSuccess();
                } else {
                    mData = null;
                    listenerDevice.onPrintSuccess();
                    PrintWifiTicketUtil.getInstance().cleanPrintCallBackListener();
                }
            }

            @Override
            public void onPrintFail(String errorMsg) {
                listenerDevice.onPrintFail(errorMsg);
            }
        });
        PrintWifiTicketUtil.getInstance().setmHttpConnectIp(device.getHttpIp(), device.getHttpPort());
        PrintWifiTicketUtil.getInstance().testPrint();
    }

    /**
     * 交易签购单
     */
    private UserPayNormalBean creatPrintTradeQrCodeData(PrinterData<BaseData> data, int printNum) {
        UserPayNormalBuild builder = new UserPayNormalBuild();
        TradeRecordInfo info = (TradeRecordInfo) data.getData();
        if (!TradeTypePluginManager.INSTANCE.isDevicePayCloud()) {
            info.setMerchant_name("");
        }
        int discountMoney = 0;
        if (!TextUtils.isEmpty(info.getDiscount_bpc())) {
            discountMoney = AmountUtil.changeY2FNoDMark(info.getDiscount_bpc());
        }
        if (!TextUtils.isEmpty(info.getDiscount_bmopc())) {
            discountMoney += AmountUtil.changeY2FNoDMark(info.getDiscount_bmopc());
        }
        String customPayAmount = info.getTrans_amount();
        if (!TextUtils.isEmpty(info.getCustomer_paid_amount())) {
            int cusPay = AmountUtil.changeY2FNoDMark(info.getCustomer_paid_amount());
            if (cusPay > 0) {
                customPayAmount = info.getCustomer_paid_amount();
            }
        }
        String refNo = info.getRef_no();
        if (TextUtils.isEmpty(refNo)) {
            refNo = info.getPay_channel_trans_no();
        }
        String termOfValidity = "";
        String cardTypeText = "";
        if (TextUtils.equals(info.getPayment_method(), TradeConstants.PAY_MODE_ID_BANK) ||
                TextUtils.equals(info.getPayment_method(), TradeConstants.PAY_MODE_ID_VISA)) {
            if (!TextUtils.isEmpty(info.getCard_valid_date())) {
                termOfValidity = "20" + info.getCard_valid_date().substring(0, 2) + "/" + info.getCard_valid_date().substring(2);

            }
            cardTypeText = getCardTypeText(info.getCard_type());
        }
        String outOrderNo = info.getOut_order_no();
        if (TextUtils.isEmpty(outOrderNo)) {
            outOrderNo = info.getOut_refund_no();
        }
        String origOrderNo = "";
        if (!TextUtils.isEmpty(info.getOrig_trans_no())) {
            origOrderNo = info.getOrig_trans_no();
        }
        String topTitle = TextUtils.equals(info.getApp_id(), "30c51f0c381662c3") ? "码付签购单" : "POS签购单";
        String bankName = BankUtil.getBankName(info.getCard_issuers_no());

        builder.addTopTitle(topTitle)
                .addStoreName(info.getStore_name())
                .addStoreNo(info.getStore_no())
                .addDeviceEn(info.getDevice_en())
                .addMerChantName(info.getMerchant_name())
                .addMerChantNumber(info.getMerchant_no())
                .addTerMinalNumber(info.getTerminal_no())
                .addTransactiontype(info.getTrans_type())
                .addPayType(info.getPayment_method())
                .addPayTypeText(getPayTypeText(info.getPayment_method()))
                .addTradeTypeText(getTradeTypeText(info.getPayment_method(), info.getTrans_type()))
                .addCardOrganization(bankName)
                .addAcquireeOrganization(bankName)
                .addCardNumber(PrintStringUtil.INSTANCE.hideBuyerAccount(info.getPay_user_account_id()))
                .addCardTypeText(cardTypeText)
                .addTermOfValidity(termOfValidity)
                .addBatchNumber(info.getBatch_no())
                .addVouncherNo(info.getVouch_no())
                .addTransactionReferenceNumber(refNo)
                .addPayTime(info.getTrans_end_time())
                .addNeedPayMoney(info.getTrans_amount())
                .addPlatformPMoney(AmountUtil.changeF2Y(Long.valueOf(discountMoney)))
                .addPayMoney(customPayAmount)
                .addPrintAgainHint(data.getPrinter_reprint())
                .addTransactionNumber(info.getTrans_no())
                .addAuthCode(info.getAuth_code())
                .addOutTransNo(outOrderNo)
                .addOrigTransNo(origOrderNo)
                .addBranchName(data.getBranch_name())
                .addBusinessAccount(PrintStringUtil.INSTANCE.hideBuyerAccount(info.getPay_user_account_id()))
                .addQrCodeContent(
                        data.getPrinter_qrcode() ? PrintQRCodeUtils.INSTANCE.getPrintTradeOrderQRCodeDataInfo(info.getTrans_no()) : "");
        if (!TextUtils.isEmpty(info.getSignature_picture())) {
            builder.addSignBitmap(BitmapUtil.INSTANCE.base64ToBitmap(info.getSignature_picture()));
        }
        String savePage = printNum == 1 ? "商户存根" : "消费者存根";
        if (data.getPrinter_reprint()) {
            savePage += "    ***重打印***";
        }

        builder.addPrintQrCode(data.getPrinter_qrcode());
        builder.addSavePage(savePage);
        builder.addDescription(info.getDescription());
        builder.addPrintCurPage(data.getPrinter_blank());
        builder.addPrintSign(data.getPrinter_signature());
        return builder.build();
    }

    /**
     * 结算统计但
     */
    private BillStatisticPrintBean creatPrintSettltData() {
        BillStatisticBuilder builder = new BillStatisticBuilder();
        SettlementData bean = (SettlementData) mData.getData();
        List<PrintSettleData> list = SettlementData.Companion.getSettleData(bean);
        for (int i = 0; i < list.size(); i++) {
            PrintSettleData itembean = list.get(i);
            //支付方式
            String payTypeName = PrintStringUtil.INSTANCE.getPayTypeName(itembean.getPay_mode_id());
            builder.addBillPayItemInfoLineMessageData("合计", itembean.getTradeCount() + "",
                    AmountUtil.changeF2Y(Long.valueOf(itembean.getAllAmount())));
            //消费-消费撤销-退货
            SettleSummaryObject consumebean = PrintStringUtil.INSTANCE.getSettleTypeData(TradeConstants.TRADE_TYPE_CONSUME, itembean);
            SettleSummaryObject consumeCancelbean = PrintStringUtil.INSTANCE.getSettleTypeData(TradeConstants.TRADE_TYPE_CONSUME_REVOCATION, itembean);
            SettleSummaryObject refundbean = PrintStringUtil.INSTANCE.getSettleTypeData(TradeConstants.TRADE_TYPE_RETURN_GOODS_OR_REFUND, itembean);
            builder.addBillPayItemInfoLineMessageData(payTypeName, "笔数", "金额");
            builder.addBillPayItemInfoLineMessageData("消费", consumebean.getSettle_num(), AmountUtil.changeF2Y(consumebean.getSettle_amount()));
            //银行卡和扫码要分开统计
            if (TextUtils.equals(itembean.getPay_mode_id(), TradeConstants.PAY_MODE_ID_BANK) ||
                    TextUtils.equals(itembean.getPay_mode_id(), TradeConstants.PAY_MODE_ID_VISA)) {
                if (!TextUtils.equals(consumeCancelbean.getSettle_amount(), "0") &&
                        !TextUtils.equals(consumeCancelbean.getSettle_num(), "0")) {
                    builder.addBillPayItemInfoLineMessageData("消费撤销", consumeCancelbean.getSettle_num(),
                            AmountUtil.changeF2Y(consumeCancelbean.getSettle_amount()));
                }
                if (!TextUtils.equals(refundbean.getSettle_amount(), "0") &&
                        !TextUtils.equals(refundbean.getSettle_num(), "0")) {
                    builder.addBillPayItemInfoLineMessageData("退货", refundbean.getSettle_num(),
                            AmountUtil.changeF2Y(refundbean.getSettle_amount()));
                }
            } else {
                int totalNum = Integer.valueOf(consumebean.getSettle_num()) +
                        Integer.valueOf(refundbean.getSettle_num());

                long totalMoney = Long.valueOf(refundbean.getSettle_amount()) +
                        Long.valueOf(refundbean.getSettle_amount());
                String amount = AmountUtil.changeF2Y(totalMoney);
                builder.addBillPayItemInfoLineMessageData("退款", "" + totalNum, amount);
            }
            builder.addBillInfoTypeFinishData(payTypeName);
        }
        String settleAmountStr = AmountUtil.changeF2Y(AmountUtil.changeY2FNoDMark(bean.getSettle_amount()) + "");
        String settleTime = TextUtils.isEmpty(bean.getCreate_time()) ? bean.getTrans_end_time() : bean.getCreate_time();
        //统计信息
        builder.addPayCount(bean.getSettle_num() + "")
                .addBatchNumber(bean.getBatch_settle_id() + "")
                .addPayMoney(settleAmountStr)
                .addSettleAccountTime(settleTime)
                .addStartTime(bean.getStart_time())
                .addEndTime(bean.getTrans_end_time())
                .addEnCode(bean.getDevice_en());
        //交易详情信息
        if (bean.is_print_detail()) {
            List<TransDetailObject> detailsList = bean.getTrans_detail();
            for (TransDetailObject b : detailsList) {
                String payType = b.getPayment_method();
                String tradeTime = b.getTrans_end_time();
                String tradeAmount = AmountUtil.changeF2Y(AmountUtil.changeY2FNoDMark(b.getTrans_amount()) + "");
                String tradeSdkNo = b.getTrans_no();
                String payTypeName = PrintStringUtil.INSTANCE.getPayTypeName(payType);

                if (!TextUtils.equals(payTypeName, PrintStringUtil.ACTION_PAY_MONEY_TXT) ||
                        !TextUtils.equals(payTypeName, PrintStringUtil.ACTION_PAY_VIP_TXT)) {
                    if (TextUtils.equals(b.getTrans_type(), PrintStringUtil.ORDER_STATE_CONSUME_TYPE)) {
                        payTypeName += PrintStringUtil.ACTION_PAY;
                        builder.addPayTransactionInfo(payTypeName, tradeTime, tradeAmount
                                , tradeAmount, tradeSdkNo);
                    } else {
                        payTypeName += PrintStringUtil.ORDER_STATE_REFUND;
                        builder.addRefundTransactionInfo(payTypeName, tradeTime, tradeAmount
                                , "退款成功", tradeSdkNo);
                    }
                }
            }
        }
        return builder.build();
    }


    /**
     * 获取支付方式文本
     */
    private String getPayTypeText(String payModeId) {
        return PrintStringUtil.INSTANCE.getPayTypeText(payModeId);
    }

    /**
     * 获取支付类型文本
     */
    private String getTradeTypeText(String payModeId, String tradeType) {
        return PrintStringUtil.INSTANCE.getTradeTypeText(payModeId, tradeType);
    }

    /**
     * 卡类型文本
     */
    private String getCardTypeText(String cardType) {
        String text;
        switch (cardType) {
            case "1":
                text = "借记卡";
                break;
            case "2":
                text = "贷记卡";
                break;
            default:
                text = "未知类型";
                break;
        }
        return text;
    }


    private void printChongZheng(Map<String, String> map) {

    }

}
