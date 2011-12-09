
package org.clc.android.app.redbox.data;

import java.io.Serializable;
import java.util.ArrayList;

public class PatternSetting extends BlockSetting implements Serializable {
    public String mStartPattern = null;
    public String mEndPattern = null;
    public boolean mAllNumber = false;
    public ArrayList<ExceptionNumber> mExceptions = null;

    public PatternSetting() {
    }

    public PatternSetting(String alias, String startPattern, String endPattern,
            boolean allNumber, ArrayList<ExceptionNumber> exceptions,
            boolean rejectCall, boolean deleteCallLog, boolean sendAutoSMS,
            String autoSMS) {
        super();
        mAlias = alias;
        mStartPattern = DataManager.getParsedNumber(startPattern);
        mEndPattern = DataManager.getParsedNumber(endPattern);
        mAllNumber = allNumber;
        mExceptions = exceptions;
        mRejectCall = rejectCall;
        mDeleteCallLog = deleteCallLog;
        mSendAutoSMS = sendAutoSMS;
        mAutoSMS = autoSMS;
    }

    public boolean matches(String parsedNumber) {
        for (ExceptionNumber exception : mExceptions) {
            if (parsedNumber.equals(exception.mParsedNumber)) {
                return false;
            }
        }
        if (mAllNumber) {
            return true;
        }
        if (!"".equals(mStartPattern) && parsedNumber.startsWith(mStartPattern)) {
            return true;
        }
        if (!"".equals(mEndPattern) && parsedNumber.endsWith(mEndPattern)) {
            return true;
        }

        return false;
    }

    public static class ExceptionNumber implements Serializable {
        public String mAlias;
        public String mNumber;
        public String mParsedNumber;

        public ExceptionNumber(String alias, String number) {
            mAlias = alias;
            mNumber = number;
            mParsedNumber = DataManager.getParsedNumber(number);
        }

        public ExceptionNumber(String alias, String number, String parsedNumber) {
            mAlias = alias;
            mNumber = number;
            mParsedNumber = parsedNumber;
        }
    }

    public String toString() {
        String exceptions = "";
        if (mExceptions == null) {
            exceptions = "";
        } else {
            for (ExceptionNumber exception : mExceptions) {
                exceptions += exception.mNumber + ", ";
            }
        }

        return "{ Alias : " + mAlias + " / " + "Number : " + mNumber + " / "
                + "Start pattern : " + mStartPattern + " / " + "End pattern : " + mEndPattern
                + " / " + "All number? : " + mAllNumber + " / "
                + "exceptions : " + exceptions + " / "
                + "RejectCall : " + mRejectCall + " / " + "DeleteCallLog : "
                + mDeleteCallLog + " / " + "SendAutoSMS : " + mSendAutoSMS
                + " / " + "AutoSMS : " + mAutoSMS + "}";
    }
}
