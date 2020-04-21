package com.paycoo.outprintdevice.printutil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

public class SendCommandThread extends Thread {


    private final Object mLock;
    private SendCallback mCallback;
    private byte[] mCommands;

    private StarIOPort mPort;

    private String mPortName = null;
    private String mPortSettings;
    private int mTimeout;
    private Context mContext;

    public SendCommandThread(Object lock, byte[] commands, StarIOPort port, SendCallback callback) {
        mCommands = commands;
        mPort = port;
        mCallback = callback;
        mLock = lock;
    }

    public SendCommandThread(Object lock, byte[] commands, String portName, String portSettings, int timeout, Context context, SendCallback callback) {
        mCommands = commands;
        mPortName = portName;
        mPortSettings = portSettings;
        mTimeout = timeout;
        mContext = context;
        mCallback = callback;
        mLock = lock;
    }


    @Override
    public void run() {
        SendCallback.Result communicateResult = SendCallback.Result.ErrorOpenPort;
        boolean result = false;
        synchronized (mLock) {
            try {
                if (mPort == null) {

                    if (mPortName == null) {
                        resultSendCallback(false, communicateResult, mCallback);
                        return;
                    } else {
                        //创建并连接
                        mPort = StarIOPort.getPort(mPortName, mPortSettings, mTimeout, mContext);
                    }
                }
                if (mPort == null) {
                    communicateResult = SendCallback.Result.ErrorOpenPort;
                    resultSendCallback(false, communicateResult, mCallback);
                    return;
                }
                StarPrinterStatus status;

                communicateResult = SendCallback.Result.ErrorBeginCheckedBlock;

                status = mPort.beginCheckedBlock();

                if (status.coverOpen) {
                    throw new StarIOPortException("Printer cover is open");
                } else if (status.receiptPaperEmpty) {
//                    status.receiptPaperNearEmptyInner || status.receiptPaperNearEmptyOuter（纸快用尽）
                    communicateResult = SendCallback.Result.ErrorPageEmpty;
                    throw new StarIOPortException("Receipt paper is empty");
                } else if (status.offline) {
                    throw new StarIOPortException("Printer is offline");
                }

                communicateResult = SendCallback.Result.ErrorWritePort;

                mPort.writePort(mCommands, 0, mCommands.length);

                result = true;
                communicateResult = SendCallback.Result.Success;

            } catch (StarIOPortException e) {
                // Nothing
            }

            if (mPort != null && mPortName != null) {
                try {
                    //重连1次
                    StarIOPort.releasePort(mPort);
                } catch (StarIOPortException e) {
                    // Nothing
                }
                mPort = null;
            }

            resultSendCallback(result, communicateResult, mCallback);
        }
    }

    private static void resultSendCallback(final boolean result, final SendCallback.Result communicateResult, final SendCallback callback) {
        if (callback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onStatus(result, communicateResult);
                }
            });
        }
    }
}

