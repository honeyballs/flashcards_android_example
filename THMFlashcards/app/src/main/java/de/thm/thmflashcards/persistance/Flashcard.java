package de.thm.thmflashcards.persistance;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Yannick Bals on 28.11.2017.
 */

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(entity = SubCategory.class,
                                parentColumns = "id",
                                childColumns = "sub_category_id",
                                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "sub_category_id")})
public class Flashcard {

    //Don't persist constants
    @Ignore
    public static final int QUESTION_TYPE = 0;
    @Ignore
    public static final int ANSWER_TYPE = 1;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sub_category_id")
    private int subCategoryId;
    private String question;
    private String answer;
    @ColumnInfo(name = "answer_image_path")
    private String answerImagePath;
    @ColumnInfo(name = "no_correct")
    private int noCorrect;
    @ColumnInfo(name = "no_wrong")
    private int noWrong;

    //Don't persist the current type so the cards will always be loaded showing the question
    @Ignore
    private int currentType;

    @Ignore
    private double quote;

    //Constructor to set a text answer and an image path - set null if one isn't there
    public Flashcard(String question, String answer, String answerImagePath) {
        this.question = question;
        this.answer = answer;
        this.answerImagePath = answerImagePath;
        this.noCorrect = 0;
        this.noWrong = 0;
        this.currentType = QUESTION_TYPE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(int subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswerImagePath() {
        return answerImagePath;
    }

    public void setAnswerImagePath(String answerImagePath) {
        this.answerImagePath = answerImagePath;
    }

    public int getNoCorrect() {
        return noCorrect;
    }

    public void setNoCorrect(int noCorrect) {
        this.noCorrect = noCorrect;
    }

    public int getNoWrong() {
        return noWrong;
    }

    public void setNoWrong(int noWrong) {
        this.noWrong = noWrong;
    }

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    public double getQuote() {
        return quote;
    }

    public void setQuote(double quote) {
        this.quote = quote;
    }
}
