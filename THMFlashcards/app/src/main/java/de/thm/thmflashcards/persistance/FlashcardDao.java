package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Yannick Bals on 30.11.2017.
 */

@Dao
public interface FlashcardDao {

    @Insert
    public long insertFlashcard(Flashcard flashcard);

    @Delete
    public int deleteFlashcard(Flashcard flashcard);

    @Delete
    public int deleteMultipleFlashcards(List<Flashcard> flashcards);

    @Update
    public int updateFlashcard(Flashcard flashcard);

    @Query("SELECT * FROM flashcards WHERE sub_category_id = :subCategoryId")
    public List<Flashcard> getAllFlashcardsOfSubCategory(int subCategoryId);

}
