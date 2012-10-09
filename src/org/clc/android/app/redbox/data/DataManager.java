package org.clc.android.app.redbox.data;

import android.util.Log;

import org.clc.android.app.redbox.data.ActionHistoryManager.OnHistoryChangeListener;
import org.clc.android.app.redbox.data.BlockSettingsManager.OnBlockSettingChangeListener;
import org.clc.android.app.redbox.data.GroupRulesManager.OnGroupRulesChangeListener;
import org.clc.android.app.redbox.data.PatternSettingsManager.OnPatternSettingChangeListener;
import org.clc.android.app.redbox.data.SuggestionsManager.OnSuggestionChangedListener;

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

/**
 * Manage data control.
 * 
 * @author sj38.park
 */
public class DataManager {
	private static final String TAG = "RedBox data";

	// Data version, not application version.
	private static final int VERSION = 1;
	private static final String DATA_FILE_PATH = "/data/data/org.clc.android.app.redbox/block_settings.rbx";

	/**
	 * Phone number seperators that can be element of number.
	 */
	public static final String[] NUMBER_SEPERATORS = { "+", "-", " ", "(", ")" };

	public static final String INTERNATIONAL_MARK = "+";
	public static final String INTERNATIONAL_MARK_ENCODED = "-";

	private static DataManager mSingleton = null;

	private BlockSettingsManager mBlockManager = null;
	private PatternSettingsManager mPatternManager = null;
	private ActionHistoryManager mHistoryManager = null;
	private GroupRulesManager mGroupsManager = null;
	private SuggestionsManager mSuggestionsManager = null;

	private boolean mDataLoaded = false;

