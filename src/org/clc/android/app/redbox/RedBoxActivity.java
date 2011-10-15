package org.clc.android.app.redbox;

import java.util.ArrayList;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.DataManager.OnBlockSettingChangeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RedBoxActivity extends Activity implements
        OnBlockSettingChangeListener {
    private static final String TAG = "RedBox";

    public static final String ID = "id";

    private static final int PICK_CONTACT_REQUEST = 1;

    private ListView mNumbersListView = null;
    private NumbersListAdapter mAdapter = null;
    private LayoutInflater mLayoutInflater = null;

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

        Context context = getApplicationContext();
        context.startService(new Intent(context, RedBoxService.class));

        DataManager.getInstance().setOnBlockSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataManager.getInstance().saveSettings();
    }

    private void showNumberSelectionDialog(final String name,
            final CharSequence[] numbers) {
        final boolean[] checked = new boolean[numbers.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_select_number);
        builder.setMultiChoiceItems(numbers, null,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        checked[which] = isChecked;
                    }
                });
        builder.setPositiveButton(R.string.add_number_button,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < numbers.length; i++) {
                            if (checked[i]) {
                                addNumber(name, numbers[i].toString());
                            }
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_CONTACT_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.error_while_pick_contact,
                            Toast.LENGTH_SHORT).show();
                    return;

                }
                final Cursor cursor = managedQuery(data.getData(), null, null,
                        null, null);
                boolean numberExist = false;
                while (cursor.moveToNext()) {
                    final String name = cursor
                            .getString(cursor
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    final String contactId = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    final String hasPhone = cursor
                            .getString(cursor
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        numberExist = true;
                        final Cursor phones = getContentResolver()
                                .query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = " + contactId, null, null);

                        final CharSequence[] phoneNumbers = new CharSequence[phones
                                .getCount()];
                        while (phones.moveToNext()) {
                            phoneNumbers[phones.getPosition()] = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        phones.close();
                        showNumberSelectionDialog(name, phoneNumbers);
                    }
                }
                cursor.close();
                if (!numberExist) {
                    Toast.makeText(this, R.string.error_blank_contact,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }

    public void onGetNumberFromContactsClicked(View v) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    public void onGetNumberFromCallLogClicked(View v) {
        Uri callLogUri = CallLog.Calls.CONTENT_URI;
        if (Build.MODEL.equals("SHW-M250S") || Build.MODEL.equals("SHW-M250K")) {
            callLogUri = Uri.parse("content://logs/call");
        }
        final Cursor cursor = getContentResolver().query(callLogUri,
                RedBoxService.CALL_PROJECTION, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 20");
        final CharSequence[] numbers = new CharSequence[cursor.getCount()];
        while (cursor.moveToNext()) {
            numbers[cursor.getPosition()] = cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.NUMBER));
        }
        cursor.close();
        this.showNumberSelectionDialog("", numbers);
    }

    private void addNumber(String alias, String number) {
        if ("".equals(number)) {
            Toast.makeText(this, R.string.error_blank_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (DataManager.getInstance().isExist(number)) {
            Toast.makeText(this, R.string.error_duplicate_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        BlockSetting setting = new BlockSetting(alias, number);
        DataManager.getInstance().add(setting);
        mAdapter.notifyDataSetChanged();
    }

    public void onAddNumberClicked(View v) {
        EditText textView = (EditText) findViewById(R.id.number_input_textView);
        String number = textView.getText().toString();
        textView.setText("");
        addNumber("", number);
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