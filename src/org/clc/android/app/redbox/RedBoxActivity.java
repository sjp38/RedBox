package org.clc.android.app.redbox;

import java.util.ArrayList;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.DataManager.OnBlockSettingChangeListener;
import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RedBoxActivity extends Activity implements
        OnBlockSettingChangeListener {
    private static final String TAG = "RedBox";

    public static final String ID = "id";

    private ListView mNumbersListView = null;
    private NumbersListAdapter mAdapter = null;
    private LayoutInflater mLayoutInflater = null;
    private PhoneNumberEditWidget mPhoneNumberInsertWidget = null;

    private OnClickListener mNumberClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final View parent = (View) v.getParent();
            final int id = parent.getId();

            Intent blockSettingIntent = new Intent();
            blockSettingIntent.setClass(RedBoxActivity.this,
                    RedBoxBlockSettingActivity.class);
            blockSettingIntent.putExtra(ID, id);

            RedBoxActivity.this.startActivity(blockSettingIntent);

        }
    };

    private OnCheckedChangeListener mRejectCallCheckChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            final View parent = (View) view.getParent();
            final int id = parent.getId();
            DataManager.getInstance().setRejectCall(id, isChecked);
        }
    };

    private OnCheckedChangeListener mRemoveCallLogCheckChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            final View parent = (View) view.getParent();
            final int id = parent.getId();
            DataManager.getInstance().setDeleteCallLog(id, isChecked);
        }
    };

    private OnCheckedChangeListener mSendAutoSMSCheckChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            final View parent = (View) view.getParent();
            final int id = parent.getId();
            BlockSetting setting = DataManager.getInstance()
                    .getBlockSetting(id);
            DataManager.getInstance().setSendAutoSMS(id, isChecked, null);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mNumbersListView = (ListView) findViewById(R.id.numbersList);

        mAdapter = new NumbersListAdapter();
        mNumbersListView.setAdapter(mAdapter);

        mPhoneNumberInsertWidget = (PhoneNumberEditWidget) findViewById(R.id.number_input_textView);

        Context context = getApplicationContext();
        context.startService(new Intent(context, RedBoxService.class));

        DataManager.getInstance().setOnBlockSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataManager.getInstance().saveSettings();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhoneNumberEditWidget.PICK_CONTACT_REQUEST:
                mPhoneNumberInsertWidget.onContactActivityResult(resultCode,
                        data);
                break;
            default:
                break;
        }
    }

    private void addNumber(String alias, String number, boolean notify) {
        if ("".equals(number)) {
            if (notify) {
                Toast.makeText(this, R.string.error_blank_number,
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (DataManager.getInstance().isExist(number)) {
            if (notify) {
                Toast.makeText(this, R.string.error_duplicate_number,
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        BlockSetting setting = new BlockSetting(alias, number);
        DataManager.getInstance().add(setting);
        mAdapter.notifyDataSetChanged();
    }

    public void onAddNumberClicked(View v) {
        final ArrayList<BlockSetting> settings = mPhoneNumberInsertWidget
                .getBlockSettings();
        for (BlockSetting setting : settings) {
            addNumber(setting.mAlias, setting.mNumber, true);
        }
    }

    @Override
    public void onBlockSettingsChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class NumbersListAdapter extends BaseAdapter {
        NumbersListAdapter() {
            super();
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            final ArrayList<BlockSetting> settings = DataManager.getInstance()
                    .getBlockSettings();
            return settings.size();
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
            numberList.setId(position);
            TextView alias = (TextView) numberList
                    .findViewById(R.id.alias_textview);
            TextView number = (TextView) numberList
                    .findViewById(R.id.number_textview);
            CheckBox rejectCallCheckBox = (CheckBox) numberList
                    .findViewById(R.id.reject_call_checkBox);
            CheckBox removeCallLogCheckBox = (CheckBox) numberList
                    .findViewById(R.id.remove_call_log_checkBox);
            CheckBox sendAutoSMSCheckBox = (CheckBox) numberList
                    .findViewById(R.id.send_auto_sms_checkBox);

            View aliasNumberLayout = numberList
                    .findViewById(R.id.aliasNumberLayout);
            aliasNumberLayout.setOnClickListener(mNumberClickListener);

            BlockSetting setting = DataManager.getInstance().getBlockSetting(
                    position);
            if (setting.mAlias.equals("")) {
                alias.setText(setting.mNumber);
                number.setText("");
            } else {
                alias.setText(setting.mAlias);
                number.setText(setting.mNumber);
            }
            rejectCallCheckBox.setChecked(setting.mRejectCall);
            rejectCallCheckBox
                    .setOnCheckedChangeListener(mRejectCallCheckChangeListener);
            removeCallLogCheckBox.setChecked(setting.mDeleteCallLog);
            removeCallLogCheckBox
                    .setOnCheckedChangeListener(mRemoveCallLogCheckChangeListener);
            sendAutoSMSCheckBox.setChecked(setting.mSendAutoSMS);
            sendAutoSMSCheckBox
                    .setOnCheckedChangeListener(mSendAutoSMSCheckChangeListener);
            return numberList;
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getInstance().getBlockSetting(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        private void deleteNumbers(boolean blocked) {
            final ArrayList<BlockSetting> deleteSettings = new ArrayList<BlockSetting>();
            final ArrayList<BlockSetting> settings = DataManager.getInstance()
                    .getBlockSettings();
            for (BlockSetting setting : settings) {
                if (setting.mRejectCall == blocked) {
                    deleteSettings.add(setting);
                }
            }
            for (BlockSetting deleteSetting : deleteSettings) {
                DataManager.getInstance().delete(deleteSetting);
            }
            notifyDataSetChanged();
        }
    }
}