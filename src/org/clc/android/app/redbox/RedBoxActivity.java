package org.clc.android.app.redbox;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RedBoxActivity extends Activity {
    public static final String DATA_PATH = "numbers";
    private static final String TAG = "RedBox";

    private static final int OPTION_MENU_DELETE_BLOCKED = 1;
    private static final int OPTION_MENU_DELETE_UNBLOCKED = 2;

    private ListView mNumbersListView = null;
    private NumbersListAdapter mAdapter = null;
    private LayoutInflater mLayoutInflater = null;
    private SharedPreferences mPreferences = null;

    private OnCheckedChangeListener mNumberCheckChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            View parent = (View) buttonView.getParent();
            TextView numberView = (TextView) parent
                    .findViewById(R.id.number_textview);
            String number = numberView.getText().toString();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(number, isChecked);
            editor.commit();
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPreferences = getSharedPreferences(DATA_PATH, MODE_PRIVATE);

        mNumbersListView = (ListView) findViewById(R.id.numbersList);

        mAdapter = new NumbersListAdapter();
        mNumbersListView.setAdapter(mAdapter);

        mAdapter.bindDatas();

        Context context = getApplicationContext();
        context.startService(new Intent(context, RedBoxService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu
                .add(0, OPTION_MENU_DELETE_BLOCKED, 0,
                        R.string.delete_blocked_menu);
        menu.add(0, OPTION_MENU_DELETE_UNBLOCKED, 0,
                R.string.delete_unblocked_menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case OPTION_MENU_DELETE_BLOCKED:
                mAdapter.deleteNumbers(true);
                return true;
            case OPTION_MENU_DELETE_UNBLOCKED:
                mAdapter.deleteNumbers(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onGetNumberFromContactsClicked(View v) {
        Toast.makeText(this, R.string.wait_next_update, Toast.LENGTH_SHORT)
                .show();
    }

    public void onGetNumberFromCallLogClicked(View v) {
        Toast.makeText(this, R.string.wait_next_update, Toast.LENGTH_SHORT)
                .show();
    }

    public void onAddNumberClicked(View v) {
        EditText textView = (EditText) findViewById(R.id.number_input_textView);
        String number = textView.getText().toString();
        if (mPreferences.contains(number)) {
            Toast.makeText(this, R.string.duplicate_number, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(number, true);
        editor.commit();
        mAdapter.notifyDataSetChanged();
    }

    public class NumberData {
        NumberData(String number, boolean enabled) {
            mNumber = number;
            mEnabled = enabled;
        }

        public String mNumber;
        public boolean mEnabled;
    }

    private class NumbersListAdapter extends BaseAdapter {
        ArrayList<NumberData> mNumbers = null;

        NumbersListAdapter() {
            super();
            mNumbers = new ArrayList<NumberData>();
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mNumbers.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View numberList;
            if (convertView == null) {
                numberList = mLayoutInflater.inflate(R.layout.number_list,
                        parent, false);
            } else {
                numberList = convertView;
            }
            TextView number = (TextView) numberList
                    .findViewById(R.id.number_textview);
            CheckBox checkBox = (CheckBox) numberList
                    .findViewById(R.id.number_enabled_checkBox);

            NumberData data = (NumberData) getItem(position);
            number.setText(data.mNumber);
            checkBox.setChecked(data.mEnabled);
            checkBox.setOnCheckedChangeListener(mNumberCheckChangeListener);

            return numberList;
        }

        @Override
        public Object getItem(int position) {
            if (mNumbers.size() > position) {
                return mNumbers.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            bindDatas();
            super.notifyDataSetChanged();
        }

        private void bindDatas() {
            if (mPreferences == null) {
                Log.e(TAG, "mPreferences is null! maybe not opened yet!!");
                return;
            }
            mNumbers.clear();
            Map<String, ?> datas = mPreferences.getAll();
            for (Entry<String, ?> entry : datas.entrySet()) {
                NumberData data = new NumberData(entry.getKey(),
                        (Boolean) entry.getValue());
                mNumbers.add(data);
            }
        }

        private void deleteNumbers(boolean blocked) {
            SharedPreferences.Editor editor = mPreferences.edit();
            Map<String, ?> datas = mPreferences.getAll();

            for (Entry<String, ?> entry : datas.entrySet()) {
                if ((Boolean) entry.getValue() == blocked) {
                    editor.remove(entry.getKey());
                }
            }
            editor.commit();
            notifyDataSetChanged();
        }
    }
}