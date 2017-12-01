package de.thm.thmflashcards;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Yannick Bals on 01.12.2017.
 */

public class AddCardActivity extends AppCompatActivity {

    private EditText questionEdit;
    private EditText answerEdit;
    private FloatingActionButton addImageButton;

    private String imagePath = null;
    private int subCategoryId = -1;

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
        //TODO: Change icon, implement image select

        //Retrieve the subCategoryId
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(getResources().getString(R.string.subCategoryKey))) {
            subCategoryId = intent.getIntExtra(getResources().getString(R.string.subCategoryKey), -1);
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
            case R.id.about_item:
                //Do shit
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
