package de.thm.thmflashcards.imageHandling;

import android.graphics.Bitmap;

/**
 * Created by Yannick Bals on 08.01.2018.
 */

public interface ImageCallback {

    void setPathToImage(String path);
    void setImage(Bitmap bmp);

}
