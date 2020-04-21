package com.paycoo.outprintdevice.bean;

import android.graphics.Bitmap;
import com.paycoo.outprintdevice.control.PrintConstans;

/**
 * 普通消费打印单
 */
public class UserPayNormalBean {
    public int orderType;//本次打印单类型(消费/退货/消费撤销)
    public String orderTitle = PrintConstans.SAVE_PAGE_TO_BUSINESS;
    public String topTitle;//签购单顶部标题
    public String storeName;//门店名
    public String storeNo;//门店号
    public String merChantName;//商户名
    public String merChantNumber;//商户号
    public String terminalNumber;//终端号
    public String deviceEn;

    public String tradeTypeText;//交易类型文本
    public String payTypeText;//支付类型文本

    public String transactionType;//交易类型
    public String payType;//支付方式
    public String cardLssuer;//发卡机构
    public String acquirer;//收单机构

    public String cardNumber;//卡号
    public String cardTypeText;//银行卡类型文本
    public String termOfValidity;//有效期
    public String batchNumber;//批次号
    public String vouncherNo;//凭证号
    public String authCode;//授权码
    public String transactionReferenceNumber;//交易参考号

    public String payTime;//日期/时间

    public String receivableMoney;//应收金额(交易金额)
    public String platformMoney;//优惠金额
    public String payMoney;//实付金额

    public String merchantOrderNumer;//商户订单号
    public String payNumber;//交易号
    public String out_order_no;//外部订单号
    public String orig_trans_no;//原交易号

    public String enCode;//设备EN号
    public String mCode;//设备mcode号
    public String versionCode;//设备版本号
    public boolean isShowPrintAgainHint = false;//是否显示重打印字样提示
    public String printAgainHint = PrintConstans.PRINT_AGAIN_HINT;//重打印提示

    public String businessAccount;//付款账号

    public String isMoreThan1000Hint;//是否超过1000提示
    public Bitmap signBitmap;//电子签名图片
    public String confirmNoErrortext = PrintConstans.CONFIRM_NO_ERROR_INTO_BUSINESS_ACCOUNT;//确认无误存入账户label
    public String savePage = PrintConstans.SAVE_PAGE_TO_BUSINESS;//存根

    public String scanQrCodeHint = PrintConstans.PLAEASE_USE_POS_SCAN_QR_CODE_HINT;
    public Bitmap topLogoImg;//顶部logo图
    public boolean isPrintQrCode = true;//是否打印二维码
    public String qrCodeContent;//打印的二维码内容
    public boolean printSign = true;
    public String branchName = "";//分支名

    public String description;//备注
    public int printCutPage = 4;//打印留白行数


}
