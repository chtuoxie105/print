package com.paycoo.outprintdevice.control;

import android.graphics.Bitmap;
import com.paycoo.outprintdevice.bean.UserPayNormalBean;

public class UserPayNormalBuild {
    UserPayNormalBean bean = new UserPayNormalBean();

    public UserPayNormalBuild() {

    }

    /**
     * 单子顶部图片
     *
     * @param topLogoImg
     * @return
     */
    public UserPayNormalBuild addTopLogoImg(Bitmap topLogoImg) {
        bean.topLogoImg = topLogoImg;
        return this;
    }
    /**
     * 签购单标题\
     */
    public UserPayNormalBuild addTopTitle(String topTitle) {
        bean.topTitle = topTitle;
        return this;
    }


    /**
     * 单子标题（图片下面）
     */
    public UserPayNormalBuild addOrderTitle(String orderTitle) {
        bean.orderTitle = orderTitle;
        return this;
    }
    /**
     * 门店名
     */
    public UserPayNormalBuild addStoreName(String storeName) {
        bean.storeName = storeName;
        return this;
    }
    /**
     * 门店号
     */
    public UserPayNormalBuild addStoreNo(String storeNo) {
        bean.storeNo = storeNo;
        return this;
    }

    /**
     * 商户名
     */
    public UserPayNormalBuild addMerChantName(String merChantName) {
        bean.merChantName = merChantName;
        return this;
    }
    /**
     * deviceEn
     */
    public UserPayNormalBuild addDeviceEn(String deviceEn) {
        bean.deviceEn = deviceEn;
        return this;
    }


    /**
     * 商户号
     */
    public UserPayNormalBuild addMerChantNumber(String merChantNumber) {
        bean.merChantNumber = merChantNumber;
        return this;
    }

    /**
     * 终端号
     */
    public UserPayNormalBuild addTerMinalNumber(String terminalNumber) {
        bean.terminalNumber = terminalNumber;
        return this;
    }

    /**
     * 交易类型文本
     */
    public UserPayNormalBuild addTradeTypeText(String tradeTypeText) {
        bean.tradeTypeText = tradeTypeText;
        return this;
    }
    /**
     * 支付类型文本
     */
    public UserPayNormalBuild addPayTypeText(String payTypeText) {
        bean.payTypeText = payTypeText;
        return this;
    }

    /**
     * 交易类型
     */
    public UserPayNormalBuild addTransactiontype(String transactionType) {
        bean.transactionType = transactionType;
        return this;
    }

    /**
     * 支付类型
     */
    public UserPayNormalBuild addPayType(String payType) {
        bean.payType = payType;
        return this;
    }

    /**
     * 发卡机构
     */
    public UserPayNormalBuild addCardOrganization(String cardOrganzation) {
        bean.cardLssuer = cardOrganzation;
        return this;
    }

    /**
     * 收单机构
     */
    public UserPayNormalBuild addAcquireeOrganization(String acquirer) {
        bean.acquirer = acquirer;
        return this;
    }

    /**
     * 卡号
     */
    public UserPayNormalBuild addCardNumber(String cardNumber) {
        bean.cardNumber = cardNumber;
        return this;
    }
    /**
     * 卡类型
     */
    public UserPayNormalBuild addCardTypeText(String cardTypeText) {
        bean.cardTypeText = cardTypeText;
        return this;
    }


    /**
     * 有效期
     */
    public UserPayNormalBuild addTermOfValidity(String termOfValidity) {
        bean.termOfValidity = termOfValidity;
        return this;
    }

    /**
     * 批次号
     */
    public UserPayNormalBuild addBatchNumber(String batchNumber) {
        bean.batchNumber = batchNumber;
        return this;
    }

    /**
     * 凭证号
     */
    public UserPayNormalBuild addVouncherNo(String vouncherNo) {
        bean.vouncherNo = vouncherNo;
        return this;
    }

    /**
     * 交易参考号
     */
    public UserPayNormalBuild addTransactionReferenceNumber(String transactionReferenceNumber) {
        bean.transactionReferenceNumber = transactionReferenceNumber;
        return this;
    }

