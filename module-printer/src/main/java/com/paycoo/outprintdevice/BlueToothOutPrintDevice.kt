package com.paycoo.outprintdevice

import android.text.TextUtils
import android.util.Log
import com.paycoo.cashier.basis.data.*
import com.paycoo.cashier.basis.device.listener.DevicePrinterListener
import com.paycoo.cashier.basis.device.listener.OnOutPrintDeviceConnectStatusListener
import com.paycoo.cashier.basis.device.outprint.AbstractOutPrintDevice
import com.paycoo.cashier.basis.trade.PrintStringUtil
import com.paycoo.cashier.basis.trade.PrintStringUtil.getSettleTypeData
import com.paycoo.cashier.basis.trade.TradeConstants
import com.paycoo.cashier.basis.utils.AmountUtil
import com.paycoo.cashier.basis.utils.BitmapUtil
import com.paycoo.cashier.basis.zxing.XQRCode
import com.paycoo.outprintdevice.bluetooth.connect.BlueToothPrintService
import com.paycoo.outprintdevice.bluetooth.util.GPrinterCommand
import com.paycoo.outprintdevice.bluetooth.util.PrintPic
import com.paycoo.outprintdevice.bluetooth.util.PrinterWriter
import com.paycoo.outprintdevice.bluetooth.util.PrinterWriter58mm

class BlueToothOutPrintDevice : AbstractOutPrintDevice() {
    private var mData: PrinterData<BaseData>? = null
    private var printerCouple = 1
    private var mPrintDevice: BlueToothPrintService? = null
    override fun onCheckPrintConnectStatus(device: OutPrintDeviceBean, l: OnOutPrintDeviceConnectStatusListener) {
        l.onConnectStatusCheckResult(device, false)
    }

    override fun print(device: OutPrintDeviceBean, data: PrinterData<BaseData>, listenerDevice: DevicePrinterListener) {
        mPrintDevice = BlueToothPrintService.instance
        mPrintDevice!!.mPrintListener = object : DevicePrinterListener {
            override fun onPrintSuccess() {
                if (printerCouple < mData!!.printer_couple) {
                    if (mData!!.printer_type === PrinterData.PrintType.TRADE_TYPE) {
                        val info = mData!!.data as TradeRecordInfo
                        if (info.isNeedWaitPrint) {
                            listenerDevice.onPrintTradeMerChantSuccess()
                        } else {
                            startPrintTradeConsume()
                        }
                    }
                } else {
                    Log.e("11", "onPrintTradeMerChantSuccess>>>>onPrintSuccess")
//                    mData = null
                    listenerDevice.onPrintSuccess()
                }
            }

            override fun onPrintFail(errorMsg: String) {
                listenerDevice.onPrintFail(errorMsg)
            }

            override fun onPrintTradeMerChantSuccess() {
                Log.e(
                    "11",
                    "onPrintTradeMerChantSuccess>>>>:printerCouple>>>" + printerCouple + "==printer_couple==" + mData!!.printer_couple
                )
            }
        }
        mData = data
        printerCouple = 1
        if (data.printer_type == PrinterData.PrintType.TRADE_TYPE) {
            printTrade()
        } else if (data.printer_type == PrinterData.PrintType.SETTLEMENT_TYPE) {
            printSettle()
        }else{
            listenerDevice.onPrintFail("不支持的打印数据")
        }
    }

    override fun printTest(device: OutPrintDeviceBean, data: String, listenerDevice: DevicePrinterListener) {
        mPrintDevice = BlueToothPrintService.instance
        mPrintDevice!!.mPrintListener = listenerDevice
        mPrintDevice!!.addPrintData(getPrintTestData())
        mPrintDevice!!.startPrint()
        mData = null
    }

    override fun startPrintTradeConsume() {
        ++printerCouple
        Log.e("11", "startPrintTradeConsume>>>>printerCouple：$printerCouple")
        printTrade()
    }

