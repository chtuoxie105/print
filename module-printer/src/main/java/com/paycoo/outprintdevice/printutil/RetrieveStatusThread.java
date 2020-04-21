package com.paycoo.outprintdevice.printutil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

/**
 * 状态监听器
 */
public class RetrieveStatusThread extends Thread {
    private final Object mLock;
    private SendCallback.StatusCallback mCallback;

    private StarIOPort mPort;

    private String mPortName = null;
    private String mPortSettings;
    private int mTimeout;
    private int mPositionId;
    private Context mContext;

    @SuppressWarnings("unused")
    RetrieveStatusThread(Object lock, int positionID, StarIOPort port, SendCallback.StatusCallback callback) {
        mPort = port;
        mCallback = callback;
        mLock = lock;
        mPositionId = positionID;
    }

    public RetrieveStatusThread(Object lock, int positionID, String portName, String portSettings, int timeout, Context context, SendCallback.StatusCallback callback) {
        mPortName = portName;
        mPortSettings = portSettings;
        mTimeout = timeout;
        mContext = context;
        mCallback = callback;
        mLock = lock;
        mPositionId = positionID;
    }

    @Override
    public void run() {

        synchronized (mLock) {
            StarPrinterStatus status = null;

            try {
                if (mPort == null) {

                    if (mPortName == null) {
                        resultSendCallback(mPositionId,mPortName,null, mCallback);
                        return;
                    } else {
                        mPort = StarIOPort.getPort(mPortName, mPortSettings, mTimeout, mContext);
                    }
                }

                if (mPort == null) {
                    resultSendCallback(mPositionId,mPortName,null, mCallback);
                    return;
                }

                status = mPort.retreiveStatus();

            } catch (StarIOPortException e) {
                // Nothing
            }

            if (mPort != null && mPortName != null) {
                try {
                    StarIOPort.releasePort(mPort);
                } catch (StarIOPortException e) {
                    // Nothing
                }
                mPort = null;
            }

            resultSendCallback(mPositionId,mPortName,status, mCallback);
        }
    }

    private static void resultSendCallback(final int itemId, final String ip, final StarPrinterStatus status, final SendCallback.StatusCallback callback) {
        if (callback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onStatus(itemId,ip,status);
                }
            });
        }
    }
}
