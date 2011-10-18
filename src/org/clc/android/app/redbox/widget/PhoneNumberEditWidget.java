package org.clc.android.app.redbox.widget;

import java.util.ArrayList;

import org.clc.android.app.redbox.R;
import org.clc.android.app.redbox.RedBoxService;
import org.clc.android.app.redbox.data.BlockSetting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PhoneNumberEditWidget extends LinearLayout {
    private static final String PHONE_NUMBER_SEPERATOR = ",";
    private static final String ALIAS_SEPERATOR = ":";
    private static final String CONTACT_SEPERATOR = ";";
    public static final int PICK_CONTACT_REQUEST = 1;

    private static final int FROM_CONTACT_MENU = 0;
    private static final int FROM_CALL_LOG_MENU = 1;

    private EditText mPhonenumberEditText = null;
    private Button mMenuButton = null;

    private View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final CharSequence[] items = getContext().getResources()
                    .getTextArray(R.array.menu_list_phonenumber_edit_widget);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case FROM_CONTACT_MENU:
                            onGetNumberFromContactsClicked();
                            break;
                        case FROM_CALL_LOG_MENU:
                            onGetNumberFromCallLogClicked();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }
    };

    public PhoneNumberEditWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.phonenumber_edit_widget_layout, this, true);

        mPhonenumberEditText = (EditText) findViewById(R.id.phonenumber_editText);
        mMenuButton = (Button) findViewById(R.id.phonenumber_edit_menu_button);

        mMenuButton.setOnClickListener(mMenuButtonClickListener);
    }

    private void showNumberSelectionDialog(final String name,
            final CharSequence[] numbers) {
        final boolean[] checked = new boolean[numbers.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        String addNumber = "";
                        for (int i = 0; i < numbers.length; i++) {
                            if (checked[i]) {
                                if (!"".equals(addNumber)) {
                                    addNumber += PHONE_NUMBER_SEPERATOR;
                                } else {
                                    addNumber += name + ALIAS_SEPERATOR;
                                }
                                addNumber += numbers[i];
                            }
                        }
                        addNumber += CONTACT_SEPERATOR;
                        mPhonenumberEditText.setText(addNumber);
                    }
                });
        builder.create().show();
    }

    public void onContactActivityResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getContext(), R.string.error_while_pick_contact,
                    Toast.LENGTH_SHORT).show();
            return;

        }
        final Cursor cursor = ((Activity) getContext()).managedQuery(data
                .getData(), null, null, null, null);
        boolean numberExist = false;
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            final String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            final String hasPhone = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1")) {
                numberExist = true;
                final Cursor phones = getContext().getContentResolver().query(
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
            Toast.makeText(getContext(), R.string.error_blank_contact,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onGetNumberFromContactsClicked() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        ((Activity) getContext()).startActivityForResult(pickContactIntent,
                PICK_CONTACT_REQUEST);
    }

    public void onGetNumberFromCallLogClicked() {
        Uri callLogUri = CallLog.Calls.CONTENT_URI;
        if (Build.MODEL.equals("SHW-M250S") || Build.MODEL.equals("SHW-M250K")) {
            callLogUri = Uri.parse("content://logs/call");
        }
        final Cursor cursor = getContext().getContentResolver().query(
                callLogUri, RedBoxService.CALL_PROJECTION, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 20");
        final CharSequence[] numbers = new CharSequence[cursor.getCount()];
        while (cursor.moveToNext()) {
            numbers[cursor.getPosition()] = cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.NUMBER));
        }
        cursor.close();
        this.showNumberSelectionDialog("", numbers);
    }

    public ArrayList<BlockSetting> getBlockSettings() {
        final ArrayList<BlockSetting> settings = new ArrayList<BlockSetting>();
        final String inputText = mPhonenumberEditText.getText().toString();
        final String[] contacts = inputText.split(CONTACT_SEPERATOR);
        for (final String contact : contacts) {
            final String[] aliasAndNumbers = contact.split(ALIAS_SEPERATOR);
            String alias = "";
            String[] numbers = null;
            if (aliasAndNumbers.length > 1) {
                alias = aliasAndNumbers[0];
                numbers = aliasAndNumbers[1].split(PHONE_NUMBER_SEPERATOR);
            } else {
                numbers = aliasAndNumbers[0].split(PHONE_NUMBER_SEPERATOR);
            }
            for (String number : numbers) {
                final BlockSetting setting = new BlockSetting(alias, number);
                settings.add(setting);
            }
        }
        return settings;
    }

}
