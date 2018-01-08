package de.thm.thmflashcards;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;

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
        }
    }
}