    private fun printTrade() {
        onPrintWrite58mmPageData()
    }

    private fun printSettle() {
        val settleBeanInfo: SettlementData = mData!!.data as SettlementData
        val data = SettlementData.getSettleData(settleBeanInfo)
        val printer = PrinterWriter58mm(PrinterWriter58mm.TYPE_58, PrinterWriter.HEIGHT_PARTING_DEFAULT)
        printer.init()
        printer.setFontSize(0)
        printSummary(data, settleBeanInfo, printer)
        if (settleBeanInfo.is_print_detail) {
            mPrintDevice?.addPrintData(printer.dataAndReset)
            // 打印结算明细
            printSettlementDetails(settleBeanInfo,printer)
        }
        printer.checkStatus()
        mPrintDevice?.addPrintData(printer.dataAndClose)
        mPrintDevice?.startPrint()
    }

    private fun getPrintTestData(): ByteArray {
        val printer = PrinterWriter58mm(PrinterWriter58mm.TYPE_58, PrinterWriter.HEIGHT_PARTING_DEFAULT)
        printer.setAlignCenter()
        printer.init()
        printer.setFontSize(0)
        printer.print("测试打印,测试打印,测试打印")
        printer.print("\n\n\n")
        return printer.dataAndClose
    }

    private fun onPrintWrite58mmPageData(): PrinterWriter {
        val info: TradeRecordInfo = mData!!.data as TradeRecordInfo

        val printer = PrinterWriter58mm(PrinterWriter58mm.TYPE_58, PrinterWriter.HEIGHT_PARTING_DEFAULT)
        printer.init()
        printer.setAlignCenter()
        printer.setEmphasizedOn()
        printer.setFontSize(0)
        if (!TextUtils.isEmpty(info.app_id) && info.app_id == "30c51f0c381662c3") {
            printer.print("码付签购单")
        } else {
            printer.print("签购单")
        }
        printer.printLineFeed()
        printer.setEmphasizedOff()

        printLine(printer)
        if (!TextUtils.isEmpty(info.merchant_name)) {
            print1TextLine(printer, "商户名:" + info.merchant_name)
        }
        if (!TextUtils.isEmpty(info.merchant_name)) {
            print1TextLine(printer, "门店名:" + info.store_name)
        }
        printLine(printer)
        print1TextLine(printer, "交易时间:${info.trans_end_time}")
        print1TextLine(printer, "交易类型")
        print1TextLine(printer, info.trade_type_text + "/" + info.pay_type_text, 0, 1)
        if (!TextUtils.isEmpty(info.trans_amount) && info.trans_amount.toDouble() > 0) {
            if (TextUtils.equals(info.trans_type, TradeConstants.TRADE_TYPE_CONSUME)) {
                print1TextLine(printer, "交易金额:")
            } else {
                print1TextLine(printer, "退款金额:")
            }
            print1TextLine(printer, "RMB " + AmountUtil.formatStringYDouble2Potin(info.trans_amount), 0, 1)
        }
        printer.setFontSize(0)
        if (TextUtils.equals(info.trans_type, TradeConstants.TRADE_TYPE_CONSUME)) {
            var discountMoney = 0
            if (!TextUtils.isEmpty(info.discount_bpc)) {
                discountMoney = AmountUtil.changeY2FNoDMark(info.discount_bpc)
            }
            if (!TextUtils.isEmpty(info.discount_bmopc)) {
                discountMoney += AmountUtil.changeY2FNoDMark(info.discount_bmopc)
            }
            if (discountMoney > 0) {
                print1TextLine(printer, "优惠金额：RMB ${AmountUtil.changeF2Y(discountMoney.toLong())}")
            }
            var customerPaidAmount = info.trans_amount
            if (discountMoney > 0) {
                customerPaidAmount =
                    AmountUtil.changeF2Y((AmountUtil.changeY2FNoDMark(info.trans_amount) - discountMoney).toLong())
            }
            print1TextLine(printer, "实付金额：RMB ${AmountUtil.formatStringYDouble2Potin(customerPaidAmount)}")
        }
        printLine(printer)
        if (!TextUtils.isEmpty(info.pay_user_account_id)) {
            print1TextLine(printer, "付款账户:" + PrintStringUtil.hideBuyerAccount(info.pay_user_account_id))
        }
        print1TextLine(printer, "交易号:")
        print1TextLine(printer, info.trans_no)

        if (!TextUtils.isEmpty(info.orig_trans_no) && !TextUtils.equals(info.orig_trans_no, info.trans_no)) {
            print1TextLine(printer, "原交易号:")
            print1TextLine(printer, info.orig_trans_no)
        }
        var refNo = info.ref_no
        if (TextUtils.isEmpty(refNo)) {
            refNo = info.pay_platform_trans_no
        }

        if (!TextUtils.isEmpty(refNo)) {
            print1TextLine(printer, "参考号:")
            print1TextLine(printer, refNo)
        }

        if (!TextUtils.isEmpty(info.out_order_no)) {
            print1TextLine(printer, "外部订单号:")
            print1TextLine(printer, info.out_order_no)
        }
        if (!TextUtils.isEmpty(info.description)) {
            print1TextLine(printer, "备注:")
            print1TextLine(printer, info.description)
        }


        mPrintDevice?.addPrintData(printer.dataAndReset)
        val bmpPrit = PrintPic.getInstance()
        if (mData!!.printer_signature) {
            if (1 == printerCouple) {
                printLine(printer)
                if (TextUtils.isEmpty(info.signature_picture)) {
                    print1TextLine(printer, "顾客签名:\n\n\n")
                } else {
                    print1TextLine(printer, "顾客签名:")
                    mPrintDevice?.addPrintData(printer.dataAndReset)

                    printBase64BitMap(bmpPrit, info.signature_picture)
                }
            }
        }

        printLine(printer)
        print1TextLine(printer, "*本人确认以上交易，同意将其计入账户")
        var deviceInfoStr = info.store_no
        if (!TextUtils.isEmpty(info.device_en)) {
            deviceInfoStr += if (TextUtils.isEmpty(deviceInfoStr)) info.device_en else "_${info.device_en}"
        }
        if (!TextUtils.isEmpty(info.merchant_no)) {
            deviceInfoStr += "(" + info.merchant_no + ")"
        }
        if (!TextUtils.isEmpty(deviceInfoStr)) {
            print1TextLine(printer, deviceInfoStr)
        }
        print1TextLine(printer, "csp+/" + mData!!.branch_name + "/${info.app_version}")
        printLine(printer)
        if (printerCouple == 1) {
            val comments = if (mData!!.printer_reprint) "商户存根   ***重打印***" else "商户存根"
            print1TextLine(printer, comments)
        } else if (printerCouple == 2) {
            val comments = if (mData!!.printer_reprint) "消费者存根    ***重打印***" else "消费者存根"
            print1TextLine(printer, comments)
        }
        printLine(printer)
        if (mData!!.printer_qrcode) {
            mPrintDevice?.addPrintData(printer.dataAndReset)

            printQrCode(bmpPrit, mData!!.printqrcode_msg)
            printer?.init()
            printer.setAlignCenter()
            print1TextLine(printer, "请使用旺POS扫描此二维码")
        }
        print1TextLine(printer, "\n\n\n")
        printer.feedPaperCutPartial()
        printer.checkStatus()
        mPrintDevice?.addPrintData(printer.dataAndClose)
        mPrintDevice?.startPrint()
        return printer
    }

