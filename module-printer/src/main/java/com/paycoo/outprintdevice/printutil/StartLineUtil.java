package com.paycoo.outprintdevice.printutil;


import android.util.Log;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;

import java.nio.charset.Charset;

public class StartLineUtil {
    static StarIoExt.Emulation emulation = StarIoExt.Emulation.StarPRNT;
    public static String line = "------------------------------------------------";

    /**
     * 分左右两边排列（左边是字符，右边那文本）
     */
    public static String getTwoAlignLeftCharAndRightTextStr(String leftItem, String rightItem) {
        return getTwoLineItemtext(leftItem, rightItem, false, true);
    }

    /**
     * 分左右两边排列（左边是文本，右边字符）
     */
    public static String getTwoAlignLeftTextAndRightCharStr(String leftItem, String rightItem) {
        return getTwoLineItemtext(leftItem, rightItem, true, false);
    }

    /**
     * 分左右两边排列（左边是文本，右边文本）
     */
    public static String getTwoAlignLeftTextAndRightTextStr(String leftItem, String rightItem) {
        return getTwoLineItemtext(leftItem, rightItem, true, true);
    }

    /**
     * 分左右两边排列（左边是字符，右边字符）
     */
    public static String getTwoAlignLeftCharAndRightCharStr(String leftItem, String rightItem) {
        return getTwoLineItemtext(leftItem, rightItem, false, false);
    }

    /**
     * 分布2列排，居左右两边缘
     *
     * @param leftItem
     * @param rightItem
     * @param leftTextIsText
     * @param rightTextIsText
     * @return
     */
    private static String getTwoLineItemtext(String leftItem, String rightItem, boolean leftTextIsText, boolean rightTextIsText) {
        int size = 0;
        if (leftTextIsText) {
            size = line.length() - leftItem.length() * 2;
        } else {
            size = line.length() - leftItem.length();
        }
        if (rightTextIsText) {
            size = size - rightItem.length() * 2;
        } else {
            size = size - rightItem.length();
        }

        StringBuffer buffer = new StringBuffer(leftItem);
        for (int i = 0; i < size; i++) {
            buffer.append(" ");
        }
        buffer.append(rightItem);
        Log.e("11",  buffer.toString());
        buffer.append("\n");
        return buffer.toString();
    }

    /**
     * 分3列平分排列（左边是文本，中间文本，右边文本）
     */
    public static String getThreeAlignLeftTextAndCenterTextAndRightTextStr(String leftItem, String centerItem, String rightItem) {
        return getThreeAlignLeftAndCenterAndRightStr(leftItem, centerItem, rightItem, true, true, true);
    }

    /**
     * 分3列平分排列（左边是文本，中间字符，右边字符）
     */
    public static String getThreeAlignLeftTextAndCenterCharAndRightCharStr(String leftItem, String centerItem, String rightItem) {
        return getThreeAlignLeftAndCenterAndRightStr(leftItem, centerItem, rightItem, true, false, false);
    }

    public static String getThreeAlignLeftAndCenterAndRightStr(String leftItem, String centerItem, String rightItem,
                                                               boolean leftIsText, boolean centerIsText, boolean rightIsText) {
        //除去文本和字符后剩余的空白空间
        StringBuffer buffer = new StringBuffer();
        int centerSize = 0;
        if (leftIsText) {
            centerSize = line.length() / 2 - leftItem.length() * 2;
        } else {
            centerSize = line.length() / 2 - leftItem.length();
        }
        buffer.append(leftItem);

        for (int i = 0; i < centerSize; i++) {
            buffer.append(" ");
        }
        buffer.append(centerItem);
        int rightBlankSize = 0;
        if (leftIsText) {
            rightBlankSize = line.length() - leftItem.length() * 2;
        } else {
            rightBlankSize = line.length() - rightItem.length();
        }
        if (rightIsText) {
            rightBlankSize = rightBlankSize - rightItem.length() * 2;
        } else {
            rightBlankSize = rightBlankSize - rightItem.length();
        }
        if (centerIsText) {
            rightBlankSize = rightBlankSize - centerItem.length() * 2;
        } else {
            rightBlankSize = rightBlankSize - centerItem.length();
        }
        rightBlankSize = rightBlankSize - centerSize;

        for (int i = 0; i < rightBlankSize; i++) {
            buffer.append(" ");
        }
        buffer.append(rightItem);

        Log.e("11", buffer.toString());
        buffer.append("\n");
        return buffer.toString();
    }

    public static byte[] get1LinePrintTextByteArray(String printMsg){
        Log.e("11",printMsg);
        return (printMsg + "\n").getBytes(getEncoding());
    }

    public static Charset getEncoding() {
        return Charset.forName("GB2312");
    }

    public static ICommandBuilder getCommandBuilder() {
        ICommandBuilder builder = StarIoExt.createCommandBuilder(emulation);
        return builder;
    }

}
