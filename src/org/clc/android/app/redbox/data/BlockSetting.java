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
    public boolean mRejectCall;
    public boolean mDeleteCallLog;
    public boolean mSendAutoSMS;
    public String mAutoSMS;

    public BlockSetting(String alias, String number, boolean blockCall,
            boolean deleteCallLog, boolean sendAutoSMS, String autoSMS) {
        mAlias = alias;
        mNumber = number;
        mRejectCall = blockCall;
        mDeleteCallLog = deleteCallLog;
        mSendAutoSMS = sendAutoSMS;
        mAutoSMS = autoSMS;
    }

    public BlockSetting(String alias, String number) {
        this(alias, number, true, true, false, "");
    }

    public BlockSetting(String number) {
        this("", number, true, true, false, "");
    }

    public String toString() {
        return "{ Alias : " + mAlias + " / " + "Number : " + mNumber + " / "
                + "RejectCall : " + mRejectCall + " / " + "DeleteCallLog : "
                + mDeleteCallLog + " / " + "SendAutoSMS : " + mSendAutoSMS
                + " / " + "AutoSMS : " + mAutoSMS + "}";
    }
}
