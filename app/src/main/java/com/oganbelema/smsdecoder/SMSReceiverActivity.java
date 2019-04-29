package com.oganbelema.smsdecoder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an activity that receives an sms, decodes it and builds it's ui with decoded message
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

    /**
     * Regex of the expected SMS message
     */
    String nonGreedyRegex =".*?";	// Non-greedy match on filler
    String dateRegex ="(11\\/18\\/2016)";	// MMDDYYYY 1
    String timeRegex ="((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])" +
            "?(?:\\s?(?:am|AM|pm|PM))?)";	// HourMinuteSec 1
    String re5="( )";	// White Space 1
    String re6="(TXG)";	// Word 1
    String re7="(\\s+)";	// White Space 2
    String re8="(C55)";	// Alphanum 1
    String re9="(\\/DRAINQ)";	// Unix Path 1
    String re10="(.)";	// Any Single Character 1
    String re11="( )";	// White Space 3
    String re12="(WHITE)";	// Word 2
    String re13="(\\/GIN)";	// Unix Path 2
    String re14="(~)";	// Any Single Character 2
    String widthRegex ="(200)";	// Integer Number 1
    String lengthRegex ="(200)";	// Integer Number 2
    String color1 ="(CD00A0)";	// Alphanum 2
    String color2 ="(FFFFFF)";	// Word 3

    /**
     * Pattern for the date
     */
    Pattern mDatePattern = Pattern.compile(nonGreedyRegex+dateRegex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Pattern for the time
     */
    Pattern mTimePattern = Pattern.compile(nonGreedyRegex+timeRegex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Pattern for width and length
     */
    Pattern mWidthAndLengthPattern = Pattern.compile(nonGreedyRegex+ widthRegex +nonGreedyRegex+ lengthRegex,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Pattern for the color
     */
    Pattern mColorsPattern = Pattern.compile(nonGreedyRegex+ color1 +nonGreedyRegex+ color2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Pattern for the coded message
     */
    Pattern mCodedMessagePattern = Pattern.compile(nonGreedyRegex+re6+re5+re8+re9+re10+re11+re12+re13+re14,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    /**
     * TextView for dimension
     */
    private TextView mDimensionTextView;

    /**
     * TextView for coded message
     */
    private TextView mCodedMessageTextView;

    /**
     * TextView for date and time
     */
    private TextView mDateAndTimeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_receiver);

        Group waitingForMessageViews = findViewById(R.id.waiting_for_message_views);
        mDimensionTextView = findViewById(R.id.dimension_textView);
        mCodedMessageTextView = findViewById(R.id.coded_message_textView);
        mDateAndTimeTextView = findViewById(R.id.date_time_textView);

        if (!hasPermissions(this, mPermissions)){
            ActivityCompat.requestPermissions(this, mPermissions, REQUEST_PERMISSION_CODE);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null){

            String message = bundle.getString(SMSReceiver.SMS_BODY_KEY);

            if (message != null){
                waitingForMessageViews.setVisibility(View.GONE);

                showSenderInActionBar();

                extractWidthAndLength(message);

                extractColors(message);

                extractCodedMessage(message);

                extractDateAndTime(message);
            }

        }

    }

    /**
     * This method extracts the coded message from the received sms message using regex pattern
     * matcher
     * @param message the received sms message body
     */
    private void extractCodedMessage(String message) {
        Matcher codedMessageMatcher = mCodedMessagePattern.matcher(message);

        if (codedMessageMatcher.find()){
            String codedMessage ="";
            codedMessage += codedMessageMatcher.group(1);
            codedMessage+= codedMessageMatcher.group(2);
            codedMessage += codedMessageMatcher.group(3);
            codedMessage += codedMessageMatcher.group(4);
            codedMessage += codedMessageMatcher.group(5);
            codedMessage += codedMessageMatcher.group(6);
            codedMessage += codedMessageMatcher.group(7);
            displayCodedMessage(codedMessage);
        }
    }

    /**
     * Extracts the colors from the received sms message using regex pattern matcher
     * @param message the received sms message body
     */
    private void extractColors(String message) {
        Matcher colorMatcher = mColorsPattern.matcher(message);

        if (colorMatcher.find()){
            String colorCode1 = colorMatcher.group(1);
            String colorCode2 = colorMatcher.group(2);
            styleDimensionTextView(colorCode1, colorCode2);
        }
    }

    /**
     * Extracts the date and time from the received sms message using regex pattern matcher
     * @param message the received sms message body
     */
    private void extractDateAndTime(String message) {
        Matcher dateMatcher = mDatePattern.matcher(message);
        Matcher timeMatcher = mTimePattern.matcher(message);

        String date = "";
        String time = "";

        if (dateMatcher.find()){
            date = dateMatcher.group(1);
        }

        if (timeMatcher.find()){
            time = timeMatcher.group(1);
        }

        displayDateAndTime(date, time);
    }

    /**
     * Extracts the width and length from the received sms message body using regex pattern matcher
     * @param message the received sms message body
     */
    private void extractWidthAndLength(String message) {
        Matcher widthAndLengthMatcher = mWidthAndLengthPattern.matcher(message);
        if (widthAndLengthMatcher.find()){
            String dimensionW = widthAndLengthMatcher.group(1);
            String dimensionL = widthAndLengthMatcher.group(2);
            displayDimensionTextView(dimensionW, dimensionL);
        }
    }

    /**
     * Displays the name of the sms sender in the actionbar
     */
    private void showSenderInActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null){
            getSupportActionBar().setSubtitle(getString(R.string.message_from));
        }
    }

    /**
     * Displays the coded message in the text view
     * @param codedMessage coded message received from SMS
     */
    private void displayCodedMessage(String codedMessage) {
        mCodedMessageTextView.setText(codedMessage);
        mCodedMessageTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Styles the dimension text background and text with the received colors
     * @param color1 the first extracted color
     * @param color2 the second extracted color
     */
    private void styleDimensionTextView(String color1, String color2) {
        mDimensionTextView.setBackgroundColor(Color.parseColor("#"+color1));
        mDimensionTextView.setTextColor(Color.parseColor("#"+color2));
    }

    /**
     * Sets up the dimensions textview with the dimensions decoded and sets it's text
     * @param width the extracted width
     * @param length the extracted length
     */
    private void displayDimensionTextView(String width, String length) {
        try {
            int widthInInteger = Integer.valueOf(width);
            int lengthInInteger = Integer.valueOf(length);
            mDimensionTextView.setWidth(dpToPixel(widthInInteger));
            mDimensionTextView.setHeight(dpToPixel(lengthInInteger));
            mDimensionTextView.setText(getString(R.string.width_and_length, width, length));
            mDimensionTextView.setVisibility(View.VISIBLE);
        } catch (Exception e){
            Log.e(TAG, "Error parsing dimension", e);
        }
    }

    /**
     * Formats the date and populates the view with the date and time
     * @param date the extracted date
     * @param time the extracted time
     */
    private void displayDateAndTime(String date, String time) {
        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat oldFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = null;
        try {
            formattedDate = newFormat.format(oldFormat.parse(date));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
        }
        mDateAndTimeTextView.setText(getString(R.string.date_and_time, formattedDate, time));
        mDateAndTimeTextView.setVisibility(View.VISIBLE);
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

    /**
     * Converts integer density independent pixel to pixel
     * @param dp density independent pixel to be converted
     * @return density independent pixel in pixel
     */
    private int dpToPixel(int dp){
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,getResources().getDisplayMetrics()));
    }
}
