package de.thm.thmflashcards;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cards = new ArrayList<>();

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

        //Set up a listener to refresh the list when pulled down
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new RefreshListener());

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
     * Turn all cards back to the question side
     */
    public void turnAllCards() {
        for (Flashcard card : cards) {
            card.setCurrentType(Flashcard.QUESTION_TYPE);
        }
        adapter.notifyDataSetChanged();
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

    private class RefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            swipeRefresh.setRefreshing(true);
            reloadCards();
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
            //Calculate the success quote for all questions
            for (Flashcard card : flashcards) {
                card.setQuote((double) card.getNoCorrect() / ((double) card.getNoCorrect() + (double) card.getNoWrong()));
            }
            //Sort the Arraylist by quote
            Collections.sort(flashcards, new Comparator<Flashcard>() {
                @Override
                public int compare(Flashcard flashcard, Flashcard t1) {
                    //Return -1 if less, 0 if equal, 1 if greater
                    if (flashcard.getQuote() < t1.getQuote()) {
                        return -1;
                    } else if (flashcard.getQuote() == t1.getQuote()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });

            cards.clear();
            cards.addAll(flashcards);

            //Notify the adapter
            adapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
        }
    }

}
