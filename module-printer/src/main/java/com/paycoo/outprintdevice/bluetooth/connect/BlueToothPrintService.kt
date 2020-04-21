package com.paycoo.outprintdevice.bluetooth.connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.paycoo.cashier.basis.device.listener.DevicePrinterListener
import com.paycoo.cashier.basis.storage.PrintDeviceDataManager
import com.paycoo.cashier.basis.utils.ToastUtil
import com.paycoo.cashier.basis.utils.log.LogUtil
import com.paycoo.outprintdevice.bluetooth.util.PrinterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and

class BlueToothPrintService : Handler.Callback {
    companion object {
        //当前连接状态
        const val BLUE_STATE_NONE = 0
        const val BLUE_STATE_CONNECTING = 1
        const val BLUE_STATE_CONNECTED = 2
        //数据发送完成后，询问打印机状态最大次数，一次500毫秒;共计10秒;
        const val QUERY_BACK_MAX_NUMBER = 20
        private const val TAG = "蓝牙打印:"
        val instance: BlueToothPrintService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BlueToothPrintService()
        }
    }

    private var mState: Int = BLUE_STATE_NONE

    private val MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB")
    private var mSocket: BluetoothSocket? = null
    private var mInStream: InputStream? = null
    private var mOutStream: OutputStream? = null
    private var mHander: Handler? = null
    private var mHanderThread = HandlerThread("blue_tooth")

    private var mQueue: ArrayList<ByteArray> = ArrayList()
    private var mDevice: BluetoothDevice? = null
    var mPrintListener: DevicePrinterListener? = null
    private var isStartReadeCallBack = false
    private var mReadeThread: ReadDataThread? = null

    constructor() {
        mHanderThread.start()
        mHander = Handler(mHanderThread.looper, this)
        val bean = PrintDeviceDataManager.getBlueToothDevice()

        if (null != bean && !TextUtils.isEmpty(bean.httpIp)) {
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(bean.httpIp)
        }
        sendMsg(0, 0)
    }

    @Synchronized
    fun addPrintData(bytes: ByteArray?) {
        if (null == mDevice) {
            LogUtil.i(TAG + "请配置蓝牙打印设备")
            onPrintFail("请配置蓝牙打印设备")
            return
        }
        if (TextUtils.isEmpty(mDevice!!.address)) {
            LogUtil.i(TAG + "当前蓝牙设备地址错误")
            onPrintFail("当前蓝牙设备地址错误")
            return
        }

        if (null == mQueue) {
            mQueue = java.util.ArrayList()
        }
        if (null != bytes) {
            mQueue.add(bytes)
        }

    }

    fun startPrint() {
        if (mState == BLUE_STATE_CONNECTED) {
            mInStream = null
            sendMsg(1)
        } else {
            ToastUtil.showShortToast("正在连接蓝牙设备")
            sendMsg(0)
        }
    }


    override fun handleMessage(msg: Message?): Boolean {
        if (null == msg) {
            return false
        }
        when (msg.what) {
            0 -> connect()
            1 -> print()
            2 -> readBackCode()
        }

        return true
    }

    private fun sendMsg(what: Int, time: Long = 0) {
        val msg = mHander?.obtainMessage()
        msg?.what = what
        mHander?.sendMessageDelayed(msg, time)

    }

    private fun connect() {
        LogUtil.i(TAG + "当前状态>>>:" + mState)
        if (mState == BLUE_STATE_CONNECTED) {
            LogUtil.i(TAG + "当前处于连接状态>>发送打印消息>>>:")
            sendMsg(1)
            return
        }
        LogUtil.i(TAG + "请求连接蓝牙>>>>>" + mDevice?.address)
        try {
            mState = BLUE_STATE_NONE
            mSocket = mDevice?.createInsecureRfcommSocketToServiceRecord(MY_UUID)
            mState = BLUE_STATE_CONNECTING
            if (!mSocket!!.isConnected) {
                mSocket?.connect()
            }

            if (null == mInStream || null == mOutStream) {
                mInStream = mSocket?.inputStream
                mOutStream = mSocket?.outputStream
            }
            if (null == mReadeThread) {
                mReadeThread = ReadDataThread(mInStream!!)
            }
            mState = BLUE_STATE_CONNECTED
            LogUtil.i(TAG + "蓝牙连接成功>>>>>，发起打印检测")
            sendMsg(1)
        } catch (e: Exception) {
            isStartReadeCallBack = false
            e.printStackTrace()
            LogUtil.i(TAG + "蓝牙连接失败>>>>>:重置当前状态:" + e.message)
            mState = BLUE_STATE_NONE
            onPrintFail("蓝牙连接失败")
        }
    }

    private fun print() {
        LogUtil.i(TAG + "发起打印，传输打印数据")
        mInStream = mSocket?.inputStream
        while (mQueue.size > 0) {
            write(mQueue.removeAt(0))
        }
        mReadeThread?.start()
        sendMsg(2,500)
    }

    private var mQueryNumber = 0
    /**
     * 读取打印机状态
     */
    private fun readBackCode() {
        if(null ==mPrintListener){
            Log.e("11",TAG + "已回收回调>>>>>>>>中止")
            return
        }
        val code = mReadeThread?.getReadBackCode()
        LogUtil.i(TAG +  "本次轮询读取的状态>>>:$code"+"数据大小>>>:"+mQueue.size)
        if (mQueue.size > 0) {
            sendMsg(2)
        } else {
            if (mQueryNumber >= QUERY_BACK_MAX_NUMBER) {
                mQueryNumber=0
                onPrintFail("检查打印机")
                return
            }
            if (code == -3 || code == -2) {
                Log.e("11", "发起查询>>>>:$mQueryNumber")
                mReadeThread?.start()
                if(code==-2) {
                    ++mQueryNumber
                }
                sendMsg(2, 500)
            } else {
                if (code == 18) {
                    onPrintSuccess()
                } else {
                    onPrintFail("检查打印机")
                }
                mQueryNumber=0
            }
        }
    }


    private fun write(buffer: ByteArray) {
        if (mOutStream == null) {
            onPrintFail("打印机连接失败")
            LogUtil.i(TAG + "打印机连接失败,开启重连")
            mState = BLUE_STATE_NONE
            sendMsg(0)
            return
        }
        LogUtil.i(TAG + "发送数据")
        isStartReadeCallBack = true
        try {
            mOutStream?.write(buffer)
        } catch (e: Exception) {
            mState = BLUE_STATE_NONE
            mOutStream = null
            isStartReadeCallBack = false
            e.printStackTrace()
            onPrintFail("数据发送失败")
            LogUtil.i(TAG + "发送发生异常:" + e.message)
        }
    }


    fun closeSocket() {
        try {
            mSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun onPrintSuccess() {
        isStartReadeCallBack = false
        mReadeThread?.resetBackCode()
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            mPrintListener?.onPrintSuccess()
        }
    }

    private fun onPrintFail(errorMsg: String) {
        isStartReadeCallBack = false
        mReadeThread?.resetBackCode()
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            mPrintListener?.onPrintFail(errorMsg)
            cleanPrintListener()
        }
    }

    private fun cleanPrintListener() {
        mPrintListener = null
        mQueue = ArrayList()
    }

    inner class ReadDataThread : Thread {
        private var mReade: InputStream? = null
        //返回code：-3:初始状态;-2:正在读取中；-1:读取异常;18:打印成功;其他：失败
        private var mReadCode = -3
        private var isStopQuery =true

        constructor(input: InputStream) : super() {
            mReade = input
        }

        override fun run() {
            Log.e("11", "线程>>>开启读取数据======,当前code:$mReadCode")
            //上次读取的状态
            while (true) {
                if (null == mReade) {
                    break
                }
                if(!isStopQuery){
                    Log.e("11","线程>>>正在读取中====中断")
                    return
                }
                try {
                    isStopQuery =false
                    mReadCode = -2
                    mOutStream?.write(PrinterUtils.checkStatus())
                    val bytes = mReade!!.read()
                    Log.e("11", "线程>>>读取数据======:$bytes")
                    isStopQuery =true
                    mReadCode = bytes
                    if (bytes == 18) {
                        //成功
                        Log.e("11", "线程>>>===成功===")
                    }
                    break
                } catch (ex: IOException) {
                    Log.e("11", "线程>>>读取数据======失败")
                    ex.printStackTrace()
                    // 读取数据失败处理
                    mReadCode = -1
                    isStopQuery =true
                    break
                }
            }
        }

        fun getReadBackCode() = mReadCode
        /**
         * 中止读取code时，重置
         */
        fun resetBackCode() {
            isStopQuery =true
            mReadCode = -3
            Log.e("11","线程>>>重置=====")
        }

    }
}