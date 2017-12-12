package de.thm.thmflashcards;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;


/**
 * Created by Yannick Bals on 12.12.2017.
 */

public class ViewImageActivity extends AppCompatActivity {

    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 960;

    private String path;
    private Bitmap thumbnail;
    private PhotoView imageView;
    private boolean isPortrait;

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

        new ImageLoader().execute(path);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            //Set the target height and width
            int reqWidth = 0;
            int reqHeight = 0;
            if (isPortrait) {
                reqWidth = IMAGE_WIDTH;
                reqHeight = IMAGE_HEIGHT;
            } else {
                reqWidth = IMAGE_HEIGHT;
                reqHeight = IMAGE_WIDTH;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            //Set this true to get the real size of an image
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(strings[0], options);

            //Claculate the inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            //Decode the Bitmap with the set inSampleSize
            options.inJustDecodeBounds=false;
            Bitmap bmp = BitmapFactory.decodeFile(strings[0], options);

            //After we downsized the Bitmap we may need to rotate it
            return rotateIfNecessary(strings[0], bmp);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }

        //We do not want to load the full image, it will slow the app down drastically. Instead we load a downsized image
        //See https://developer.android.com/topic/performance/graphics/load-bitmap.html
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            //Raw size
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height/2;
                final int halfWidth = width/2;

                //Calculate the largest inSampleSize value as a power of 2 which keeps width and height larger than required.
                while((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }

            }
            return inSampleSize;
        }

        /**
         * On some phones, mainly Samsung, images get rotated. We need to reverse the rotation if necessary.
         * @param path The path to the image
         * @return A correctly oriented Bitmap
         */
        private Bitmap rotateIfNecessary(String path, Bitmap source) {
            Bitmap bmp = source;
            Bitmap rotatedBmp = null;
            try {
                //Read the rotation information from Exif data
                ExifInterface ei = new ExifInterface(path);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBmp = rotateBitmap(bmp, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBmp = rotateBitmap(bmp, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBmp = rotateBitmap(bmp, 270);
                        break;
                    default:
                        rotatedBmp = bmp;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return rotatedBmp;
        }

        /**
         * Rotate a Bitmap image to a certain degree.
         * @param source The source image
         * @param angle The required rotation angle
         * @return A correctly oriented Bitmap
         */
        private Bitmap rotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }
    }
}
