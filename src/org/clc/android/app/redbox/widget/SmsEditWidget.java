
package org.clc.android.app.redbox.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.clc.android.app.redbox.R;
import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;

import java.util.ArrayList;

public class SmsEditWidget extends LinearLayout {
    private EditText mSmsEditText;
    private Button mMenuButton;

    private View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final ArrayList<CharSequence> totalItems = new ArrayList<CharSequence>();
            for (int i = 0; i < DataManager.getInstance().getSize(); i++) {
                BlockSetting setting = DataManager.getInstance().get(i);
                if (!"".equals(setting.mAutoSMS)) {
                    totalItems.add(setting.mAutoSMS);
                }
            }

            final CharSequence[] items;
            items = getContext().getResources().getTextArray(
                    R.array.auto_sms_preset);
            for (int i = 0; i < items.length; i++) {
                totalItems.add(0, items[i]);
            }
            final CharSequence[] totalArray = totalItems
                    .toArray(new CharSequence[totalItems.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setItems(totalArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String insertText = mSmsEditText.getText().toString();
                    if (!"".equals(insertText)) {
                        insertText += "\n";
                    }
                    mSmsEditText.setText(insertText + totalArray[which]);
                    mSmsEditText.setSelection(insertText.length() + totalArray[which].length());
                }
            });
            builder.show();
        }
    };

    public SmsEditWidget(Context context) {
        super(context);
        initViews(context);
    }

    public SmsEditWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sms_edit_widget_layout, this, true);

        mSmsEditText = (EditText) findViewById(R.id.sms_editText);
        mMenuButton = (Button) findViewById(R.id.sms_edit_menu_button);

        mMenuButton.setOnClickListener(mMenuButtonClickListener);
    }

    public void setText(String text) {
        mSmsEditText.setText(text);
    }

    public Editable getText() {
        return mSmsEditText.getText();
    }
}
