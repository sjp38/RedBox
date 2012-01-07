
package org.clc.android.app.redbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.GroupRule;
import org.clc.android.app.redbox.widget.PhoneNumberEditWidget;
import org.clc.android.app.redbox.widget.SmsEditWidget;

import java.util.ArrayList;

public class RedBoxGroupSettingActivity extends Activity {
    private static final String TAG = "RedBox_group_settingActivity";

    private LayoutInflater mLayoutInflater = null;
    private TextView mAliasTextView;
    private PhoneNumberEditWidget mPhoneNumberEditor;
    private CheckBox mRejectCallCheckBox;
    private CheckBox mDeleteCallLogCheckBox;
    private CheckBox mSendAutoSMSCheckBox;
    private SmsEditWidget mAutoSMSEditWidget;

    private ArrayList<BlockSetting> mMembers = new ArrayList<BlockSetting>();

    private int mId = -1;

    View.OnClickListener mMemberDeleteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final View memberView = (View) v.getParent();
            final BlockSetting member = (BlockSetting) memberView.getTag();
            memberView.setTag(null);
            mMembers.remove(member);
            final LinearLayout list = (LinearLayout) memberView.getParent();
            final int index = list.indexOfChild(memberView);
            list.removeView(memberView);

            if (list.getChildCount() > 0) {
                if (index == 0) {
                    list.removeViewAt(0);
                } else if (index > 0) {
                    list.removeViewAt(index - 1);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_redboxgroupsettingactivity);

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
        GroupRule loadedGroup = null;
        if (mId == -1) {
            loadedGroup = new GroupRule("", false, false, false, "", new ArrayList<BlockSetting>());
        } else {
            loadedGroup = (GroupRule) DataManager.getInstance().get(mId);
        }
        final GroupRule currentGroup = getCurrentSetting();

        if (loadedGroup.toString().equals(currentGroup.toString())) {
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
        mAliasTextView = (TextView) findViewById(R.id.group_setting_alias);
        mPhoneNumberEditor = (PhoneNumberEditWidget) findViewById(R.id.group_setting_number_input_widget);
        mPhoneNumberEditor
                .setOnNumberSelectedListener(new PhoneNumberEditWidget.OnNumberSelectedListener() {
                    @Override
                    public void onNumberSelected() {
                        onAddNumberClicked(null);
                    }
                });

        mAliasTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAliasEditDialog();
            }
        });

        final Button deleteButton = (Button) findViewById(R.id.delete);
        mRejectCallCheckBox = (CheckBox) findViewById(R.id.rejectCallcheckBox);
        mDeleteCallLogCheckBox = (CheckBox) findViewById(R.id.deleteCallLogCheckBox);
        mSendAutoSMSCheckBox = (CheckBox) findViewById(R.id.sendAutoSMSCheckBox);
        mAutoSMSEditWidget = (SmsEditWidget) findViewById(R.id.autoSMSeditText);

        if (mId == -1) {
            deleteButton.setEnabled(false);
        } else {
            GroupRule rule = (GroupRule) DataManager.getInstance().get(mId);
            mAliasTextView.setText(rule.mAlias);

            mRejectCallCheckBox.setChecked(rule.mRejectCall);
            mDeleteCallLogCheckBox.setChecked(rule.mDeleteCallLog);
            mSendAutoSMSCheckBox.setChecked(rule.mSendAutoSMS);
            mAutoSMSEditWidget.setText(rule.mAutoSMS);

            final ArrayList<BlockSetting> members = rule.getMembers();
            for (BlockSetting member : members) {
                addMemberList(member, false);
            }
            mMembers = (ArrayList<BlockSetting>) members.clone();
        }
    }

    public void onAddNumberClicked(View v) {
        final ArrayList<BlockSetting> members = mPhoneNumberEditor
                .getBlockSettings();
        mPhoneNumberEditor.setText("");
        for (BlockSetting member : members) {
            mMembers.add(member);
            addMemberList(member, true);
        }
    }

    private void addMemberList(final BlockSetting member, boolean isUserAction) {
        if (isUserAction && DataManager.getInstance().isExist(member.mParsedNumber)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_duplicate_number_will_overwrite);
            builder.setPositiveButton(R.string.dialog_positive_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataManager.getInstance().remove(member.mParsedNumber);
                            addMemberList(member, true);
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton(R.string.dialog_negative_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
            return;
        }
        LinearLayout membersList = (LinearLayout) findViewById(R.id.group_setting_members_list);
        View memberView = mLayoutInflater.inflate(R.layout.number_list,
                membersList, false);
        memberView.setTag(member);

        final View rejectCallCheckBox = memberView.findViewById(R.id.reject_call_checkBox);
        final View removeCallLogCheckBox = memberView.findViewById(R.id.remove_call_log_checkBox);
        final View sendAutoSMSCheckBox = memberView.findViewById(R.id.send_auto_sms_checkBox);
        rejectCallCheckBox.setVisibility(View.GONE);
        removeCallLogCheckBox.setVisibility(View.GONE);
        sendAutoSMSCheckBox.setVisibility(View.GONE);

        View deleteButton = memberView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(mMemberDeleteButtonClickListener);

        TextView alias = (TextView) memberView.findViewById(R.id.alias_textview);
        TextView number = (TextView) memberView.findViewById(R.id.number_textview);

        if (member.mAlias.equals("")) {
            alias.setText(member.mNumber);
            number.setText("");
        } else {
            alias.setText(member.mAlias);
            number.setText(member.mNumber);
        }
        if (membersList.getChildCount() > 0) {
            View listDivider = mLayoutInflater.inflate(R.layout.list_divider,
                    membersList);
        }
        membersList.addView(memberView);
    }

    private void showAliasEditDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        final TextView target = (TextView) findViewById(R.id.group_setting_alias);
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

    private GroupRule getCurrentSetting() {
        final String alias = mAliasTextView.getText().toString();
        final ArrayList<BlockSetting> members = mMembers;

        final boolean rejectCall = mRejectCallCheckBox.isChecked();
        final boolean deleteCallLog = mDeleteCallLogCheckBox.isChecked();
        final boolean sendAutoSMS = mSendAutoSMSCheckBox.isChecked();
        final String autoSMS = mAutoSMSEditWidget.getText().toString();

        return new GroupRule(alias, rejectCall, deleteCallLog, sendAutoSMS, autoSMS, members);
    }

    public void onSaveButtonClicked(View v) {
        final GroupRule group = getCurrentSetting();
        if (mId == -1) {
            DataManager.getInstance().add(group);
        } else {
            DataManager.getInstance().update(mId, group);
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
}
