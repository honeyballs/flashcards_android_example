package de.thm.thmflashcards.imageHandling;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Yannick Bals on 08.01.2018.
 */

public class ImageHandler {

    private Context callingActivity;
    private ImageCallback callback;
    private int imageWidth;
    private int imageHeight;
    private boolean isPortrait;

    public ImageHandler(Context activity, int width, int height) {
        this.callingActivity = activity;
        this.callback = (ImageCallback) activity;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    //We do not want to load the full image, it will slow the app down drastically. Instead we load a downsized image
    //See https://developer.android.com/topic/performance/graphics/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
    public static Bitmap rotateIfNecessary(String path, Bitmap source) {
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
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Create a file to save the image in. This is passed to the camera app via the intent.
     *
     * @return the created file
     */
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //Put our files into an extra folder
        File storageDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save the path in Activity
        callback.setPathToImage(image.getAbsolutePath());
        return image;
    }

    /**
     * Add the photo to the system's media provider so it can be seen in the gallery.
     */
    public void addPhotoToGallery(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        callingActivity.sendBroadcast(mediaScanIntent);
    }

    /**
     * Gets the real absolute path of an image from an Uri.
     *
     * @param contentUri The Uri you want to resolve
     * @return The absolute path
     */
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(callingActivity, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public void loadImageFromStringPath(String path, Boolean portrait) {
        isPortrait = portrait;
        new ImageLoader().execute(path);
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            //Set the target height and width, switch the values according to orientation
            int reqWidth = 0;
            int reqHeight = 0;
            if (isPortrait) {
                reqWidth = imageWidth;
                reqHeight = imageHeight;
            } else {
                reqWidth = imageHeight;
                reqHeight = imageWidth;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            //Set this true to get the real size of an image
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(strings[0], options);

            //Calculate the inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            //Decode the Bitmap with the set inSampleSize
            options.inJustDecodeBounds=false;
            Bitmap bmp = BitmapFactory.decodeFile(strings[0], options);

            //After we downsized the Bitmap we may need to rotate it
            return rotateIfNecessary(strings[0], bmp);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            callback.setImage(bitmap);
        }
    }
}
