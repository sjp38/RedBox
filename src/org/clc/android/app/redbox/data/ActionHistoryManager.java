package org.clc.android.app.redbox.data;

import java.util.ArrayList;

/**
 * History manager. Just array list of ActionRecords.
 * 
 * @author sj38.park
 * 
 */
public class ActionHistoryManager {
    private static final String TAG = "RedBox data_history";

    private static final int MAX_RECORD = 200;

    private ArrayList<ActionRecord> mRecords = new ArrayList<ActionRecord>();
    private ArrayList<OnHistoryChangeListener> mListeners = new ArrayList<OnHistoryChangeListener>();

    public void setDatas(ArrayList<ActionRecord> records) {
        mRecords = records;
    }

    public ArrayList<ActionRecord> getDatas() {
        return mRecords;
    }

    public void notifyHistoryChanged() {
        for (OnHistoryChangeListener listener : this.mListeners) {
            listener.onHistoryChanged();
        }
    }

    public void setOnHistoryChangeListener(OnHistoryChangeListener listener) {
        mListeners.add(listener);
    }

    public int getSize() {
        return mRecords.size();
    }

    public ActionRecord get(int id) {
        if (id >= mRecords.size()) {
            return null;
        }
        return mRecords.get(id);
    }

    public void add(ActionRecord record) {
        mRecords.add(0, record);
        notifyHistoryChanged();

        if (mRecords.size() > MAX_RECORD) {
            mRecords.remove(mRecords.size() - 1);
        }
    }

    public void update(int id, ActionRecord record) {
        if (mRecords.size() <= id) {
            return;
        }
        mRecords.remove(id);
        mRecords.add(id, record);
        notifyHistoryChanged();
    }

    public void remove(int id) {
        if (id >= mRecords.size()) {
            return;
        }
        mRecords.remove(id);
        notifyHistoryChanged();
    }

    public void remove(ActionRecord setting) {
        mRecords.remove(setting);
        notifyHistoryChanged();
    }

    public static interface OnHistoryChangeListener {
        void onHistoryChanged();
    }
}
