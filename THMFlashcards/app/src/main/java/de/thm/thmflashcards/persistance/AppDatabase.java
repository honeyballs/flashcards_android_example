package de.thm.thmflashcards.persistance;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

@Database(entities = {Category.class, SubCategory.class, Flashcard.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    //Use the singleton pattern because instantiating this object is expensive
    private static AppDatabase instance;

    public abstract CategoryDao categoryDao();
    public abstract SubCategoryDao subCategoryDao();
    public abstract FlashcardDao flashcardDao();

    public static AppDatabase getAppDataBase(Context context) {
        if (instance == null) {
            //Get an instance to the database to run your queries on.
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "flashcards_db")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    //Migration Classes to persist user data during version changes
    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String migrationQuery = "CREATE TABLE 'flashcards' (" +
                    "'id' INTEGER NOT NULL, " +
                    "'sub_category_id' INTEGER NOT NULL, " +
                    "'question' TEXT, " +
                    "'answer' TEXT, " +
                    "'answer_image_path' TEXT, " +
                    "'no_correct' INTEGER NOT NULL, " +
                    "'no_wrong' INTEGER NOT NULL, " +
                    "PRIMARY KEY('id'), " +
                    "FOREIGN KEY('sub_category_id') REFERENCES sub_categories('id') ON DELETE CASCADE)";

            String createIndexQuery = "CREATE INDEX index_flashcards_sub_category_id ON flashcards(sub_category_id)";

            //Execute necessary queries
            database.execSQL(migrationQuery);
            database.execSQL(createIndexQuery);
        }
    };

}
