package de.thm.thmflashcards;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Yannick Bals on 09.11.2017.
 */

public class CardsDetailFragment extends Fragment {

    private ArrayList<Flashcard> cards;
    private int subCategoryId;

    private RecyclerView list;
    private CardsAdapter adapter;
    private FloatingActionButton addCardButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cards = new ArrayList<>();

        //Add Test data
        /*Flashcard card1 = new Flashcard("Frage 1?", "Antwort 1", null);
        Flashcard card2 = new Flashcard("Frage 2", "Antwort 2", null);
        Flashcard card3 = new Flashcard("Dies ist eine sehr lange Frage wie man sie normalerweise auch auf Karteikarten findet, oder nicht?", "Die Antwort auf diese Frage ist genau so lang. Hiermit will ich nur testen, wie die Darstellung langer Texte funktioniert", null);

        card3.setCurrentType(Flashcard.ANSWER_TYPE);

        cards.add(card1);
        cards.add(card2);
        cards.add(card3);*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cards_detail, container, false);

        //Initialize the RecyclerView
        list = view.findViewById(R.id.detailRecycler);
        //Improves performance if size of the view doesn't change
        list.hasFixedSize();
        //The layout manager handles placing of items and determines reuse
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);
        //Set the adapter
        adapter = new CardsAdapter(cards, getActivity());
        list.setAdapter(adapter);

        addCardButton = view.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new AddCardListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //load the cards from the db
        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            subCategoryId = args.getInt(getResources().getString(R.string.subCategoryKey), -1);
            reloadCards();
        }
    }



    /**
     * Refresh the flashcards
     */
    private void reloadCards() {
        new CardsLoader().execute(subCategoryId);
    }

    /**
     * Start the Add Cards Activity.
     */
    private class AddCardListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), AddCardActivity.class);
            intent.putExtra(getResources().getString(R.string.subCategoryKey), subCategoryId);
            startActivity(intent);
        }
    }

    //AsyncTask to load the cards
    private class CardsLoader extends AsyncTask<Integer, Void, List<Flashcard>> {

        @Override
        protected List<Flashcard> doInBackground(Integer... integers) {
            Log.e("subCategoryId", ""+integers[0]);
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            return db.flashcardDao().getAllFlashcardsOfSubCategory(integers[0]);
        }

        @Override
        protected void onPostExecute(List<Flashcard> flashcards) {
            cards.clear();
            cards.addAll(flashcards);
            //Notify the adapter
            Log.e("In AsyncTask", "true");
            Log.e("Cards list", cards.toString());
            adapter.notifyDataSetChanged();
        }
    }

}
