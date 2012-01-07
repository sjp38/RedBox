
package org.clc.android.app.redbox.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clc.android.app.redbox.R;
import org.clc.android.app.redbox.RedBoxSuggestionActivity;

public class SmsEditWidget extends LinearLayout implements TextWatcher {
    private static final int MAX_MESSAGE_LENGTH = 280;
    public static final int RESULT_FOR_SUGGESTION = 2;

    private EditText mSmsEditText;
    private ImageButton mMenuButton;
    private TextView mCurrentLengthTextView;

    private View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final Intent intent = new Intent();
            intent.setClassName("org.clc.android.app.redbox",
                    "org.clc.android.app.redbox.RedBoxSuggestionActivity");
            ((Activity) getContext()).startActivityForResult(intent, RESULT_FOR_SUGGESTION);
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
        mMenuButton = (ImageButton) findViewById(R.id.sms_edit_menu_button);
        mCurrentLengthTextView = (TextView) findViewById(R.id.sms_textLength);

        mSmsEditText.addTextChangedListener(this);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH);
        mSmsEditText.setFilters(filterArray);
        mMenuButton.setOnClickListener(mMenuButtonClickListener);
    }

    public void setText(String text) {
        mSmsEditText.setText(text);
    }

    public Editable getText() {
        return mSmsEditText.getText();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final int currentLength = mSmsEditText.getText().toString().length();
        mCurrentLengthTextView.setText(currentLength + "/" + MAX_MESSAGE_LENGTH);
    }

    public void onSuggestionPicked(int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }
        final String suggestion = data.getStringExtra(RedBoxSuggestionActivity.SUGGESTION_EXTRA);
        final int selectionEnd = mSmsEditText.getSelectionEnd();
        final String formerText = mSmsEditText.getText().toString();
        final String next = formerText.substring(0, selectionEnd) + suggestion
                + formerText.substring(selectionEnd, formerText.length());
        mSmsEditText.setText(next);

    }
}
