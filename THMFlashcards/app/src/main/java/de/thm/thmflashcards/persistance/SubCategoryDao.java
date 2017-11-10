package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by Yannick Bals on 10.11.2017.
 */

@Dao
public interface SubCategoryDao {

    @Insert
    public long insertSubCategory(SubCategory subCategory);

    @Delete
    public int deleteSubCategoty(SubCategory subCategory);

    @Query("SELECT * FROM sub_categories WHERE category_id = :id")
    public SubCategory[] loadSubCategoriesOfCategory(int id);

}
