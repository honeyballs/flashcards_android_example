package de.thm.thmflashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Yannick Bals on 07.11.2017.
 */

public class MainActivity extends AppCompatActivity {

    private boolean isDualView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout of the Activity and use the toolbar
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FrameLayout detailContainer = findViewById(R.id.detailContainer);
        isDualView = detailContainer != null && detailContainer.getVisibility() == View.VISIBLE;
        if (isDualView) {
            CardsDetailFragment detailFragment = new CardsDetailFragment();
            //Optional: Pass Intent to the Fragment
            getFragmentManager().beginTransaction().add(R.id.detailContainer, detailFragment, "DETAIL").commit();
        }


    }

}
