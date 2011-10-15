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

import android.util.Log;

/**
 * Manage data control.
 * 
 * @author sj38.park
 * 
 */
public class DataManager {
    private static final String DATA_FILE_PATH = "/data/data/org.clc.android.app.redbox/block_settings.rbx";
    private static final String TAG = "RedBox data";

    private static DataManager mSingleton = null;

    private ArrayList<BlockSetting> mBlockSettings = new ArrayList<BlockSetting>();
    private ArrayList<OnBlockSettingChangeListener> mListeners = new ArrayList<OnBlockSettingChangeListener>();

    private boolean mDataLoaded = false;

    public static DataManager getInstance() {
        if (mSingleton == null) {
            mSingleton = new DataManager();
        }
        return mSingleton;
    }

    /**
     * Get block settings. If this is first time, read from file.
     * 
     * @return
     */
    public synchronized ArrayList<BlockSetting> getBlockSettings() {
        if (mDataLoaded) {
            return mBlockSettings;
        }
        File file = new File(DATA_FILE_PATH);
        try {
            Log.i(TAG, "load setting data.");
            FileInputStream fis = new FileInputStream(file);
            ObjectInput serialized = new ObjectInputStream(fis);
            mBlockSettings = (ArrayList<BlockSetting>) serialized.readObject();
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

    public BlockSetting getBlockSetting(int id) {
        if (id >= mBlockSettings.size()) {
            return null;
        }
        return mBlockSettings.get(id);
    }

    public void add(BlockSetting block) {
        Log.d(TAG, "add " + block.toString());
        if (isExist(block.mNumber)) {
            return;
        }
        mBlockSettings.add(block);
        for (OnBlockSettingChangeListener listener : mListeners) {
            listener.onBlockSettingsChanged();
        }
        notifyDataChanged();
    }

    public void delete(int id) {
        mBlockSettings.remove(id);
        notifyDataChanged();
    }

    public void delete(String number) {
        Log.d(TAG, "delete " + number);
        for (BlockSetting setting : mBlockSettings) {
            if (setting.mNumber.equals(number)) {
                mBlockSettings.remove(setting);
                notifyDataChanged();
                break;
            }
        }
    }

    public void delete(BlockSetting setting) {
        if (mBlockSettings.remove(setting)) {
            notifyDataChanged();
        }
    }

    public boolean isExist(String number) {
        for (BlockSetting block : mBlockSettings) {
            if (block.mNumber.equals(number)) {
                return true;
            }
        }
        return false;
    }

    private void notifyDataChanged() {
        for (OnBlockSettingChangeListener listener : mListeners) {
            listener.onBlockSettingsChanged();
        }
    }

    public void setSetting(int id, BlockSetting setting) {
        BlockSetting originalSetting = mBlockSettings.get(id);
        originalSetting.mAlias = setting.mAlias;
        originalSetting.mNumber = setting.mNumber;
        originalSetting.mRejectCall = setting.mRejectCall;
        originalSetting.mDeleteCallLog = setting.mDeleteCallLog;
        originalSetting.mSendAutoSMS = setting.mSendAutoSMS;
        originalSetting.mAutoSMS = setting.mAutoSMS;
        notifyDataChanged();
    }

    public void setAlias(int id, String alias) {
        BlockSetting setting = mBlockSettings.get(id);
        setting.mAlias = alias;
        notifyDataChanged();
    }

    public void setNumber(int id, String number) {
        BlockSetting setting = mBlockSettings.get(id);
        setting.mNumber = number;
        notifyDataChanged();
    }

    public void setRejectCall(int id, boolean block) {
        BlockSetting setting = mBlockSettings.get(id);
        setting.mRejectCall = block;
        notifyDataChanged();
    }

    public void setDeleteCallLog(int id, boolean delete) {
        BlockSetting setting = mBlockSettings.get(id);
        setting.mDeleteCallLog = delete;
        notifyDataChanged();
    }

    public void setSendAutoSMS(int id, boolean send, String message) {
        BlockSetting setting = mBlockSettings.get(id);
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