	private DataManager() {
		mPatternManager = new PatternSettingsManager();
		mBlockManager = new BlockSettingsManager();
		mHistoryManager = new ActionHistoryManager();
		mGroupsManager = new GroupRulesManager();
		mSuggestionsManager = new SuggestionsManager();
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
		FileInputStream fis = null;
		ObjectInput serialized = null;
		TreeMap<Long, BlockSetting> blockSettings = null;
		ArrayList<PatternSetting> patternSettings = null;
		ArrayList<ActionRecord> actionRecords = null;
		ArrayList<GroupRule> groupRules = null;
		ArrayList<String> suggestions = null;
		try {
			Log.i(TAG, "load setting data.");
			fis = new FileInputStream(file);
			serialized = new ObjectInputStream(fis);
			blockSettings = (TreeMap<Long, BlockSetting>) serialized
					.readObject();
			patternSettings = (ArrayList<PatternSetting>) serialized
					.readObject();
			actionRecords = (ArrayList<ActionRecord>) serialized.readObject();
			groupRules = (ArrayList<GroupRule>) serialized.readObject();
			suggestions = (ArrayList<String>) serialized.readObject();

		} catch (IOException e) {
			Log.e(TAG, "IOException while read data file!", e);
			return;
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Class not found.", e);
		} finally {
			if (serialized != null) {
				try {
					serialized.close();
				} catch (IOException e) {

				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {

				}
			}
			if (blockSettings != null) {
				mBlockManager.setDatas(blockSettings);
			}
			if (patternSettings != null) {
				mPatternManager.setDatas(patternSettings);
			}
			if (actionRecords != null) {
				mHistoryManager.setDatas(actionRecords);
			}
			if (groupRules != null) {
				mGroupsManager.setDatas(groupRules);
			}
			if (suggestions != null) {
				mSuggestionsManager.setData(suggestions);
			}

			mDataLoaded = true;
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
			serialized.writeObject(mHistoryManager.getDatas());
			serialized.writeObject(mGroupsManager.getDatas());
			serialized.writeObject(mSuggestionsManager.getData());
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
		if (parsedNumber.startsWith(INTERNATIONAL_MARK_ENCODED)) {
			parsedNumber = parsedNumber.substring(
					INTERNATIONAL_MARK_ENCODED.length(), parsedNumber.length());
		}
		return parsedNumber.matches("\\d+");
	}

	public static String getParsedNumber(final BlockSetting setting) {
		return getParsedNumber(setting.mNumber);
	}

	public static String getParsedNumber(final String number) {
		boolean isInternationalNumber = false;
		if (number.startsWith(INTERNATIONAL_MARK)) {
			isInternationalNumber = true;
		}

		String parsed = number;
		parsed = parsed.replace(INTERNATIONAL_MARK, INTERNATIONAL_MARK_ENCODED);

		for (String seperator : NUMBER_SEPERATORS) {
			parsed = parsed.replace(seperator, "");
		}

		if (isInternationalNumber) {
			parsed = INTERNATIONAL_MARK_ENCODED + parsed;
		}
		return parsed;
	}

	public boolean existActiveRule() {
		return mPatternManager.existActiveRule()
				|| mGroupsManager.existActiveRule()
				|| mBlockManager.existActiveRule();
	}

	public int getSize() {
		return mPatternManager.getSize() + mGroupsManager.getSize()
				+ mBlockManager.getSize();
	}

	public int getHistorySize() {
		return mHistoryManager.getSize();
	}

	public ArrayList<PatternSetting> getPatterns() {
		return mPatternManager.getDatas();
	}

	/**
	 * For block / pattern / group.
	 * 
	 * @param id
	 * @return
	 */
	public BlockSetting get(int id) {
		if (id < mPatternManager.getSize()) {
			return mPatternManager.get(id);
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			return mGroupsManager.get(id - mPatternManager.getSize());
		}
		return mBlockManager.get(id - mPatternManager.getSize()
				- mGroupsManager.getSize());
	}

	public ActionRecord getHistory(int id) {
		return mHistoryManager.get(id);
	}

	/**
	 * For block / group.
	 * 
	 * @param parsedNumber
	 * @return
	 */
	public BlockSetting getBlockSetting(String parsedNumber) {
		BlockSetting setting = mBlockManager.get(parsedNumber);
		if (setting != null) {
			return mBlockManager.get(parsedNumber);
		} else {
			return mGroupsManager.get(parsedNumber);
		}
	}

	/**
	 * For block / group.
	 * 
	 * @param setting
	 * @return
	 */
	public boolean isExist(BlockSetting setting) {
		return mBlockManager.isExist(setting)
				|| mGroupsManager.isExist(setting.mParsedNumber);
	}

	public void add(BlockSetting setting) {
		Log.d(TAG, "add " + setting.toString());
		if (setting instanceof PatternSetting) {
			mPatternManager.add((PatternSetting) setting);
		} else if (setting instanceof GroupRule) {
			mGroupsManager.addGroup((GroupRule) setting);
		} else {
			mBlockManager.add(setting);
		}
	}

	public void addHistory(ActionRecord record) {
		Log.d(TAG, "add history " + record.toString());
		mHistoryManager.add(record);
	}

	public void removeHistory(int id) {
		Log.d(TAG, "remove history " + id);
		mHistoryManager.remove(id);
	}

	public void remove(final int id) {
		if (id < mPatternManager.getSize()) {
			mPatternManager.remove(id);
			return;
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			mGroupsManager.removeGroup(id - mPatternManager.getSize());
			return;
		}
		mBlockManager.remove(id - mPatternManager.getSize()
				- mGroupsManager.getSize());
	}

	public void remove(final BlockSetting setting) {
		if (setting instanceof PatternSetting) {
			mPatternManager.remove((PatternSetting) setting);
		} else if (setting instanceof GroupRule) {
			mGroupsManager.remove((GroupRule) setting);
		} else {
			mBlockManager.remove(setting);
		}
	}

	public void remove(String parsedNumber) {
		BlockSetting setting = mBlockManager.get(parsedNumber);
		if (setting != null) {
			mBlockManager.remove(setting);
			return;
		}
		mGroupsManager.removeMember(parsedNumber);
	}

	public boolean isExist(final String parsedNumber) {
		return mBlockManager.isExist(parsedNumber)
				|| mGroupsManager.isExist(parsedNumber);
	}

	public void update(final int id, final BlockSetting setting) {
		if (id < mPatternManager.getSize()) {
			if (setting instanceof PatternSetting) {
				mPatternManager.update(id, (PatternSetting) setting);
			} else {
				Log.e(TAG, "Block setting update with pattern setting id!");
			}
			return;
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			if (setting instanceof GroupRule) {
				mGroupsManager.update(id - mPatternManager.getSize(),
						(GroupRule) setting);
			} else {
				Log.e(TAG, "Non group rule update with group rule id!");
			}
			return;
		}
		mBlockManager.update(
				id - mGroupsManager.getSize() - mPatternManager.getSize(),
				setting);
	}

	public void updateRejectCall(final int id, boolean reject) {
		if (id < mPatternManager.getSize()) {
			mPatternManager.updateRejectCall(id, reject);
			return;
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			mGroupsManager.updateRejectCall(id - mPatternManager.getSize(),
					reject);
			return;
		}
		mBlockManager.updateRejectCall(id - mPatternManager.getSize()
				- mGroupsManager.getSize(), reject);
	}

	public void updateDeleteCallLog(final int id, boolean deleteCallLog) {
		if (id < mPatternManager.getSize()) {
			mPatternManager.updateDeleteCallLog(id, deleteCallLog);
			return;
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			mGroupsManager.updateDeleteCallLog(id - mPatternManager.getSize(),
					deleteCallLog);
			return;
		}
		mBlockManager.updateDeleteCallLog(id - mPatternManager.getSize()
				- mGroupsManager.getSize(), deleteCallLog);
	}

	public void updateSendAutoSMS(final int id, boolean send) {
		if (id < mPatternManager.getSize()) {
			mPatternManager.updateSendAutoSMS(id, send);
			return;
		} else if (id < mPatternManager.getSize() + mGroupsManager.getSize()) {
			mGroupsManager.updateSendAutoSMS(id - mPatternManager.getSize(),
					send);
			return;
		}
		mBlockManager.updateSendAutoSMS(id - mPatternManager.getSize()
				- mGroupsManager.getSize(), send);
	}

	public String getSuggestion(int position) {
		return mSuggestionsManager.get(position);
	}

	public void updateSuggestion(int position, String suggestion) {
		mSuggestionsManager.update(position, suggestion);
	}

	public void deleteSuggestion(int position) {
		mSuggestionsManager.delete(position);
	}

	public void addSuggestion(String suggestion) {
		mSuggestionsManager.add(suggestion);
	}

	public int getSuggestionsCount() {
		return mSuggestionsManager.getCount();
	}

	public void updateSuggestionPriority(int position) {
		mSuggestionsManager.updatePriority(position);
	}

	public void setOnBlockSettingChangeListener(
			OnBlockSettingChangeListener listener) {
		mBlockManager.setOnBlockSettingChangeListener(listener);
	}

	public void setOnPatternSettingChangeListener(
			OnPatternSettingChangeListener listener) {
		mPatternManager.setOnPatternSettingChangeListener(listener);
	}

	public void setOnHistoryChangeListener(OnHistoryChangeListener listener) {
		mHistoryManager.setOnHistoryChangeListener(listener);
	}

	public void setOnGroupRulesChangeListener(
			OnGroupRulesChangeListener listener) {
		mGroupsManager.setOnGroupRulesChangeListener(listener);
	}

	public void setOnSuggestionsChangedListener(
			OnSuggestionChangedListener listener) {
		mSuggestionsManager.setOnSuggestionChangedListener(listener);
	}

	public int getId(final BlockSetting rule) {
		if (rule instanceof PatternSetting) {
			int id = mPatternManager.getId((PatternSetting) rule);
			return id;
		} else if (rule instanceof GroupRule) {
			int id = mGroupsManager.getPosition((GroupRule) rule);
			return id == -1 ? -1 : id + mPatternManager.getSize();
		}
		int id = mBlockManager.getId(rule);
		return id == -1 ? -1 : id + mPatternManager.getSize()
				+ mGroupsManager.getSize();
	}
}
