
package org.clc.android.app.redbox.data;

import java.util.ArrayList;

public class SuggestionsManager {
    private static final int MAX_SUGGESTION = 200;
    private ArrayList<String> mSuggestions = new ArrayList<String>();
    private OnSuggestionChangedListener mListener = null;

    public void setData(ArrayList<String> data) {
        mSuggestions = data;
    }

    public ArrayList<String> getData() {
        return mSuggestions;
    }

    public String get(int position) {
        if (position >= mSuggestions.size()) {
            return null;
        }
        return mSuggestions.get(position);
    }

    public void update(int position, String suggestion) {
        if (position >= mSuggestions.size()) {
            return;
        }
        mSuggestions.remove(position);
        mSuggestions.add(position, suggestion);
        notifySuggestionChanged();
    }

    public void delete(int position) {
        if (position >= mSuggestions.size()) {
            return;
        }
        mSuggestions.remove(position);
        notifySuggestionChanged();
    }

    public void add(String suggestion) {
        mSuggestions.add(0, suggestion);
        if (mSuggestions.size() >= MAX_SUGGESTION) {
            mSuggestions.remove(mSuggestions.size() - 1);
        }
        notifySuggestionChanged();
    }

    public int getCount() {
        return mSuggestions.size();
    }

    public void updatePriority(int position) {
        final String suggestion = mSuggestions.get(position);
        mSuggestions.remove(position);
        mSuggestions.add(0, suggestion);
    }

    private void notifySuggestionChanged() {
        if (mListener != null) {
            mListener.onSuggestionChanged();
        }
        DataManager.getInstance().saveSettings();
    }

    public void setOnSuggestionChangedListener(OnSuggestionChangedListener listener) {
        mListener = listener;
    }

    public static interface OnSuggestionChangedListener {
        public void onSuggestionChanged();
    }
}