    /**
     * 授权码
     * @param authCode
     * @return
     */
    public UserPayNormalBuild addAuthCode(String authCode){
        bean.authCode =authCode;
        return this;
    }
    /**
     * 支付时间
     */
    public UserPayNormalBuild addPayTime(String payTime) {
        bean.payTime = payTime;
        return this;
    }

    /**
     * 应收金额
     *
     */
    public UserPayNormalBuild addNeedPayMoney(String receivableMoney) {
        bean.receivableMoney = receivableMoney;
        return this;
    }

    /**
     * 优惠金额、
     */
    public UserPayNormalBuild addPlatformPMoney(String platformMoney) {
        bean.platformMoney = platformMoney;
        return this;
    }

    /**
     * 实付金额
     */
    public UserPayNormalBuild addPayMoney(String payMoney) {
        bean.payMoney = payMoney;
        return this;
    }


    /**
     * 添加重打印字样
     */
    public UserPayNormalBuild addPrintAgainHint(boolean isPrint) {
        bean.isShowPrintAgainHint = isPrint;
        return this;
    }

    /**
     * 交易号
     */
    public UserPayNormalBuild addTransactionNumber(String payNumber) {
        bean.payNumber = payNumber;
        return this;
    }

    /**
     * 外部订单号
     */
    public UserPayNormalBuild addOutTransNo(String outOrderNo) {
        bean.out_order_no = outOrderNo;
        return this;
    }
    /**
     * 原交易订单号
     */
    public UserPayNormalBuild addOrigTransNo(String orig_trans_no) {
        bean.orig_trans_no = orig_trans_no;
        return this;
    }


    /**
     * 商户订单号
     *
     * @param merchantOrderNumer
     * @return
     */
    public UserPayNormalBuild addMerchantOrderNumber(String merchantOrderNumer) {
        bean.merchantOrderNumer = merchantOrderNumer;
        return this;
    }

    /**
     * 电子签名图片
     *
     * @param bmp
     * @return
     */
    public UserPayNormalBuild addSignBitmap(Bitmap bmp) {
        bean.signBitmap = bmp;
        return this;
    }


    /**
     * 设备EN号
     */
    public UserPayNormalBuild addEnCode(String encode) {
        bean.enCode = encode;
        return this;
    }

    /**
     * 设备mCode号
     */
    public UserPayNormalBuild addMCode(String mCode) {
        bean.mCode = mCode;
        return this;
    }

    /**
     * 设备设备版本号
     */
    public UserPayNormalBuild addVersionCode(String versionCode) {
        bean.versionCode = versionCode;
        return this;
    }

    /**
     * 商户账户号
     */
    public UserPayNormalBuild addBusinessAccount(String businessAccount) {
        bean.businessAccount = businessAccount;
        return this;
    }
    /**
     * 存根
     */
    public UserPayNormalBuild addSavePage(String savePage) {
        bean.savePage = savePage;
        return this;
    }


    /**
     * 是否二维码信息
     */
    public UserPayNormalBuild addPrintQrCode(boolean isPrintQrCode) {
        bean.isPrintQrCode = isPrintQrCode;
        return this;
    }
    /**
     * 二维码信息
     */
    public UserPayNormalBuild addQrCodeContent(String qrCodeContent) {
        bean.qrCodeContent = qrCodeContent;
        return this;
    }
    /**
     * 二维码信息
     */
    public UserPayNormalBuild addBranchName(String branchName) {
        bean.branchName = branchName;
        return this;
    }




    /**
     * 备注
     */
    public UserPayNormalBuild addDescription(String description) {
        bean.description = description;
        return this;
    }

    /**
     * 打印留白行数
     */
    public UserPayNormalBuild addPrintCurPage(int printCutPage) {
        bean.printCutPage = printCutPage;
        return this;
    }
    /**
     * 是否打印签名
     */
    public UserPayNormalBuild addPrintSign(boolean printSign) {
        bean.printSign = printSign;
        return this;
    }



    public UserPayNormalBean build() {
        return bean;
    }


}
