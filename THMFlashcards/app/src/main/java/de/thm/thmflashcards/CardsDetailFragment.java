package de.thm.thmflashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Yannick Bals on 09.11.2017.
 */

public class CardsDetailFragment extends Fragment {

    private ArrayList<Flashcard> cards;

    private RecyclerView list;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cards = new ArrayList<>();

        //Add Test data
        Flashcard card1 = new Flashcard("Frage 1?", "Antwort 1", 13, 20);
        Flashcard card2 = new Flashcard("Frage 2", "Antwort 2", 5, 1);
        Flashcard card3 = new Flashcard("Dies ist eine sehr lange Frage wie man sie normalerweise auch auf Karteikarten findet, oder nicht?", "Die Antwort auf diese Frage ist genau so lang. Hiermit will ich nur testen, wie die Darstellung langer Texte funktioniert", 15, 12);

        card3.setCurrentType(Flashcard.ANSWER_TYPE);

        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
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
        CardsAdapter adapter = new CardsAdapter(cards, getActivity());
        list.setAdapter(adapter);

        return view;
    }
}
