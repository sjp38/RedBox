
package org.clc.android.app.redbox.data;

import java.util.ArrayList;

public class GroupRulesManager {
    private static final String TAG = "RedBox_groupsManager";

    private ArrayList<GroupRule> mGroupRules = new ArrayList<GroupRule>();
    private OnGroupRulesChangeListener mListener = null;

    public void setDatas(ArrayList<GroupRule> groupRules) {
        mGroupRules = groupRules;
    }

    public ArrayList<GroupRule> getDatas() {
        return mGroupRules;
    }

    public GroupRule get(int position) {
        if (position >= mGroupRules.size()) {
            return null;
        }
        return mGroupRules.get(position);
    }

    public GroupRule get(String parsedNumber) {
        for (GroupRule group : mGroupRules) {
            for (BlockSetting member : group.mMembers) {
                if (parsedNumber.equals(member.mParsedNumber)) {
                    return group;
                }
            }
        }
        return null;
    }

    public void addNumberTo(int position, BlockSetting number) {
        if (position >= mGroupRules.size()) {
            return;
        }
        GroupRule groupRule = mGroupRules.get(position);
        groupRule.mMembers.add(number);
        notifyGroupRulesChanged();
    }

    public void removeNumberFrom(int position, BlockSetting number) {
        if (position >= mGroupRules.size()) {
            return;
        }
        GroupRule groupRule = mGroupRules.get(position);
        groupRule.mMembers.remove(number);
        notifyGroupRulesChanged();
    }

    public int getSize() {
        return mGroupRules.size();
    }

    public int getMemberSize(int groupPosition) {
        if (groupPosition >= mGroupRules.size()) {
            return -1;
        }
        GroupRule groupRule = mGroupRules.get(groupPosition);
        return groupRule.mMembers.size();
    }

    public BlockSetting getMemberFrom(int groupPosition, int numberPosition) {
        if (groupPosition >= mGroupRules.size()) {
            return null;
        }
        GroupRule groupRule = mGroupRules.get(groupPosition);
        ArrayList<BlockSetting> blockSettings = groupRule.mMembers;
        if (numberPosition >= blockSettings.size()) {
            return null;
        }
        return blockSettings.get(numberPosition);
    }

    public void setDefaultAutoSms(int position, String autoSms) {
        if (position >= mGroupRules.size()) {
            return;
        }
        GroupRule groupRule = mGroupRules.get(position);
        groupRule.mAutoSMS = autoSms;
        notifyGroupRulesChanged();
    }

    public void update(int position, GroupRule groupRule) {
        if (position >= mGroupRules.size()) {
            return;
        }
        mGroupRules.remove(position);
        mGroupRules.add(position, groupRule);
        notifyGroupRulesChanged();
    }

    public void updateRejectCall(int position, boolean rejectCall) {
        if (position >= mGroupRules.size()) {
            return;
        }
        final GroupRule group = mGroupRules.get(position);
        group.mRejectCall = rejectCall;
        notifyGroupRulesChanged();
    }

    public void updateDeleteCallLog(int position, boolean deleteCallLog) {
        if (position >= mGroupRules.size()) {
            return;
        }
        final GroupRule group = mGroupRules.get(position);
        group.mDeleteCallLog = deleteCallLog;
        notifyGroupRulesChanged();
    }

    public void updateSendAutoSMS(int position, boolean sendAutoSMS) {
        if (position >= mGroupRules.size()) {
            return;
        }
        final GroupRule group = mGroupRules.get(position);
        group.mSendAutoSMS = sendAutoSMS;
        notifyGroupRulesChanged();
    }

    public void addGroup(GroupRule groupRule) {
        mGroupRules.add(groupRule);
        notifyGroupRulesChanged();
    }

    public void removeGroup(int position) {
        if (position >= mGroupRules.size()) {
            return;
        }
        mGroupRules.remove(position);
        notifyGroupRulesChanged();
    }

    public void remove(GroupRule rule) {
        mGroupRules.remove(rule);
    }

    public void removeMember(String parsedNumber) {
        int targetPosition = -1;
        BlockSetting selectedMember = null;
        for (GroupRule group : mGroupRules) {
            for (BlockSetting member : group.mMembers) {
                if (parsedNumber.equals(member.mParsedNumber)) {
                    targetPosition = group.mMembers.indexOf(member);
                    break;
                }
            }

            if (targetPosition != -1) {
                group.mMembers.remove(targetPosition);
                break;
            }
        }
    }

    public boolean isExist(String parsedNumber) {
        for (GroupRule group : mGroupRules) {
            for (BlockSetting member : group.mMembers) {
                if (member.mParsedNumber.equals(parsedNumber)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BlockSetting getMember(String parsedNumber) {
        for (GroupRule group : mGroupRules) {
            for (BlockSetting member : group.mMembers) {
                if (member.mParsedNumber.equals(parsedNumber)) {
                    return member;
                }
            }
        }
        return null;
    }

    public int getPosition(GroupRule rule) {
        for (GroupRule group : mGroupRules) {
            if (group.mAlias.equals(rule.mAlias)) {
                return mGroupRules.indexOf(group);
            }
        }
        return -1;
    }

    private void notifyGroupRulesChanged() {
        if (mListener != null) {
            mListener.onGroupRulesChanged();
        }
    }

    public void setOnGroupRulesChangeListener(OnGroupRulesChangeListener listener) {
        mListener = listener;
    }

    public static interface OnGroupRulesChangeListener {
        void onGroupRulesChanged();
    }
}
