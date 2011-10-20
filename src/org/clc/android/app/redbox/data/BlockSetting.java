package org.clc.android.app.redbox.data;

import java.io.Serializable;

/**
 * Just struct.
 * 
 * @author sj38.park
 * 
 */
public class BlockSetting implements Serializable {
    public String mAlias;
    public String mNumber;
    public String mParsedNumber;
    public boolean mRejectCall;
    public boolean mDeleteCallLog;
    public boolean mSendAutoSMS;
    public String mAutoSMS;

    public BlockSetting() {
    }

    public BlockSetting(String alias, String number, boolean rejectCall,
            boolean deleteCallLog, boolean sendAutoSMS, String autoSMS) {
        mAlias = alias;
        mNumber = number;
        mParsedNumber = DataManager.getParsedNumber(number);
        mRejectCall = rejectCall;
        mDeleteCallLog = deleteCallLog;
        mSendAutoSMS = sendAutoSMS;
        mAutoSMS = autoSMS;
    }

    public BlockSetting(String alias, String number) {
        this(alias, number, false, false, false, "");
    }

    public BlockSetting(String number) {
        this("", number, false, false, false, "");
    }

    public String toString() {
        return "{ Alias : " + mAlias + " / " + "Number : " + mNumber + " / "
                + "RejectCall : " + mRejectCall + " / " + "DeleteCallLog : "
                + mDeleteCallLog + " / " + "SendAutoSMS : " + mSendAutoSMS
                + " / " + "AutoSMS : " + mAutoSMS + "}";
    }
}
