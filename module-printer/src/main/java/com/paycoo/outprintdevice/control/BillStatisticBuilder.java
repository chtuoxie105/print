package com.paycoo.outprintdevice.control;


import com.paycoo.outprintdevice.bean.BillStatisticPrintBean;

import java.util.ArrayList;
import java.util.List;

public class BillStatisticBuilder {
    BillStatisticPrintBean bean = new BillStatisticPrintBean();
    //------消费信息列表-----------
    List<BillStatisticPrintBean.BillInfoType> listBillInfo = new ArrayList<>();
    //-----总计---------
    BillStatisticPrintBean.BillInfo billInfo = new BillStatisticPrintBean.BillInfo();
    //-----------交易明细-----------
    List<BillStatisticPrintBean.TransactionInfo> listTransactionList = new ArrayList<>();

    public BillStatisticBuilder() {

    }

    /**
     * 添加支付宝、微信、银联卡整块消费信息(先使用 addBillPayItemInfoLineMessageData() 添加子类的项目)
     *
     */
    public BillStatisticBuilder addBillInfoTypeFinishData(String payName) {
        BillStatisticPrintBean.BillInfoType billInfoType = new BillStatisticPrintBean.BillInfoType();
//        billInfoType.setType(type);
        billInfoType.setPayName(payName);
        List<BillStatisticPrintBean.BillPayItem> l = new ArrayList<>();
        l.addAll(billPayItemList);
        billInfoType.setBillPayItemList(l);
        listBillInfo.add(billInfoType);
        billPayItemList.clear();
        return this;
    }

    List<BillStatisticPrintBean.BillPayItem> billPayItemList = new ArrayList<>();

    /**
     * 添加合计、消费、退款等其余一列的信息
     *
     * @param leftText
     * @param centerText
     * @param rightText
     */
    public BillStatisticBuilder addBillPayItemInfoLineMessageData(String leftText, String centerText, String rightText) {
        BillStatisticPrintBean.BillPayItem billPayItem = new BillStatisticPrintBean.BillPayItem();
        billPayItem.itemLeftTitleText = leftText;
        billPayItem.itemCenterPayCountText = centerText;
        billPayItem.itemRightPayMoneyText = rightText;
        billPayItemList.add(billPayItem);
        return this;
    }
    //----------总计信息------------

    /**
     * 交易笔数
     */
    public BillStatisticBuilder addPayCount(String count) {
        billInfo.transactionCount = count;
        return this;
    }

    /**
     * 结算批号
     */
    public BillStatisticBuilder addBatchNumber(String batchNumber) {
        billInfo.batchNumber = batchNumber;
        return this;
    }

    /**
     * 结算金额(125.25)
     */
    public BillStatisticBuilder addPayMoney(String money) {
        billInfo.billMoney = money;
        return this;
    }

    /**
     * 结算时间(2019-07-10 15:51:52)
     */
    public BillStatisticBuilder addSettleAccountTime(String settleAccountsTime) {
        billInfo.settleAccountsTime = settleAccountsTime;
        return this;
    }

    /**
     * 开始时间(2019-07-10 15:51:52)
     */
    public BillStatisticBuilder addStartTime(String startTime) {
        billInfo.startTime = startTime;
        return this;
    }

    /**
     * 结束时间(2019-07-10 15:51:52)
     */
    public BillStatisticBuilder addEndTime(String endTime) {
        billInfo.endTime = endTime;
        return this;
    }

    /**
     * 设备EN号
     */
    public BillStatisticBuilder addEnCode(String encode) {
        billInfo.enCode = encode;
        return this;
    }
    //-----------交易明细-----------

    /**
     * 添加支付信息
     *
     * @param payType           支付方式（银联卡支付）
     * @param payTime           支付时间（2019-07-10 15:51:52）
     * @param merChantGetMoney  商户应收金额（2558.36）
     * @param consumberPayMoney 用户实付金额（2558.36）
     * @param orderNumbre       收银订单号
     * @return
     */
    public BillStatisticBuilder addPayTransactionInfo(String payType, String payTime, String merChantGetMoney
            , String consumberPayMoney, String orderNumbre) {
        BillStatisticPrintBean.TransactionInfo transactionInfoBean = new BillStatisticPrintBean.TransactionInfo();
        transactionInfoBean.typeName = payType;
        transactionInfoBean.typeTime = payTime;
        transactionInfoBean.merChantGetMoney = merChantGetMoney;
        transactionInfoBean.consumerPayMoney = consumberPayMoney;
        transactionInfoBean.orderNumber = orderNumbre;
        listTransactionList.add(transactionInfoBean);
        return this;
    }

    /**
     * 添加重打印字样
     */
    public BillStatisticBuilder addPrintAgainHint(boolean isPrint) {
        bean.isShowPrintAgainHint = isPrint;
        return this;
    }

    /**
     * 添加退款信息
     *
     * @param refundName  退款方式（银联卡退款）
     * @param refundTime  退款时间 （2019-07-10 15:51:52）
     * @param refundMoney 退款金额（2558.36 ）
     * @param refundState 退款状态（退款中）
     * @param orderNumber 收银订单号
     * @return
     */
    public BillStatisticBuilder addRefundTransactionInfo(String refundName, String refundTime
            , String refundMoney, String refundState, String orderNumber) {
        BillStatisticPrintBean.TransactionInfo transactionInfoBean = new BillStatisticPrintBean.TransactionInfo();
        transactionInfoBean.typeName = refundName;
        transactionInfoBean.typeTime = refundTime;
        transactionInfoBean.refundMoney = "-" + refundMoney;
        transactionInfoBean.state = refundState;
        transactionInfoBean.orderNumber = orderNumber;
        listTransactionList.add(transactionInfoBean);
        return this;
    }

    /**
     * 获取打印 bean
     *
     * @return
     */
    public BillStatisticPrintBean build() {
        bean.listBillInfo = listBillInfo;
        bean.billInfo = billInfo;
        bean.listTransaction = listTransactionList;
        return bean;
    }

}
