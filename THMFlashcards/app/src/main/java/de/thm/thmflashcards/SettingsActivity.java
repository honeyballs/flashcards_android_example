package de.thm.thmflashcards;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;

import java.util.Calendar;

/**
 * Created by Yannick Bals on 08.01.2018.
 */

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchCompat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Tell the toolbar that we can navigate back from this activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        switchCompat = findViewById(R.id.notificationSwitch);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switchCompat.setChecked(prefs.getBoolean(getString(R.string.remindMe), true));
        switchCompat.setShowText(false);
        switchCompat.setOnCheckedChangeListener(new SwitchListener());

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Set the settings according to the switch state.
     */
    class SwitchListener implements SwitchCompat.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getString(R.string.remindMe), b);
            editor.apply();
            //Also start/stop notifications here in case the user closes the app after visiting the settings.
            if (b) {
                initNotificationChannel();
                startAlarmIfNotExists();
            } else {
                deleteNotificationChannel();
                stopAlarmIfExists();
            }
        }
    }

    /**
     * Create a notification channel which is required for an sdk level >= 26
     *
     */
    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        } else {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = getString(R.string.n_channel);
            // The user-visible name of the channel.
            CharSequence name = getString(R.string.channel_name);
            // The user-visible description of the channel.
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{500, 500});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void deleteNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        } else {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = getString(R.string.n_channel);
            mNotificationManager.deleteNotificationChannel(id);
        }
    }

    private void startAlarmIfNotExists() {
        //Create the explizit intent
        Intent serviceIntent = new Intent(this, ReminderService.class);
        //The NO_CREATE flag returns null if the intent doesn't exist
        boolean alarmRunning = (PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmRunning) {
            //Set up an alarm to start the service
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent startServicePending = PendingIntent.getService(this, 0, serviceIntent, 0);

            //Set the time at which the notification should appear
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            //Set up the alarm. This will send a daily notification at 9:00am
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*60*24, startServicePending);
        }
    }

    private void stopAlarmIfExists() {
        //Create the explizit intent
        Intent serviceIntent = new Intent(this, ReminderService.class);
        //The NO_CREATE flag returns null if the intent doesn't exist
        boolean alarmRunning = (PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmRunning) {
            //Cancel the alarm
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent startServicePending = PendingIntent.getService(this, 0, serviceIntent, 0);
            alarmManager.cancel(startServicePending);
        }
    }
}
