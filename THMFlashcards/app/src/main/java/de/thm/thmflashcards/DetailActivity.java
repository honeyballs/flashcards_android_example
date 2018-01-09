package de.thm.thmflashcards;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

public class DetailActivity extends AppCompatActivity {

    private CardsDetailFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //We use the two fragment layout in landscape so this activity is no longer needed
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
        }

        setContentView(R.layout.main_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Tell the toolbar that we can navigate back from this activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Pass the subcategory id to the fragment
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(getResources().getString(R.string.subCategoryKey))) {
            Bundle idBundle = new Bundle();
            Log.e("id", ""+intent.getIntExtra(getResources().getString(R.string.subCategoryKey), -1));
            idBundle.putInt(getResources().getString(R.string.subCategoryKey),
                    intent.getIntExtra(getResources().getString(R.string.subCategoryKey), -1));
            fragment = (CardsDetailFragment) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
            fragment.setArguments(idBundle);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.turnAllCards) {
            fragment.turnAllCards();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //Navigate Back when the back arrow is pressed
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
