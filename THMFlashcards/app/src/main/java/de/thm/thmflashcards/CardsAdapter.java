package de.thm.thmflashcards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import de.thm.thmflashcards.imageHandling.ImageHandler;
import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.Flashcard;

/**
 * Created by Yannick Bals on 28.11.2017.
 */

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Flashcard> cards;
    private Context context;
    private RecyclerView mRecyclerView;

    public CardsAdapter(ArrayList<Flashcard> cards, Context context) {
        this.cards = cards;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
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
            v.setOnLongClickListener(new DeleteListener());
            //Initialize the listeners here so it only has to be done once when a view is created
            QuestionViewHolder qvh = new QuestionViewHolder(v);
            qvh.turn.setOnClickListener(new TurnAroundListener());
            return qvh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_answer_listitem, parent, false);
            v.setOnLongClickListener(new DeleteListener());
            AnswerViewHolder avh = new AnswerViewHolder(v);
            DidKnowListener didKnowListener = new DidKnowListener();
            avh.knowView.setOnClickListener(didKnowListener);
            avh.dontKnowView.setOnClickListener(didKnowListener);
            return new AnswerViewHolder(v);
        }
    }

    //Populate the current view with data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);
        Flashcard item = cards.get(position);

        //Assign data and listeners
        if (viewType == Flashcard.QUESTION_TYPE) {
            QuestionViewHolder qvh = (QuestionViewHolder) holder;
            qvh.question.setText(item.getQuestion());
        } else {
            AnswerViewHolder avh = (AnswerViewHolder) holder;
            avh.question.setText(item.getQuestion());
            avh.answer.setText(item.getAnswer());
            //Calculate the success rate
            double rate = (double) item.getNoCorrect() / ((double) item.getNoCorrect() + (double) item.getNoWrong());
            //Convert to percentage
            int ratePercent = (int) (rate * 100);
            String rateText = ratePercent + "%";
            avh.successRate.setText(rateText);
            //Only show the image if needed
            if (item.getAnswerImage() != null) {
                avh.answerImage.setVisibility(View.VISIBLE);
                Bitmap thumbnail = item.getAnswerImage();
                avh.answerImage.setImageBitmap(thumbnail);
                //Set up the listener to start the activity
                avh.setImageListener(new ViewAnswerImageListener(item.getAnswerImagePath(), thumbnail));
            } else {
                avh.answerImage.setVisibility(View.GONE);
            }
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
        public TextView turn;
        private View parentView;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question_text);
            turn = itemView.findViewById(R.id.turnTextView);
            parentView = itemView;
        }

    }

    public static class AnswerViewHolder extends RecyclerView.ViewHolder {

        public TextView question;
        public TextView answer;
        public CircleImageView answerImage;
        public TextView successRate;
        public TextView knowView;
        public TextView dontKnowView;

        private View parentView;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.questionTextView);
            answer = itemView.findViewById(R.id.answerTextView);
            answerImage = itemView.findViewById(R.id.answerImageView);
            successRate = itemView.findViewById(R.id.successRateView);
            knowView = itemView.findViewById(R.id.knowTextView);
            dontKnowView = itemView.findViewById(R.id.dontKnowTextView);
            parentView = itemView;
        }

        public void setImageListener(ViewAnswerImageListener answerImageListener) {
            answerImage.setOnClickListener(answerImageListener);
        }
    }



    /**
     * Define the listeners
     */

    //Increase one of the answer scores (wrong or right) and turn the card around to view the answer
    class DidKnowListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            //The RecyclerView can only find the position of a view one level below itself
            // so we have to navigate the view hierarchy upwards from the clicked view.
            View v = view;
            View parent = (View) v.getParent();
            while (!(parent instanceof RecyclerView)){
                v=parent;
                parent = (View) parent.getParent();
            }
            int position = mRecyclerView.getChildAdapterPosition(v);
            Flashcard card = cards.get(position);
            //Increase the answer counter accordingly
            if (view.getId() == R.id.knowTextView) {
                card.setNoCorrect(card.getNoCorrect()+1);
            }else {
                card.setNoWrong(card.getNoWrong()+1);
            }
            //Update the card in the db
            new UpdateCounter().execute(card);
            notifyDataSetChanged();
        }

    }

    //Turn the card back around to view the question
    class TurnAroundListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View v = view;
            View parent = (View) v.getParent();
            while (!(parent instanceof RecyclerView)){
                v=parent;
                parent = (View) parent.getParent();
            }
            int position = mRecyclerView.getChildAdapterPosition(v);
            //Just change the type of the card item
            cards.get(position).setCurrentType(Flashcard.ANSWER_TYPE);
            notifyDataSetChanged();
        }

    }

    /**
     * Starts an Activity to view the answer image in full size.
     */
    class ViewAnswerImageListener implements View.OnClickListener {

        private String path;
        private Bitmap thumbnail;

        public ViewAnswerImageListener(String path, Bitmap thumbnail) {
            this.path = path;
            this.thumbnail = thumbnail;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ViewImageActivity.class);
            intent.putExtra(context.getResources().getString(R.string.pathKey), path);
            intent.putExtra(context.getResources().getString(R.string.thumbKey), thumbnail);
            context.startActivity(intent);
        }

    }

    //Long click listener to delete questions
    class DeleteListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            final int position = mRecyclerView.getChildAdapterPosition(view);
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
