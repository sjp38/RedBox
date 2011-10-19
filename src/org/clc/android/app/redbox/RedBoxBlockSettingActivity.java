package org.clc.android.app.redbox;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RedBoxBlockSettingActivity extends Activity implements
        View.OnClickListener {
    private static final String TAG = "RedBox block setting";
    private int mId = 0;
    private PhoneNumberEditWidget mPhoneNumberEditor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_redboxsettingactivity);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        mId = extras.getInt(RedBoxActivity.ID, 0);

        initViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhoneNumberEditWidget.PICK_CONTACT_REQUEST:
                if (mPhoneNumberEditor != null) {
                    mPhoneNumberEditor
                            .onContactActivityResult(resultCode, data);
                } else {
                    Log.e(TAG, "phone number editor is null!!",
                            new RuntimeException(""));
                }
                break;
            default:
                break;
        }
    }

    private void initViews() {
        final int id = mId;

        BlockSetting setting = DataManager.getInstance().getBlockSetting(id);

        final TextView alias = (TextView) findViewById(R.id.alias_textView);
        final TextView number = (TextView) findViewById(R.id.number_textView);
        alias.setText(setting.mAlias);
        number.setText(setting.mNumber);
        alias.setOnClickListener(this);
        number.setOnClickListener(this);

        final CheckBox rejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
        final CheckBox deleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
        final CheckBox sendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);
        rejectCallCheckBox.setChecked(setting.mRejectCall);
        deleteCallLogCheckBox.setChecked(setting.mDeleteCallLog);
        sendAutoSMSCheckBox.setChecked(setting.mSendAutoSMS);

        if (!setting.mAutoSMS.equals("")) {
            final EditText autoSMS = (EditText) findViewById(R.id.autoSMSeditText);
            autoSMS.setText(setting.mAutoSMS);
        }
    }

    private void showTextSettingDialog(final int title, final int id) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final TextView target = (TextView) findViewById(id);
        final String writed = target.getText().toString();
        final PhoneNumberEditWidget phoneNumberEditor = new PhoneNumberEditWidget(
                this, true);

        input.setText(writed);
        phoneNumberEditor.setText(writed);
        builder.setTitle(title);
        if (id == R.id.number_textView) {
            builder.setView(phoneNumberEditor);
            mPhoneNumberEditor = phoneNumberEditor;
        } else {
            builder.setView(input);
        }
        builder.setPositiveButton(R.string.dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = null;
                        if (id == R.id.number_textView) {
                            text = phoneNumberEditor.getText().toString();
                        } else {
                            text = input.getText().toString();
                        }
                        if ("".equals(text) && id == R.id.number_textView) {
                            Toast.makeText(RedBoxBlockSettingActivity.this,
                                    R.string.error_blank_number,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        target.setText(text);
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative_button,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.alias_textView:
                showTextSettingDialog(R.string.setting_alias_dialog_title,
                        R.id.alias_textView);
                break;
            case R.id.number_textView:
                showTextSettingDialog(R.string.setting_number_dialog_title,
                        R.id.number_textView);
                break;
        }

    }

    public void onSaveButtonClicked(View v) {
        final TextView alias = (TextView) findViewById(R.id.alias_textView);
        final TextView number = (TextView) findViewById(R.id.number_textView);

        final CheckBox rejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
        final CheckBox deleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
        final CheckBox sendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);
        final EditText autoSMS = (EditText) findViewById(R.id.autoSMSeditText);

        String aliasValue = alias.getText().toString();
        String numberValue = number.getText().toString();
        boolean rejectCallValue = rejectCallCheckBox.isChecked();
        boolean deleteCallLogValue = deleteCallLogCheckBox.isChecked();
        boolean sendAutoSMSValue = sendAutoSMSCheckBox.isChecked();
        String autoSMSValue = autoSMS.getText().toString();

        BlockSetting setting = new BlockSetting(aliasValue, numberValue,
                rejectCallValue, deleteCallLogValue, sendAutoSMSValue,
                autoSMSValue);
        DataManager.getInstance().setSetting(mId, setting);

        finish();
    }

    public void onDiscardButtonClicked(View v) {
        finish();
    }

    public void onDeleteButtonClicked(View v) {
        DataManager.getInstance().delete(mId);
        finish();
    }
}
