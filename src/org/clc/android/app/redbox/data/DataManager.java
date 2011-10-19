package org.clc.android.app.redbox.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import android.util.Log;

/**
 * Manage data control.
 * 
 * @author sj38.park
 * 
 */
public class DataManager {
    private static final String TAG = "RedBox data";

    private static final String DATA_FILE_PATH = "/data/data/org.clc.android.app.redbox/block_settings.rbx";

    /**
     * Phone number seperators that can be element of number.
     */
    public static final String[] NUMBER_SEPERATORS = { "+", "-", " ", "(", ")" };

    private static DataManager mSingleton = null;

    private TreeMap<Long, BlockSetting> mBlockSettings = new TreeMap<Long, BlockSetting>();
    private ArrayList<OnBlockSettingChangeListener> mListeners = new ArrayList<OnBlockSettingChangeListener>();

    private boolean mDataLoaded = false;

    public DataManager() {
        getBlockSettings();
    }

    public static DataManager getInstance() {
        if (mSingleton == null) {
            mSingleton = new DataManager();
        }
        return mSingleton;
    }

    private static Long makeKey(final BlockSetting setting) {
        return makeKey(setting.mNumber);
    }

    public static boolean isValid(String number) {
        String parsedNumber = getParsedNumber(number);
        return parsedNumber.matches("\\d+");
    }

    private static Long makeKey(final String number) {
        if (!number.matches("\\d+")) {
            return null;
        }
        return Long.parseLong(number);
    }

    public static String getParsedNumber(final BlockSetting setting) {
        return getParsedNumber(setting.mNumber);
    }

    public static String getParsedNumber(final String number) {
        String parsed = null;

        for (String seperator : NUMBER_SEPERATORS) {
            parsed = number.replace(seperator, "");
        }
        return parsed;
    }

    /**
     * Get block settings. If this is first time, read from file.
     * 
     * @return
     */
    public synchronized TreeMap<Long, BlockSetting> getBlockSettings() {
        if (mDataLoaded) {
            return mBlockSettings;
        }
        File file = new File(DATA_FILE_PATH);
        try {
            Log.i(TAG, "load setting data.");
            FileInputStream fis = new FileInputStream(file);
            ObjectInput serialized = new ObjectInputStream(fis);
            mBlockSettings = (TreeMap<Long, BlockSetting>) serialized
                    .readObject();
            serialized.close();
            fis.close();
            mDataLoaded = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException while read data file!", e);
            return mBlockSettings;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found.", e);
        }
        return mBlockSettings;
    }

    public synchronized boolean saveSettings() {
        if (mBlockSettings == null) {
            Log.e(TAG, "Requesting settings saving while setting is null!");
            return true;
        }
        Log.i(TAG, "save setting data.");
        final File file = new File(DATA_FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(file);
            ObjectOutput serialized = new ObjectOutputStream(fos);
            serialized.writeObject(mBlockSettings);
            serialized.flush();
            serialized.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException while save data file!", e);
            return false;
        }
        return true;
    }

    public int getSize() {
        return mBlockSettings.size();
    }

    private Long getKeyForId(int id) {
        final Long[] keys = mBlockSettings.keySet().toArray(new Long[0]);
        return keys[id];
    }

    public BlockSetting getBlockSetting(int id) {
        if (id >= mBlockSettings.size()) {
            return null;
        }
        final Long key = getKeyForId(id);
        return mBlockSettings.get(key);
    }

    public BlockSetting getBlockSetting(String number) {
        final Long key = makeKey(number);
        return mBlockSettings.get(key);
    }

    public boolean isExist(BlockSetting setting) {
        final Long key = makeKey(setting);
        return isExistBlockSetting(key);
    }

    private boolean isExistBlockSetting(Long key) {
        return mBlockSettings.get(key) != null;
    }

    public boolean add(BlockSetting setting) {
        Log.d(TAG, "add " + setting.toString());
        Long key = makeKey(setting);
        if (isExistBlockSetting(key)) {
            return false;
        }
        mBlockSettings.put(key, setting);
        notifyDataChanged();
        return true;
    }

    private void delete(final Long key) {
        mBlockSettings.remove(key);
        notifyDataChanged();
    }

    public void delete(final int id) {
        final Long key = getKeyForId(id);
        delete(key);
    }

    public void delete(final String number) {
        final Long key = makeKey(number);
        delete(key);
    }

    public void delete(final BlockSetting setting) {
        final Long key = makeKey(setting);
        delete(key);
    }

    public boolean isExist(final String number) {
        final Long key = makeKey(number);
        return mBlockSettings.get(key) != null;
    }

    private void notifyDataChanged() {
        for (OnBlockSettingChangeListener listener : mListeners) {
            listener.onBlockSettingsChanged();
        }
    }

    BlockSetting getValueForId(final int id) {
        final Long key = getKeyForId(id);
        return mBlockSettings.get(key);
    }

    public void setSetting(final int id, final BlockSetting setting) {
        BlockSetting originalSetting = getValueForId(id);
        final Long oldKey = makeKey(originalSetting.mNumber);
        mBlockSettings.remove(oldKey);

        originalSetting.mAlias = setting.mAlias;
        originalSetting.mNumber = setting.mNumber;
        originalSetting.mRejectCall = setting.mRejectCall;
        originalSetting.mDeleteCallLog = setting.mDeleteCallLog;
        originalSetting.mSendAutoSMS = setting.mSendAutoSMS;
        originalSetting.mAutoSMS = setting.mAutoSMS;

        Long key = makeKey(setting.mNumber);
        mBlockSettings.put(key, setting);
        notifyDataChanged();
    }

    public void setAlias(final int id, final String alias) {
        final BlockSetting setting = getValueForId(id);
        setting.mAlias = alias;
        notifyDataChanged();
    }

    public void setNumber(final int id, final String number) {
        final BlockSetting setting = getValueForId(id);
        final Long oldKey = makeKey(setting.mNumber);
        mBlockSettings.remove(oldKey);

        setting.mNumber = number;

        final Long key = makeKey(number);
        mBlockSettings.put(key, setting);
        notifyDataChanged();
    }

    public void setRejectCall(final int id, final boolean block) {
        final BlockSetting setting = getValueForId(id);
        setting.mRejectCall = block;
        notifyDataChanged();
    }

    public void setDeleteCallLog(final int id, final boolean delete) {
        final BlockSetting setting = getValueForId(id);
        setting.mDeleteCallLog = delete;
        notifyDataChanged();
    }

    public void setSendAutoSMS(final int id, final boolean send,
            final String message) {
        final BlockSetting setting = getValueForId(id);
        setting.mSendAutoSMS = send;
        if (message != null) {
            setting.mAutoSMS = message;
        }
    }

    public void setOnBlockSettingChangeListener(
            OnBlockSettingChangeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<OnBlockSettingChangeListener>();
        }
        mListeners.add(listener);
    }

    public static interface OnBlockSettingChangeListener {
        void onBlockSettingsChanged();
    }
}