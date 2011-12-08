
package org.clc.android.app.redbox.data;

import java.util.ArrayList;
import java.util.TreeMap;

import android.util.Log;

/**
 * Manage data control.
 * 
 * @author sj38.park
 */
public class BlockSettingsManager {
    private static final String TAG = "RedBox data_blockManaging";

    private TreeMap<Long, BlockSetting> mBlockSettings = new TreeMap<Long, BlockSetting>();
    private ArrayList<OnBlockSettingChangeListener> mListeners = new ArrayList<OnBlockSettingChangeListener>();

    public BlockSettingsManager() {
    }

    public void setDatas(TreeMap<Long, BlockSetting> blockSettings) {
        mBlockSettings = blockSettings;
    }

    public TreeMap<Long, BlockSetting> getDatas() {
        return mBlockSettings;
    }

    private static Long makeKey(final BlockSetting setting) {
        return makeKey(setting.mParsedNumber);
    }

    private static Long makeKey(final String parsedNumber) {
        return Long.parseLong(parsedNumber);
    }

    public int getSize() {
        return mBlockSettings.size();
    }

    private Long getKeyForId(int id) {
        final Long[] keys = mBlockSettings.keySet().toArray(new Long[0]);
        return keys[id];
    }

    public BlockSetting get(int id) {
        if (id >= mBlockSettings.size()) {
            return null;
        }
        final Long key = getKeyForId(id);
        return mBlockSettings.get(key);
    }

    public BlockSetting get(String parsedNumber) {
        final Long key = makeKey(parsedNumber);
        return mBlockSettings.get(key);
    }

    public boolean isExist(BlockSetting setting) {
        final Long key = makeKey(setting);
        return isExist(key);
    }

    public boolean isExist(String parsedNumber) {
        final Long key = makeKey(parsedNumber);
        return isExist(key);
    }

    private boolean isExist(Long key) {
        return mBlockSettings.get(key) != null;
    }

    public boolean add(BlockSetting setting) {
        Log.d(TAG, "add " + setting.toString());
        Long key = makeKey(setting);
        if (isExist(key)) {
            return false;
        }
        mBlockSettings.put(key, setting);
        notifyDataChanged();
        return true;
    }

    private void remove(final Long key) {
        mBlockSettings.remove(key);
        notifyDataChanged();
    }

    public void remove(final int id) {
        final Long key = getKeyForId(id);
        remove(key);
    }

    public void remove(final BlockSetting setting) {
        final Long key = makeKey(setting);
        remove(key);
    }

    private void notifyDataChanged() {
        for (OnBlockSettingChangeListener listener : mListeners) {
            listener.onBlockSettingsChanged();
        }
    }

    private BlockSetting getValueForId(final int id) {
        final Long key = getKeyForId(id);
        return mBlockSettings.get(key);
    }

    public void update(final int id, final BlockSetting setting) {
        final BlockSetting originalSetting = getValueForId(id);
        final Long oldKey = makeKey(originalSetting.mParsedNumber);
        mBlockSettings.remove(oldKey);

        Long key = makeKey(setting.mNumber);
        mBlockSettings.put(key, setting);
        notifyDataChanged();
    }

    public void updateRejectCall(int id, boolean reject) {
        final Long key = getKeyForId(id);
        final BlockSetting setting = mBlockSettings.get(key);
        setting.mRejectCall = reject;
    }

    public void updateDeleteCallLog(int id, boolean deleteCallLog) {
        final Long key = getKeyForId(id);
        final BlockSetting setting = mBlockSettings.get(key);
        setting.mDeleteCallLog = deleteCallLog;
    }

    public void updateSendAutoSMS(int id, boolean sendAutoSMS) {
        final Long key = getKeyForId(id);
        final BlockSetting setting = mBlockSettings.get(key);
        setting.mSendAutoSMS = sendAutoSMS;
    }

    public void setOnBlockSettingChangeListener(
            OnBlockSettingChangeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<OnBlockSettingChangeListener>();
        }
        mListeners.add(listener);
    }
    
    public int getId(BlockSetting rule) {
        final Long searchingKey = makeKey(rule.mParsedNumber);
        final Long[] keys = mBlockSettings.keySet().toArray(new Long[0]);
        
        for (int i=0 ; i <keys.length; i++) {
            if (keys[i].equals(searchingKey)) {
                return i;
            }
        }
        return -1;
    }

    public static interface OnBlockSettingChangeListener {
        void onBlockSettingsChanged();
    }
}
