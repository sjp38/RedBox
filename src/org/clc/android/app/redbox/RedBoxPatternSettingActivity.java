
package org.clc.android.app.redbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.actionbarcompat.ActionBarActivity;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.PatternSetting;
import org.clc.android.app.redbox.data.PatternSetting.ExceptionNumber;
import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;
import org.clc.android.app.redbox.widget.SmsEditWidget;

import java.util.ArrayList;

public class RedBoxPatternSettingActivity extends ActionBarActivity {

    private TextView mAliasTextView = null;
    private EditText mStartWithEditText = null;
    private EditText mEndWithEditText = null;
    private CheckBox mAllCheckBox = null;
    private PhoneNumberEditWidget mPhoneNumberEditor = null;

    private int mId = -1;
    private ArrayList<ExceptionNumber> mExceptions = new ArrayList<ExceptionNumber>();
    private LayoutInflater mLayoutInflater = null;

    private View.OnClickListener mAliasClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAliasEditDialog();
        }
    };
    private CompoundButton.OnCheckedChangeListener mAllCheckBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {
                mStartWithEditText.setEnabled(false);
                mEndWithEditText.setEnabled(false);
            } else {
                mStartWithEditText.setEnabled(true);
                mEndWithEditText.setEnabled(true);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_redboxpatternsettingactivity);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getInt(RedBoxActivity.ID, -1);
        }
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PhoneNumberEditWidget.PICK_CONTACT_REQUEST:
                mPhoneNumberEditor.onContactActivityResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        PatternSetting loadedSetting = null;
        if (mId == -1) {
            loadedSetting = new PatternSetting("", "", "", false, null,
                    false, false, false, "");
        } else {
            loadedSetting = (PatternSetting) DataManager.getInstance().get(mId);
        }
        final PatternSetting currentSetting = getCurrentSetting();

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

    private void initViews() {
        mAliasTextView = (TextView) findViewById(R.id.pattern_setting_alias);
        mStartWithEditText = (EditText) findViewById(R.id.pattern_setting_startWith_input);
        mEndWithEditText = (EditText) findViewById(R.id.pattern_setting_endWith_input);
        mAllCheckBox = (CheckBox) findViewById(R.id.pattern_setting_all_checkbox);
        mPhoneNumberEditor = (PhoneNumberEditWidget) findViewById(R.id.number_input_textView);

        mAliasTextView.setOnClickListener(mAliasClickListener);
        mAllCheckBox.setOnCheckedChangeListener(mAllCheckBoxListener);

        mPhoneNumberEditor.setNoAddPatternMenu(true);

        final Button deleteButton = (Button) findViewById(R.id.delete);
        if (mId == -1) {
            deleteButton.setEnabled(false);
        } else {
            PatternSetting setting = (PatternSetting) DataManager.getInstance()
                    .get(mId);
            mAliasTextView.setText(setting.mAlias);
            mStartWithEditText.setText(setting.mStartPattern);
            mEndWithEditText.setText(setting.mEndPattern);
            mAllCheckBox.setChecked(setting.mAllNumber);

            final CheckBox rejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
            final CheckBox deleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
            final CheckBox sendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);
            final SmsEditWidget autoSMSEditText = (SmsEditWidget) findViewById(R.id.autoSMSeditText);

            rejectCallCheckBox.setChecked(setting.mRejectCall);
            deleteCallLogCheckBox.setChecked(setting.mDeleteCallLog);
            sendAutoSMSCheckBox.setChecked(setting.mSendAutoSMS);
            autoSMSEditText.setText(setting.mAutoSMS);

            final ArrayList<ExceptionNumber> exceptions = setting.mExceptions;
            for (ExceptionNumber exception : exceptions) {
                addExceptionList(exception);
            }
            mExceptions = (ArrayList<ExceptionNumber>) exceptions.clone();
        }
    }

    public void addExceptionList(ExceptionNumber exception) {
        LinearLayout exceptionsGroup = (LinearLayout) findViewById(R.id.pattern_setting_exceptions_group);
        View exceptionView = mLayoutInflater.inflate(R.layout.number_list,
                exceptionsGroup, false);
        exceptionView.setTag(exception);

        final View rejectCallCheckBox = exceptionView
                .findViewById(R.id.reject_call_checkBox);
        final View removeCallLogCheckBox = exceptionView
                .findViewById(R.id.remove_call_log_checkBox);
        final View sendAutoSMSCheckBox = exceptionView
                .findViewById(R.id.send_auto_sms_checkBox);
        rejectCallCheckBox.setVisibility(View.GONE);
        removeCallLogCheckBox.setVisibility(View.GONE);
        sendAutoSMSCheckBox.setVisibility(View.GONE);

        View deleteButton = exceptionView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(mDeleteButtonClickListener);

        TextView alias = (TextView) exceptionView
                .findViewById(R.id.alias_textview);
        TextView number = (TextView) exceptionView
                .findViewById(R.id.number_textview);

        if (exception.mAlias.equals("")) {
            alias.setText(exception.mNumber);
            number.setText("");
        } else {
            alias.setText(exception.mAlias);
            number.setText(exception.mNumber);
        }
        if (exceptionsGroup.getChildCount() > 0) {
            View listDivider = mLayoutInflater.inflate(R.layout.list_divider,
                    exceptionsGroup);
        }
        exceptionsGroup.addView(exceptionView);
    }

    public void onAddNumberClicked(View v) {
        final ArrayList<BlockSetting> exceptions = mPhoneNumberEditor
                .getBlockSettings();
        mPhoneNumberEditor.setText("");
        for (BlockSetting setting : exceptions) {
            final ExceptionNumber exception = new ExceptionNumber(
                    setting.mAlias, setting.mNumber);
            mExceptions.add(exception);
            addExceptionList(exception);
        }
    }

    View.OnClickListener mDeleteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final View exceptionView = (View) v.getParent();
            final ExceptionNumber exception = (ExceptionNumber) exceptionView
                    .getTag();
            exceptionView.setTag(null);
            mExceptions.remove(exception);
            final LinearLayout group = (LinearLayout) exceptionView.getParent();
            final int index = group.indexOfChild(exceptionView);
            group.removeView(exceptionView);

            if (group.getChildCount() > 0) {
                if (index == 0) {
                    group.removeViewAt(0);
                } else if (index > 0) {
                    group.removeViewAt(index - 1);
                }
            }
        }
    };

    private PatternSetting getCurrentSetting() {
        final String alias = mAliasTextView.getText().toString();
        final String startWith = mStartWithEditText.getText().toString();
        final String endWith = mEndWithEditText.getText().toString();
        final boolean allNumber = mAllCheckBox.isChecked();
        final ArrayList<ExceptionNumber> exceptions = mExceptions;

        final CheckBox rejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
        final CheckBox deleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
        final CheckBox sendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);
        final SmsEditWidget autoSMSEditText = (SmsEditWidget) findViewById(R.id.autoSMSeditText);

        final boolean rejectCall = rejectCallCheckBox.isChecked();
        final boolean deleteCallLog = deleteCallLogCheckBox.isChecked();
        final boolean sendAutoSMS = sendAutoSMSCheckBox.isChecked();
        final String autoSMS = autoSMSEditText.getText().toString();

        return new PatternSetting(alias, startWith,
                endWith, allNumber, exceptions, rejectCall, deleteCallLog,
                sendAutoSMS, autoSMS);
    }

    public void onSaveButtonClicked(View v) {
        final PatternSetting setting = getCurrentSetting();
        if (mId == -1) {
            DataManager.getInstance().add(setting);
        } else {
            DataManager.getInstance().update(mId, setting);
        }
        finish();
    }

    public void onDiscardButtonClicked(View v) {
        finish();
    }

    public void onDeleteButtonClicked(View v) {
        DataManager.getInstance().remove(mId);
        finish();
    }

    private void showAliasEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final TextView target = (TextView) findViewById(R.id.pattern_setting_alias);
        final String writed = target.getText().toString();

        input.setText(writed);
        builder.setTitle(R.string.pattern_setting_alias_dialog_title);
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String text = input.getText().toString();
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
}
