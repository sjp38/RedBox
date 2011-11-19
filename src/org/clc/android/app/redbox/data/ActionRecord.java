package org.clc.android.app.redbox.data;

import java.io.Serializable;

public class ActionRecord implements Serializable {
    public long mTimeStamp;
    public BlockSetting mMatchedRule;

    public ActionRecord(long time, BlockSetting matchedRule) {
        mTimeStamp = time;
        mMatchedRule = matchedRule;
    }

    public String toString() {
        return "on " + mTimeStamp + ", matched by " + mMatchedRule;
    }
}