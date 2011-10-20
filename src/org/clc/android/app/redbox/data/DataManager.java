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

import org.clc.android.app.redbox.data.BlockSettingsManager.OnBlockSettingChangeListener;
import org.clc.android.app.redbox.data.PatternSettingsManager.OnPatternSettingChangeListener;

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

    private BlockSettingsManager mBlockManager = null;
    private PatternSettingsManager mPatternManager = null;

    private boolean mDataLoaded = false;

    private DataManager() {
        mPatternManager = new PatternSettingsManager();
        mBlockManager = new BlockSettingsManager();
        loadDatas();
    }

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
    public void loadDatas() {
        if (mDataLoaded) {
            return;
        }
        File file = new File(DATA_FILE_PATH);
        try {
            Log.i(TAG, "load setting data.");
            FileInputStream fis = new FileInputStream(file);
            ObjectInput serialized = new ObjectInputStream(fis);
            TreeMap<Long, BlockSetting> blockSettings = (TreeMap<Long, BlockSetting>) serialized
                    .readObject();
            ArrayList<PatternSetting> patternSettings = (ArrayList<PatternSetting>) serialized
                    .readObject();
            serialized.close();
            fis.close();
            mBlockManager.setDatas(blockSettings);
            mPatternManager.setDatas(patternSettings);

            mDataLoaded = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException while read data file!", e);
            return;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found.", e);
        }
        return;
    }

    public synchronized boolean saveSettings() {
        Log.i(TAG, "save setting data.");
        final File file = new File(DATA_FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(file);
            ObjectOutput serialized = new ObjectOutputStream(fos);
            serialized.writeObject(mBlockManager.getDatas());
            serialized.writeObject(mPatternManager.getDatas());
            serialized.flush();
            serialized.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException while save data file!", e);
            return false;
        }
        return true;
    }

    public static boolean isValid(String number) {
        String parsedNumber = getParsedNumber(number);
        return parsedNumber.matches("\\d+");
    }

    public static String getParsedNumber(final BlockSetting setting) {
        return getParsedNumber(setting.mNumber);
    }

    public static String getParsedNumber(final String number) {
        String parsed = number;

        for (String seperator : NUMBER_SEPERATORS) {
            parsed = parsed.replace(seperator, "");
        }
        return parsed;
    }

    public int getSize() {
        return mPatternManager.getSize() + mBlockManager.getSize();
    }

    public ArrayList<PatternSetting> getPatterns() {
        return mPatternManager.getDatas();
    }

    /**
     * For block / pattern.
     * 
     * @param id
     * @return
     */
    public BlockSetting get(int id) {
        if (id < mPatternManager.getSize()) {
            return mPatternManager.get(id);
        }
        return mBlockManager.get(id - mPatternManager.getSize());
    }

    /**
     * For block only.
     * 
     * @param parsedNumber
     * @return
     */
    public BlockSetting getBlockSetting(String parsedNumber) {
        return mBlockManager.get(parsedNumber);
    }

    /**
     * For block only.
     * 
     * @param setting
     * @return
     */
    public boolean isExist(BlockSetting setting) {
        return mBlockManager.isExist(setting);
    }

    public void add(BlockSetting setting) {
        Log.d(TAG, "add " + setting.toString());
        if (setting instanceof PatternSetting) {
            mPatternManager.add((PatternSetting) setting);
        } else {
            mBlockManager.add(setting);
        }
    }

    public void remove(final int id) {
        if (id < mPatternManager.getSize()) {
            mPatternManager.remove(id);
            return;
        }
        mBlockManager.remove(id - mPatternManager.getSize());
    }

    public void remove(final BlockSetting setting) {
        if (setting instanceof PatternSetting) {
            mPatternManager.remove((PatternSetting) setting);
        } else {
            mBlockManager.remove(setting);
        }
    }

    public boolean isExist(final String parsedNumber) {
        return mBlockManager.isExist(parsedNumber);
    }

    public void update(final int id, final BlockSetting setting) {
        if (id < mPatternManager.getSize()) {
            if (setting instanceof PatternSetting) {
                mPatternManager.update(id, (PatternSetting) setting);
            } else {
                Log.e(TAG, "Block setting update with pattern setting id!");
            }
            return;
        }
        mBlockManager.update(id - mPatternManager.getSize(), setting);
    }

    public void updateRejectCall(final int id, boolean reject) {
        if (id < mPatternManager.getSize()) {
            mPatternManager.updateRejectCall(id, reject);
            return;
        }
        mBlockManager.updateRejectCall(id - mPatternManager.getSize(), reject);
    }

    public void updateDeleteCallLog(final int id, boolean deleteCallLog) {
        if (id < mPatternManager.getSize()) {
            mPatternManager.updateDeleteCallLog(id, deleteCallLog);
            return;
        }
        mBlockManager.updateDeleteCallLog(id - mPatternManager.getSize(),
                deleteCallLog);
    }

    public void updateSendAutoSMS(final int id, boolean send) {
        if (id < mPatternManager.getSize()) {
            mPatternManager.updateSendAutoSMS(id, send);
            return;
        }
        mBlockManager.updateSendAutoSMS(id - mPatternManager.getSize(), send);
    }

    public void setOnBlockSettingChangeListener(
            OnBlockSettingChangeListener listener) {
        mBlockManager.setOnBlockSettingChangeListener(listener);
    }

    public void setOnPatternSettingChangeListener(
            OnPatternSettingChangeListener listener) {
        mPatternManager.setOnPatternSettingChangeListener(listener);
    }
}