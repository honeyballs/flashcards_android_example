package de.thm.thmflashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Yannick Bals on 07.11.2017.
 */

public class MainActivity extends AppCompatActivity implements Communicator{

    private boolean isDualView;
    private CategoryMasterFragment masterFragment;
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.about_item:
                //Do shit
                return true;
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
            CardsDetailFragment detailFragment = new CardsDetailFragment();
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
}
