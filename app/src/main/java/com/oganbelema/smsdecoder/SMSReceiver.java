package com.oganbelema.smsdecoder;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * This is a broadcast receiver to receive SMS messages and check if it is
 * from a specified contact and if it is start the SMSReceiverActivity
 * @author Belema Ogan
 */
public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = SMSReceiver.class.getSimpleName();

    public static final String PDU_TYPE = "pdus";

    public static final String SMS_BODY_KEY = "message_key";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message.
        Bundle bundle = intent.getExtras();

        if (bundle != null){
            processMessage(context, bundle);
        }
    }

    /**
     * Processes the messages contained in the received bundle
     * @param context application's context
     * @param bundle the extras in the received intent
     */
    @SuppressLint("NewApi")
    private void processMessage(Context context, Bundle bundle) {
        SmsMessage[] smsMessages;
        String format = bundle.getString("format");

        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(PDU_TYPE);
        if (pdus != null) {

            // Check the Android version.
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

            // Fill the smsMessages array.
            smsMessages = new SmsMessage[pdus.length];
            for (int i = 0; i < smsMessages.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                String senderNumber = smsMessages[i].getOriginatingAddress();
                String messageBody = smsMessages[i].getMessageBody();

                if (isRequiredContact(context, senderNumber)){
                    startSMSReceiverActivity(context, messageBody);
                }

            }
        }
    }

    /**
     * Checks if the contact of the sms sender is the required contact
     * @param context used to create a cursor object to query contact list
     * @param senderNumber the number of the sender. Used to build the uri to query the contact list
     * @return true if contact is required contact or false if it isn't or number is not saved
     */
    private boolean isRequiredContact(Context context, String senderNumber){
        String contactName = "";

        //Resolving the contact name from the contacts.
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(senderNumber));
        Cursor cursor = context.getContentResolver().query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME},null,null,null);

        if (cursor != null){
            try {
                cursor.moveToFirst();
                contactName = cursor.getString(0);
                Toast.makeText(context, contactName, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error reading contact", e);
            }finally{
                cursor.close();
            }
        }

        return contactName.equals(context.getString(R.string.ven_ten_contact));
    }

    /**
     * Starts the SMSReceiverActivity with the received sms body
     * @param context used to create intent and start activity
     * @param messageBody the body of the sms message received
     */
    private void startSMSReceiverActivity(Context context, String messageBody) {
        Intent startMainActivityIntent = new Intent(context, SMSReceiverActivity.class);
        startMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startMainActivityIntent.putExtra(SMS_BODY_KEY, messageBody);
        context.startActivity(startMainActivityIntent);
    }
}
