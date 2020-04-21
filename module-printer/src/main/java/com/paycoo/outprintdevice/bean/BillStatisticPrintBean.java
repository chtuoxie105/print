package com.paycoo.outprintdevice.bean;


import com.paycoo.outprintdevice.control.PrintConstans;

import java.util.List;

/**
 * 账单统计（结算总记单）
 */
public class BillStatisticPrintBean {

    public List<BillInfoType> listBillInfo;//支付类型合计(合计/消费/退款)(注：会根据type 自动打印title(支付宝(type)--笔数--金额))
    public BillInfo billInfo;//支付总计信息
    public List<TransactionInfo> listTransaction;//交易明细
    public boolean isShowPrintAgainHint = false;//是否显示重打印字样提示
    public String printAgainHint = PrintConstans.PRINT_AGAIN_HINT;//重打印提示

    /**
     * 交易类型
     */
    public static class BillPayItem {
        public String itemLeftTitleText;//合计
        public String itemCenterPayCountText;//笔数
        public String itemRightPayMoneyText;//金额
    }

    public static class BillInfoType {
        /**
         * 默认银联
         */
        int type = PrintConstans.PAY_TYPE_BANK_CARD;
        String payName = "";
        List<BillPayItem> billPayItemList;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getPayName() {
            return payName;
        }

        public void setPayName(String payName) {
            this.payName = payName;
        }

        public List<BillPayItem> getBillPayItemList() {
            return billPayItemList;
        }

        public void setBillPayItemList(List<BillPayItem> billPayItemList) {
            this.billPayItemList = billPayItemList;
        }
    }

    /**
     * 总计（账单信息）
     */
    public static class BillInfo {
        public String title = "总计";
        public String transactionCount;//交易笔数
        public String batchNumber;//结算批次号
        public String billMoney;//账单金额
        public String settleAccountsTime;//结算时间
        public String startTime;//开始时间
        public String endTime;//结束时间
        public String enCode;//设备EN号
        public String mark = "注：以上统计仅供参考，资金以实际到账为准";
    }

    /**
     * 交易明细
     */
    public static class TransactionInfo {
        public String typeName;//交易类型名字
        public String typeTime;//交易时间
        //---------退款订单信息-----
        public String refundMoney;//退款金额
        public String state;//交易状态
        //--------支付订单信息-----
        public String merChantGetMoney;//商户应收
        public String consumerPayMoney;//用户实付
        public String orderNumber;//收银订单号
    }

}
