package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

@Dao
public interface CategoryDao {

    //Default onConflict Management is abort, we don't need to set it here
    @Insert
    public long insertCategory(Category category);

    @Delete
    public int deleteCategory(Category category);

    @Query("SELECT * FROM categories")
    public Category[] loadAllCategories();

}