    /**
     * 结算统计但
     */
    private fun printSummary(
        data: List<PrintSettleData>,
        info: SettlementData, printer: PrinterWriter
    ) {
        printer.setAlignCenter()
        printer.setEmphasizedOn()
        printer.print("结算总计单")
        printer.printLineFeed()
        printer.setEmphasizedOff()
        mPrintDevice?.addPrintData(printer.dataAndReset)
        printLine(printer)
        for (i in 0 until data.size) {
            val detail = data[i]
            val type = PrintStringUtil.getPayTypeName(detail.pay_mode_id)
            print3TextLine(printer, type, "笔数", "金额")
            print3TextLine(
                printer,
                "合计",
                detail.tradeCount.toString(),
                AmountUtil.changeF2Y(detail.allAmount.toString())
            )

            val itemBean = getSettleTypeData("1", detail)
            if (itemBean != null) {
                print3TextLine(printer, "消费", itemBean.settle_num, AmountUtil.changeF2Y(itemBean.settle_amount))
            }
            if (detail.pay_mode_id == TradeConstants.PAY_MODE_ID_WX || detail.pay_mode_id == TradeConstants.PAY_MODE_ID_ALIPAY || detail.pay_mode_id == TradeConstants.PAY_MODE_ID_UNIONPAY_CODE
            ) {
                val refundBean = getSettleTypeData("3", detail)
                val consumeCancelBean = getSettleTypeData("2", detail)

                var orderNumber = 0
                var orderAmount = 0
                if (null != refundBean) {
                    orderNumber = refundBean.settle_num.toInt()
                    orderAmount = refundBean.settle_amount.toInt()
                }
                if (null != consumeCancelBean) {
                    orderNumber += consumeCancelBean.settle_num.toInt()
                    orderAmount += consumeCancelBean.settle_amount.toInt()
                }
                print3TextLine(printer, "退款", orderNumber.toString(), AmountUtil.changeF2Y(orderAmount.toString()))
            } else {
                val itemBean = getSettleTypeData("2", detail)
                if (itemBean != null) {
                    print3TextLine(printer, "消费撤销", itemBean.settle_num, AmountUtil.changeF2Y(itemBean.settle_amount))
                }
            }
            if (detail.pay_mode_id == TradeConstants.PAY_MODE_ID_BANK || detail.pay_mode_id == TradeConstants.PAY_MODE_ID_VISA) {
                val itemBean = getSettleTypeData("3", detail)
                if (itemBean != null) {
                    print3TextLine(printer, "退货", itemBean.settle_num, getAmount(itemBean.settle_amount))
                }
            }
            printLine(printer)
            mPrintDevice?.addPrintData(printer.dataAndReset)
        }

        printer.setAlignCenter()
        printer.setEmphasizedOn()
        printer.print("总计")
        printer.printLineFeed()
        printer.setEmphasizedOff()

        print1TextLine(printer, "交易笔数：${info.settle_num}")
        print1TextLine(printer, "结算批次号：${info.batch_settle_id}")
        val settleAmountStr = AmountUtil.changeF2Y(AmountUtil.changeY2FNoDMark(info.settle_amount).toString())
        print1TextLine(printer, "结算金额：$settleAmountStr")
        val settleTime = if (TextUtils.isEmpty(info.create_time)) {
            info.trans_end_time
        } else {
            info.create_time
        }
        print1TextLine(printer, "结算时间：$settleTime")
        print1TextLine(printer, "开始时间：${info.start_time}")
        print1TextLine(printer, "结束时间：${info.trans_end_time}")
        print1TextLine(printer, "设备EN号：${info.device_en}")
        print1TextLine(printer, "注：以上统计仅供参考，资金以实际到账为准")
        print1TextLine(printer, "\n\n\n")
        printer.feedPaperCutPartial()
    }

