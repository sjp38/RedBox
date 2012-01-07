
package org.clc.android.app.redbox;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import org.clc.android.app.redbox.data.ActionRecord;
import org.clc.android.app.redbox.data.BlockSetting;
import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.GroupRule;
import org.clc.android.app.redbox.data.PatternSetting;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class RedBoxService extends Service {
    private static final String TAG = "RedBox_service";

    private ITelephony mTelephony = null;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                final String parsedIncomingNumber = DataManager.getParsedNumber(incomingNumber);
                if (DataManager.getInstance().isValid(parsedIncomingNumber)) {
                    // Check number rules.
                    final BlockSetting setting = DataManager.getInstance()
                            .getBlockSetting(parsedIncomingNumber);
                    if (setting != null) {
                        if (execute(setting, incomingNumber)) {
                            return;
                        }
                    }
                }

                // Check pattern rules.
                final ArrayList<PatternSetting> settings = DataManager
                        .getInstance().getPatterns();
                for (PatternSetting patternSetting : settings) {
                    if (patternSetting.matches(parsedIncomingNumber)) {
                        if (execute(patternSetting, incomingNumber)) {
                            return;
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        startPhoneStateMonitoring();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Class c = Class.forName(telephonyManager.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);

            mTelephony = (ITelephony) m.invoke(telephonyManager);
        } catch (Throwable e) {
            Log.e(TAG, "Fail to end call!!!", e);

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean execute(BlockSetting setting, String incomingNumber) {
        boolean worked = false;
        if (setting.mRejectCall) {
            endCall();
            worked = true;
        }
        if (setting.mDeleteCallLog) {
            deleteCallLog(incomingNumber);
            worked = true;
        }
        if (setting.mSendAutoSMS) {
            sendSMS(incomingNumber, setting.mAutoSMS);
            worked = true;
        }

        if (!worked) {
            return worked;
        }
        try {
            BlockSetting copiedSetting = (BlockSetting) setting.clone();
            if (setting instanceof PatternSetting || setting instanceof GroupRule) {
                copiedSetting.mNumber = incomingNumber;
            }
            ActionRecord record = new ActionRecord(System.currentTimeMillis(),
                    copiedSetting);
            DataManager.getInstance().addHistory(record);
            final Intent historyIntent = new Intent(this,
                    RedBoxHistoryActivity.class);
            historyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(historyIntent);
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "Failed to copy block setting for history." + e);
        }
        return worked;
    }

    private void startPhoneStateMonitoring() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void stopPhoneStateMonitoring() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_NONE);
    }

    private void endCall() {
        try {
            mTelephony.endCall();
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception while end call!", e);
        }
    }

    public static final String[] CALL_PROJECTION = {
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION
    };

    private static final int DEL_CALL_LOG_MESSAGE = 1;
    private static final long CALL_LOG_CREATION_WAIT_TIME = 2000;
    private static final long RECENT_CALL_LOG_JUDGE_CRITERIA = 4000;
    private static final int CALL_LOG_CREATION_WAIT_TRY_LIMIT = 3;

    private void deleteCallLog(String number) {
        Message msg = new Message();
        msg.what = DEL_CALL_LOG_MESSAGE;
        msg.arg1 = 0;
        msg.obj = number;
        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String number = (String) msg.obj;
                Uri callLogUri = CallLog.Calls.CONTENT_URI;
                String querySelection = CallLog.Calls.NUMBER + "='" + number
                        + "'";
                if (Build.MODEL.equals("SHW-M250S")
                        || Build.MODEL.equals("SHW-M250K")) {
                    callLogUri = Uri.parse("content://logs/call");
                    querySelection = null;
                }
                Cursor cursor = getContentResolver().query(callLogUri,
                        CALL_PROJECTION, querySelection, null,
                        CallLog.Calls.DEFAULT_SORT_ORDER);

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndex(CallLog.Calls._ID));
                    long date = cursor.getLong(cursor
                            .getColumnIndex(CallLog.Calls.DATE));
                    // If not recently created log, don't delete.
                    if (System.currentTimeMillis() - date > RECENT_CALL_LOG_JUDGE_CRITERIA) {
                        if (msg.arg1++ < CALL_LOG_CREATION_WAIT_TRY_LIMIT) {
                            Message newMsg = new Message();
                            newMsg.what = msg.what;
                            newMsg.arg1 = msg.arg1;
                            newMsg.obj = msg.obj;
                            sendMessageDelayed(newMsg,
                                    CALL_LOG_CREATION_WAIT_TIME);
                        }
                    } else {
                        getContentResolver().delete(callLogUri,
                                CallLog.Calls._ID + "=" + Integer.toString(id),
                                null);
                    }
                }
                cursor.close();

            }
        }.sendMessageDelayed(msg, CALL_LOG_CREATION_WAIT_TIME);

    }

    private void sendSMS(String number, String msg) {
        if ("".equals(number) || "".equals(msg)) {
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> msgs = smsManager.divideMessage(msg);
        for (String dividedMsg : msgs) {
            smsManager.sendTextMessage(number, null, dividedMsg, null, null);
        }
    }
}
