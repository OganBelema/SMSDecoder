package com.oganbelema.smsdecoder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * This is an activity that receives an sms and decodes it
 * @author Belema Ogan
 */
public class SMSReceiverActivity extends AppCompatActivity {

    private final String TAG = SMSReceiver.class.getSimpleName();

    /**
     * Request permission code
     */
    private static final int REQUEST_PERMISSION_CODE = 101;

    /**
     * Array of required permissions
     */
    private final String[] mPermissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_receiver);

        if (!hasPermissions(this, mPermissions)){
            ActivityCompat.requestPermissions(this, mPermissions, REQUEST_PERMISSION_CODE);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null){

            String message = bundle.getString(SMSReceiver.SMS_BODY_KEY);


        }

    }

    /**
     * Check that the app has the required permissions
     * @param context activity's context
     * @param permissions list of required permissions
     * @return false if app does not have permission or true if it has
     */
    private static boolean hasPermissions(Context context, String... permissions){
        if (context != null && permissions != null){
            for (String permission : permissions){
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED){
                    return  false;
                }
            }
        }
        return true;
    }
}