    /**
     * 结算详情
     */

    private fun printSettlementDetails(info: SettlementData, printer: PrinterWriter) {
        val list = info.trans_detail
        if (list.size > 0) {
            printer.setAlignCenter()
            printer.setEmphasizedOn()
            printer.print("交易明细")
            printer.printLineFeed()
            printer.setEmphasizedOff()
            mPrintDevice?.addPrintData(printer.dataAndReset)
            for (i in list.indices) {
                val info: TransDetailObject = list[i]
                printLine(printer)
                val payType = info.payment_method
                val tradeTime = info.trans_end_time
                val tradeAmount = AmountUtil.changeF2Y(AmountUtil.changeY2FNoDMark(info.trans_amount).toString())
                val tradeSdkNo = info.trans_no
                var type = PrintStringUtil.getPayTypeName(payType)

                var payTypeName = PrintStringUtil.getPayTypeName(payType)
                if (!TextUtils.equals(payTypeName, PrintStringUtil.ACTION_PAY_MONEY_TXT) || !TextUtils.equals(
                        payTypeName,
                        PrintStringUtil.ACTION_PAY_VIP_TXT
                    )
                ) {
                    if (TextUtils.equals(
                            info.trans_type,
                            PrintStringUtil.ORDER_STATE_CONSUME_TYPE
                        )
                    ) {
                        payTypeName += PrintStringUtil.ACTION_PAY
                        print2TextLine(printer, type, tradeTime)
                        print2TextLine(printer, "商户应收", tradeAmount)
                        print2TextLine(printer, "用户实付", tradeAmount)
                    } else {
                        type += "退款"
                        print2TextLine(printer, type, tradeTime)
                        print2TextLine(printer, "退款金额", "-$tradeAmount")
                        print2TextLine(printer, "退款状态", "退款成功")
                    }
                    print1TextLine(printer, "收银订单号：$tradeSdkNo")
                }
                mPrintDevice?.addPrintData(printer.dataAndReset)
            }
            printLine(printer)
        }
        print1TextLine(printer, "\n\n\n")
        printer.feedPaperCutPartial()
    }

