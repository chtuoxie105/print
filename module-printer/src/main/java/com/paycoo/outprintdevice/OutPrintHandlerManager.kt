package com.paycoo.outprintdevice

import com.paycoo.cashier.basis.data.BaseData
import com.paycoo.cashier.basis.data.OutPrintDeviceBean
import com.paycoo.cashier.basis.data.PrinterData
import com.paycoo.cashier.basis.device.listener.DevicePrinterListener
import com.paycoo.cashier.basis.device.listener.OnOutPrintDeviceConnectStatusListener
import com.paycoo.cashier.basis.device.outprint.AbstractOutPrintDevice
import com.paycoo.cashier.basis.device.outprint.IOutPrintDevice
import com.paycoo.cashier.basis.device.outprint.OutPrintDeviceType

/**
 * 外设打印分发
 */
class OutPrintHandlerManager : AbstractOutPrintDevice() {


    private var mWifiDevice: WifiOutPrintDevice? = null
    private var mBlueToothDevice: BlueToothOutPrintDevice? = null


    private fun getPrintDevice(type: String): IOutPrintDevice {
        return when (type) {
            OutPrintDeviceType.ACTION_OUT_PRINT_TYPE_BLUETOOTH -> {
                if (null == mBlueToothDevice) {
                    mBlueToothDevice = BlueToothOutPrintDevice()
                }
                mBlueToothDevice!!
            }
            else -> {
                if (null == mWifiDevice) {
                    mWifiDevice = WifiOutPrintDevice()
                }
                mWifiDevice!!
            }
        }
    }

    override fun onCheckPrintConnectStatus(device: OutPrintDeviceBean, l: OnOutPrintDeviceConnectStatusListener) {
        getPrintDevice(device.deviceType).onCheckPrintConnectStatus(device, l)
    }

    override fun print(device: OutPrintDeviceBean, data: PrinterData<BaseData>, listenerDevice: DevicePrinterListener) {
        mLastPrintDevice = getPrintDevice(device.deviceType)
        mLastPrintDevice!!.print(device, data, listenerDevice)
    }

    override fun printTest(device: OutPrintDeviceBean, data: String, listenerDevice: DevicePrinterListener) {
        getPrintDevice(device.deviceType).printTest(device, data, listenerDevice)
    }

    var mLastPrintDevice: IOutPrintDevice? = null
    override fun startPrintTradeConsume() {
        if (null != mLastPrintDevice) {
            mLastPrintDevice!!.startPrintTradeConsume()
        }
    }
}