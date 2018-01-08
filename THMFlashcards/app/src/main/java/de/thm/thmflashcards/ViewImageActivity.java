package de.thm.thmflashcards;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;
import de.thm.thmflashcards.imageHandling.ImageCallback;
import de.thm.thmflashcards.imageHandling.ImageHandler;


/**
 * Created by Yannick Bals on 12.12.2017.
 */

public class ViewImageActivity extends AppCompatActivity implements ImageCallback {

    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 960;

    private String path;
    private Bitmap thumbnail;
    private PhotoView imageView;
    private boolean isPortrait;

    private ImageHandler imageHandler = new ImageHandler(this, IMAGE_WIDTH, IMAGE_HEIGHT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.big_image_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.bigImageView);

        //Retrieve the intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(getResources().getString(R.string.pathKey))) {
                path = intent.getStringExtra(getResources().getString(R.string.pathKey));
            }
            if (intent.hasExtra(getResources().getString(R.string.thumbKey))) {
                thumbnail = intent.getParcelableExtra(getResources().getString(R.string.thumbKey));
            }
        }

        //Set the thumbnail for now. Then we will load the big image asynchronously
        imageView.setImageBitmap(thumbnail);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Determine the orientation to set the image parameters. If it is in landscape, width and height get switched
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            isPortrait = true;
        } else {
            isPortrait = false;
        }

        imageHandler.loadImageFromStringPath(path, isPortrait);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Inherited from ImageCallback, not needed here since the path is passed from the card.
     *
     * @param path Path to the image as String
     */
    @Override
    public void setPathToImage(String path) {

    }

    /**
     * Inherited from ImageCallback
     *
     * @param bmp Image to set for the ImageView
     */
    @Override
    public void setImage(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }

}
