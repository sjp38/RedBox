package org.clc.android.app.redbox.data;

import java.util.ArrayList;

/**
 * Manage data control.
 * 
 * @author sj38.park
 */
public class PatternSettingsManager {
	private static final String TAG = "RedBox data_pattern";

	private ArrayList<PatternSetting> mPatternSettings = new ArrayList<PatternSetting>();
	private ArrayList<OnPatternSettingChangeListener> mPatternListeners = new ArrayList<OnPatternSettingChangeListener>();

	public PatternSettingsManager() {
	}

	public void setDatas(ArrayList<PatternSetting> settings) {
		mPatternSettings = settings;
	}

	public ArrayList<PatternSetting> getDatas() {
		return mPatternSettings;
	}

	public void notifyPatternsChanged() {
		for (OnPatternSettingChangeListener listener : this.mPatternListeners) {
			listener.onPatternSettingsChanged();
		}
	}

	public void setOnPatternSettingChangeListener(
			OnPatternSettingChangeListener listener) {
		mPatternListeners.add(listener);
	}

	public boolean existActiveRule() {
		for (PatternSetting setting : mPatternSettings) {
			if (setting.mRejectCall || setting.mDeleteCallLog
					|| setting.mSendAutoSMS) {
				return true;
			}
		}
		return false;
	}

	public int getSize() {
		return mPatternSettings.size();
	}

	public PatternSetting get(int id) {
		if (id >= mPatternSettings.size()) {
			return null;
		}
		return mPatternSettings.get(id);
	}

	public void add(PatternSetting setting) {
		mPatternSettings.add(setting);
		notifyPatternsChanged();
	}

	public void update(int id, PatternSetting setting) {
		if (mPatternSettings.size() <= id) {
			return;
		}
		mPatternSettings.remove(id);
		mPatternSettings.add(id, setting);
		notifyPatternsChanged();
	}

	public void updateRejectCall(int id, boolean reject) {
		final PatternSetting setting = mPatternSettings.get(id);
		setting.mRejectCall = reject;
		notifyPatternsChanged();
	}

	public void updateDeleteCallLog(int id, boolean deleteCallLog) {
		final PatternSetting setting = mPatternSettings.get(id);
		setting.mDeleteCallLog = deleteCallLog;
		notifyPatternsChanged();
	}

	public void updateSendAutoSMS(int id, boolean sendAutoSMS) {
		final PatternSetting setting = mPatternSettings.get(id);
		setting.mSendAutoSMS = sendAutoSMS;
		notifyPatternsChanged();
	}

	public void remove(int id) {
		if (id >= mPatternSettings.size()) {
			return;
		}
		mPatternSettings.remove(id);
		notifyPatternsChanged();
	}

	public void remove(PatternSetting setting) {
		mPatternSettings.remove(setting);
		notifyPatternsChanged();
	}

	public int getId(PatternSetting pattern) {
		if (pattern.mAlias == null || "".equals(pattern.mAlias)) {
			return -1;
		}

		for (int i = 0; i < mPatternSettings.size(); i++) {
			final PatternSetting nowPattern = mPatternSettings.get(i);
			if (pattern.mAlias.equals(nowPattern.mAlias)) {
				return i;
			}
		}
		return -1;
	}

	public static interface OnPatternSettingChangeListener {
		void onPatternSettingsChanged();
	}
}
