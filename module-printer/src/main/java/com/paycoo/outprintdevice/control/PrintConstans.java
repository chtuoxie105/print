package com.paycoo.outprintdevice.control;

public class PrintConstans {
    //默认端口号
    public static final int CONNECT_PORT = 9100;
    //打印Socket连接时间
    public static final int CONNECT_TIME_OUT = 3000;
    //-------支付类型------
    public static final int PAY_TYPE_ZFB = 0;//支付宝
    public static final int PAY_TYPE_WEIXIN = 1;//微信
    public static final int PAY_TYPE_BANK_CARD = 2;//银联
    public static final int PAY_TYPE_UNIONPAY_CODE = 3;//银联二维码
    //-----------交易类型----------
    public static final int TRANSACTION_TYPE_CONSUM = 0;//消费
    public static final int TRANSACTION_TYPE_RETURN_GOODS = 1;//银行卡：退货；微信、支付宝：退款
    public static final int TRANSACTION_TYPE_CONSUM_CANCEL = 2;//消费撤销

    //-------消费签名费提示-------
    public static final String PAY_MONEY_MORE_THAN_1000 = "消费者签名:";
    public static final String PAY_MONEY_NO_MORE_THAN_1000 = "交易金额未超过1000，无需签名";

    //-------确认交易，存入账户提示------
    public static final String CONFIRM_NO_ERROR_INTO_BUSINESS_ACCOUNT = "*本人确认以上交易，同意将其计入账户";

    //-------存根------------
    public static final String SAVE_PAGE_TO_BUSINESS = "商户存根";
    public static final String SAVE_PAGE_TO_USER = "消费者存根";
    //---------二维码扫描提示------
    public static final String PLAEASE_USE_POS_SCAN_QR_CODE_HINT = "请使用旺POS扫描此二维码";
    //----------重打印------
    public static final String PRINT_AGAIN_HINT = "重打印";
}