    private fun getAmount(amount: String): String {
        return if (amount == "0") {
            AmountUtil.changeF2Y(amount)
        } else "-" + AmountUtil.changeF2Y(amount)
    }

    private fun print1TextLine(printer: PrinterWriter, msg: String, align: Int = 0, fontSize: Int = 0) {
        printer.setFontSize(fontSize)
        when (align) {
            1 -> printer.setAlignCenter()
            2 -> printer.setAlignRight()
            else -> printer.setAlignLeft()

        }
        printer.print(msg)
        printer.printLineFeed()
    }

    private fun print2TextLine(printer: PrinterWriter, msg1: String, msg2: String) {
        printer.printInOneLine(msg1, msg2, 0)
        printer.printLineFeed()
    }

    private fun print3TextLine(printer: PrinterWriter, msg1: String, msg2: String, msg3: String) {
        printer.printInOneLine(msg1, msg2, msg3, 0)
        printer.printLineFeed()
    }

    private fun printLine(printer: PrinterWriter) {
        printer.printLine()
        printer.printLineFeed()
    }

    private fun printBase64BitMap(printer: PrintPic, qrCodeMsg: String) {
        val bmp = BitmapUtil.base64ToBitmap(qrCodeMsg)
        printer.init(bmp)
        if (!bmp.isRecycled) {
            bmp.recycle()
        }
        printBitmapData(printer.printDraw())
    }

    private fun printQrCode(printer: PrintPic, qrCodeMsg: String) {
        val bmp = XQRCode.createQRCodeWithLogo(qrCodeMsg, 300, 300, null)
        printer.init(bmp)
        if (!bmp.isRecycled) {
            bmp.recycle()
        }
        printBitmapData(printer.printDraw())
    }

    private fun printBitmapData(byteArray: ByteArray) {
        mPrintDevice?.addPrintData(GPrinterCommand.reset)
        mPrintDevice?.addPrintData(GPrinterCommand.center)
        mPrintDevice?.addPrintData(GPrinterCommand.print)
        mPrintDevice?.addPrintData(byteArray)
    }
}