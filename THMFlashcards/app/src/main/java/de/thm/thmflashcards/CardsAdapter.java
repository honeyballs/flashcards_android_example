package de.thm.thmflashcards;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Farea on 28.11.2017.
 */

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Flashcard> cards;
    private Context context;


    public CardsAdapter(ArrayList<Flashcard> cards, Context context) {
        this.cards = cards;
        this.context = context;
    }

    //Check whether the answer or question should be displayed
    @Override
    public int getItemViewType(int position) {
        return cards.get(position).getCurrentType();
    }

    //Create new views depending on the current viewType
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Flashcard.QUESTION_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_question_listitem, parent, false);
            //Initialize the listeners here so it only has to be done once when a view is created
            return new QuestionViewHolder(v, new AnswerQuestionListener(), new DeleteListener());
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_answer_listitem, parent, false);
            return new AnswerViewHolder(v, new TurnAroundListener(), new DeleteListener());
        }
    }

    //Populate the current view with data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);
        Flashcard item = cards.get(position);

        if (viewType == Flashcard.QUESTION_TYPE) {
            QuestionViewHolder qvh = (QuestionViewHolder) holder;
            //Give the listener the position of the item to later retrieve the actual item from the ArrayList
            qvh.answerQuestionListener.setPosition(position);
            qvh.question.setText(item.getQuestion());
        } else {
            AnswerViewHolder avh = (AnswerViewHolder) holder;
            avh.turnAroundListener.setPosition(position);
            avh.question.setText(item.getQuestion());
            avh.answer.setText(item.getAnswer());
            //Only show the image if needed
            if (item.getAnswerImagePath() != null && !item.getAnswerImagePath().equals("")) {
                //TODO: Scale the image down and turn it
                Uri path = Uri.parse(item.getAnswerImagePath());
                avh.answerImage.setImageURI(path);
                avh.answerImage.setVisibility(View.VISIBLE);
            }
            //Calculate the success rate
            double rate = (double) item.getNoCorrect() / ((double) item.getNoCorrect() + (double) item.getNoWrong());
            //Convert to percentage
            int ratePercent = (int) (rate * 100);
            String rateText = ratePercent + "%";
            avh.successRate.setText(rateText);
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    /**
     * Define Viewholders for questions and answers
     */

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {

        public TextView question;
        public TextView knowView;
        public TextView dontKnowView;
        public AnswerQuestionListener answerQuestionListener;
        public DeleteListener deleteCardListener;

        public QuestionViewHolder(View itemView, AnswerQuestionListener listener, DeleteListener deleteListener) {
            super(itemView);
            question = itemView.findViewById(R.id.question_text);
            knowView = itemView.findViewById(R.id.knowTextView);
            dontKnowView = itemView.findViewById(R.id.dontKnowTextView);
            //Set the listeners here so it only has to be done once when a view is created
            this.answerQuestionListener = listener;
            knowView.setOnClickListener(answerQuestionListener);
            dontKnowView.setOnClickListener(answerQuestionListener);
            deleteCardListener = deleteListener;
            itemView.setOnLongClickListener(deleteCardListener);
        }

    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {

        public TextView question;
        public TextView answer;
        public ImageView answerImage;
        public TextView successRate;
        public TextView turn;
        public TurnAroundListener turnAroundListener;
        public DeleteListener deleteCardListener;

        public AnswerViewHolder(View itemView, TurnAroundListener listener, DeleteListener deleteListener) {
            super(itemView);
            question = itemView.findViewById(R.id.questionTextView);
            answer = itemView.findViewById(R.id.answerTextView);
            answerImage = itemView.findViewById(R.id.answerImage);
            successRate = itemView.findViewById(R.id.successRateView);
            turn = itemView.findViewById(R.id.turnTextView);
            turnAroundListener = listener;
            turn.setOnClickListener(turnAroundListener);
            deleteCardListener = deleteListener;
            itemView.setOnLongClickListener(deleteCardListener);
        }
    }

    /**
     * Define the listeners
     */

    //Increase one of the answer scores (wrong or right) and turn the card around to view the answer
    class AnswerQuestionListener implements View.OnClickListener {

        private int position;

        @Override
        public void onClick(View view) {
            Flashcard card = cards.get(position);
            //Increase the answer counter accordingly
            if (view.getId() == R.id.knowTextView) {
                card.setNoCorrect(card.getNoCorrect()+1);
            }else {
                card.setNoWrong(card.getNoWrong()+1);
            }
            //Update the card in the db
            new UpdateCounter().execute(card);
            //Turn the card around
            card.setCurrentType(Flashcard.ANSWER_TYPE);
            notifyDataSetChanged();
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    //Turn the card back around to view the question
    class TurnAroundListener implements View.OnClickListener {

        private int position;

        @Override
        public void onClick(View view) {
            //Just change the type of the card item
            cards.get(position).setCurrentType(Flashcard.QUESTION_TYPE);
            notifyDataSetChanged();
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    //Long click listener to delete questions
    class DeleteListener implements View.OnLongClickListener {

        private int position;

        @Override
        public boolean onLongClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getResources().getString(R.string.delete_card_title));
            builder.setMessage(context.getResources().getString(R.string.delete_card));
            builder.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new DeleteCard().execute(cards.get(position));
                    cards.remove(position);
                    notifyDataSetChanged();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
            return true;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    private class UpdateCounter extends AsyncTask<Flashcard, Void, Integer> {

        @Override
        protected Integer doInBackground(Flashcard... flashcards) {
            AppDatabase db = AppDatabase.getAppDataBase(context);
            return db.flashcardDao().updateFlashcard(flashcards[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.update_card_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteCard extends AsyncTask<Flashcard, Void, Integer> {

        @Override
        protected Integer doInBackground(Flashcard... flashcards) {
            AppDatabase db = AppDatabase.getAppDataBase(context);
            return db.flashcardDao().deleteFlashcard(flashcards[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.delete_cards_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
