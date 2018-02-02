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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Calendar;

/**
 * Created by Yannick Bals on 07.11.2017.
 */

public class MainActivity extends AppCompatActivity implements Communicator{

    private boolean isDualView;
    private CategoryMasterFragment masterFragment;
    private CardsDetailFragment detailFragment = null;
    private boolean[] expanded = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout of the Activity and use the toolbar
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Check if we have information to restore the expansion status of the fragment
        if (savedInstanceState != null) {
            expanded = savedInstanceState.getBooleanArray(getResources().getString(R.string.expandedArrayKey));
        }

        //Set the fragment for the master list and pass this activity as communicator
        masterFragment = new CategoryMasterFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.masterContainer, masterFragment, "MASTER").commit();

        FrameLayout detailContainer = findViewById(R.id.detailContainer);
        isDualView = detailContainer != null && detailContainer.getVisibility() == View.VISIBLE;

        //Set a daily reminder if checked in the settings.
        // This is set in onCreate to start it initially.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //If no preference is set it will default to true
        boolean remind = prefs.getBoolean(getString(R.string.remindMe), true);
        if (remind) {
            initNotificationChannel();
            startAlarmIfNotExists();
        } else {
            deleteNotificationChannel();
            stopAlarmIfExists();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Pass the expansion state to the fragment when this activity is resumed.
        if (expanded != null) {
            Bundle args = new Bundle();
            args.putBooleanArray(getResources().getString(R.string.expandedArrayKey), expanded);
            masterFragment.setArguments(args);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //If the Master Detail view is active we need to display the turn button in this toolbar.
        if (isDualView) {
            getMenuInflater().inflate(R.menu.main_w_turn_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_item:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.turnAllCards:
                if (detailFragment != null) {
                    detailFragment.turnAllCards();
                }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(getResources().getString(R.string.expandedArrayKey), masterFragment.getExpandedStatus());
    }

    @Override
    public void loadDetailFor(int subCategoryId) {
        if (isDualView) {
            detailFragment = new CardsDetailFragment();
            Bundle idBundle = new Bundle();
            idBundle.putInt(getResources().getString(R.string.subCategoryKey), subCategoryId);
            detailFragment.setArguments(idBundle);
            //Optional: Pass Intent to the Fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.detailContainer, detailFragment, "DETAIL").commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(getResources().getString(R.string.subCategoryKey), subCategoryId);
            startActivity(intent);
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
