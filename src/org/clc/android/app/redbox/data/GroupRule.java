
package org.clc.android.app.redbox.data;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupRule extends BlockSetting implements Serializable {
    private static final long serialVersionUID = -3569386866317802437L;
    ArrayList<BlockSetting> mMembers = new ArrayList<BlockSetting>();

    public GroupRule(String alias, boolean rejectCall, boolean deleteCallLog, boolean sendAutoSMS,
            String autoSMS, ArrayList<BlockSetting> members) {
        mAlias = alias;
        mRejectCall = rejectCall;
        mDeleteCallLog = deleteCallLog;
        mSendAutoSMS = sendAutoSMS;
        mAutoSMS = autoSMS;
        mMembers = members;
    }

    public ArrayList<BlockSetting> getMembers() {
        return mMembers;
    }

    @Override
    public String toString() {
        String returnValue = "[[group] alias : " + mAlias + ", rejectCall : " + mRejectCall
                + "delete call log : " + mDeleteCallLog + ", send auto SMS : " + mSendAutoSMS
                + ", auto SMS : " + mAutoSMS + ", members : ";
        for (BlockSetting member : mMembers) {
            returnValue += "{" + member.toString() + "}, ";
        }
        returnValue += "]";
        return returnValue;
    }
}
