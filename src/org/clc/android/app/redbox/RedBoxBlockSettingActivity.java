
package org.clc.android.app.redbox;

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

import com.example.android.actionbarcompat.ActionBarActivity;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;
import org.clc.android.app.redbox.widget.SmsEditWidget;

public class RedBoxBlockSettingActivity extends ActionBarActivity implements
        View.OnClickListener {
    private static final String TAG = "RedBox block setting";
    private int mId = 0;
    private PhoneNumberEditWidget mPhoneNumberEditor = null;
    private SmsEditWidget mSmsEditWidget = null;

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
            case SmsEditWidget.RESULT_FOR_SUGGESTION:
                if (mSmsEditWidget != null) {
                    mSmsEditWidget.onSuggestionPicked(resultCode, data);
                }
            default:
                break;
        }
    }

    private void initViews() {
        final int id = mId;

        BlockSetting setting = DataManager.getInstance().get(id);

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

        mSmsEditWidget = (SmsEditWidget) findViewById(R.id.autoSMSeditText);
        if (!setting.mAutoSMS.equals("")) {
            mSmsEditWidget.setText(setting.mAutoSMS);
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

    private BlockSetting getCurrentSetting() {
        final TextView alias = (TextView) findViewById(R.id.alias_textView);
        final TextView number = (TextView) findViewById(R.id.number_textView);

        final CheckBox rejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
        final CheckBox deleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
        final CheckBox sendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);

        final String aliasValue = alias.getText().toString();
        final String numberValue = number.getText().toString();
        final boolean rejectCallValue = rejectCallCheckBox.isChecked();
        final boolean deleteCallLogValue = deleteCallLogCheckBox.isChecked();
        final boolean sendAutoSMSValue = sendAutoSMSCheckBox.isChecked();
        final String autoSMSValue = mSmsEditWidget.getText().toString();

        return new BlockSetting(aliasValue, numberValue,
                rejectCallValue, deleteCallLogValue, sendAutoSMSValue,
                autoSMSValue);
    }

    @Override
    public void onBackPressed() {
        final BlockSetting loadedSetting = DataManager.getInstance().get(mId);
        final BlockSetting currentSetting = getCurrentSetting();

        if (loadedSetting.toString().equals(currentSetting.toString())) {
            super.onBackPressed();
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_changes_before_closing);
        builder.setPositiveButton(R.string.save_and_quit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveButtonClicked(null);
                    }
                });
        builder.setNegativeButton(R.string.quit_without_saving,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDiscardButtonClicked(null);
                    }
                });
        builder.show();
    }

    public void onSaveButtonClicked(final View v) {
        DataManager.getInstance().update(mId, getCurrentSetting());

        finish();
    }

    public void onDiscardButtonClicked(final View v) {
        finish();
    }

    public void onDeleteButtonClicked(final View v) {
        DataManager.getInstance().remove(mId);
        finish();
    }
}
