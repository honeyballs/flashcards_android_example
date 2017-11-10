package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

@Database(entities = {Category.class, SubCategory.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    //Use the singleton pattern because instantiating this object is expensive
    private static AppDatabase instance;

    public abstract CategoryDao categoryDao();
    public abstract SubCategoryDao subCategoryDao();

    public static AppDatabase getAppDataBase(Context context) {
        if (instance == null) {
            //Get an instance to the database to run your queries on.
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "flashcards_db").build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

}
