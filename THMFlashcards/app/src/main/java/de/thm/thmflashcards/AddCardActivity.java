package de.thm.thmflashcards;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.thm.thmflashcards.imageHandling.ImageCallback;
import de.thm.thmflashcards.imageHandling.ImageHandler;
import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Yannick Bals on 01.12.2017.
 */

public class AddCardActivity extends AppCompatActivity implements ImageCallback {

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST = 11;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int GALLERY_PERMISSION_REQUEST = 22;

    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 200;

    private EditText questionEdit;
    private EditText answerEdit;
    private FloatingActionButton addImageButton;
    private ImageView imagePreview;

    private String imagePath = null;
    private int subCategoryId = -1;

    private boolean isPortrait;

    private AlertDialog cameraOrGalleryDialog = null;

    private ImageHandler imageHandler = new ImageHandler(this, IMAGE_WIDTH, IMAGE_HEIGHT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_cards);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionEdit = findViewById(R.id.questionEditText);
        answerEdit = findViewById(R.id.answerEditText);
        addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new AddImageListener());

        imagePreview = findViewById(R.id.previewImageView);

        //Retrieve the subCategoryId
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(getResources().getString(R.string.subCategoryKey))) {
            subCategoryId = intent.getIntExtra(getResources().getString(R.string.subCategoryKey), -1);
        }

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
    }

    //Restore the image after the device was rotated.
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (imagePath == null) {
            imagePath = savedInstanceState.getString(getResources().getString(R.string.imagePathBundleKey), null);
            if (imagePath != null) {
                imageHandler.loadImageFromStringPath(imagePath, isPortrait);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_card_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.confirm_item:
                addFlashcardToDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //Navigate Back when the back arrow is pressed
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //Save the image path when the device is rotated. EditTexts are automatically saved.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getResources().getString(R.string.imagePathBundleKey), imagePath);
    }

    /**
     * Create the flashcard and persist it.
     */
    private void addFlashcardToDB() {
        //Check if all needed information is available
        if (subCategoryId != -1) {
            if (!questionEdit.getText().toString().equals("")) {
                if (!answerEdit.getText().toString().equals("")) {

                    //Create the card
                    Flashcard card = new Flashcard(questionEdit.getText().toString(), answerEdit.getText().toString(), imagePath);
                    card.setSubCategoryId(subCategoryId);

                    //Persist
                    new InsertCard().execute(card);

                } else {
                    Toast.makeText(this, getResources().getString(R.string.error_empty_answer), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_empty_question), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_subcategory), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Start the camera to take a picture.
     */
    private void loadImageCamera() {
        //Check if we have the permissions to access the camera and save the image
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //Request the permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST);
        } else {
            //Intent to start the camera
            Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Create a file to pass to the camera activity
            File imageFile = null;
            try {
                imageFile = imageHandler.createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null) {
                //We use a file provider that we defined in the manifest to retrieve an Uri
                //Additional info: https://developer.android.com/training/camera/photobasics.html
                Uri imageUri = FileProvider.getUriForFile(this, "de.thm.thmflashcards.fileprovider", imageFile);
                startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(startCameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * Pick an image from the gallery.
     */
    private void loadImageGallery() {
        //Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST);
        } else {
            Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImage, GALLERY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    //Toast.makeText(this, getResources().getString(R.string.image_added), Toast.LENGTH_SHORT).show();
                    imageHandler.addPhotoToGallery(imagePath);
                    imageHandler.loadImageFromStringPath(imagePath, isPortrait);
                    break;
                case GALLERY_REQUEST_CODE:
                  Uri selectedImage = data.getData();
                  imagePath = imageHandler.getRealPathFromURI(selectedImage);
                  //Toast.makeText(this, getResources().getString(R.string.image_added), Toast.LENGTH_SHORT).show();
                    imageHandler.loadImageFromStringPath(imagePath, isPortrait);
                  break;
                default:
                    break;
            }

        }
    }

    //Check whether the permissions have been granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT).show();
                //Start the function again
                loadImageCamera();
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.gallery_permission_granted), Toast.LENGTH_SHORT).show();
                loadImageGallery();
            } else {
                Toast.makeText(this, getResources().getString(R.string.gallery_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Implemented from ImageCallback
     *
     * @param path Path to the image as String
     */
    @Override
    public void setPathToImage(String path) {
        imagePath = path;
    }

    /**
     * Inherited from ImageCallback
     *
     * @param bmp Bitmap to show in ImageView
     */
    @Override
    public void setImage(Bitmap bmp) {
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setImageBitmap(bmp);
    }

    private class AddImageListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(AddCardActivity.this);
            builder.setTitle(getResources().getString(R.string.camera_or_gallery));
            View v = LayoutInflater.from(AddCardActivity.this).inflate(R.layout.camera_or_gallery_dialog, null);
            ImageButton camera = v.findViewById(R.id.cameraButton);
            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cameraOrGalleryDialog.dismiss();
                    loadImageCamera();
                }
            });
            ImageButton gallery = v.findViewById(R.id.galleryButton);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cameraOrGalleryDialog.dismiss();
                    loadImageGallery();
                }
            });
            //We create a reference to the dialog because we have to dismiss it after an action was chosen
            builder.setView(v);
            cameraOrGalleryDialog = builder.create();
            cameraOrGalleryDialog.show();

        }
    }


    private class InsertCard extends AsyncTask<Flashcard, Void, Long> {

        @Override
        protected Long doInBackground(Flashcard... flashcards) {
            AppDatabase db = AppDatabase.getAppDataBase(AddCardActivity.this);
            return db.flashcardDao().insertFlashcard(flashcards[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong == 0) {
                Toast.makeText(AddCardActivity.this, getResources().getString(R.string.error_insert), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddCardActivity.this, getResources().getString(R.string.success_insert), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

}
