package de.thm.thmflashcards.persistance;

/**
 * Created by Farea on 28.11.2017.
 */

public class Flashcard {

    public static final int QUESTION_TYPE = 0;
    public static final int ANSWER_TYPE = 1;

    private int id;
    private String question;
    private String answer;
    private String answerImagePath;
    private int noCorrect;
    private int noWrong;
    private int currentType;

    public Flashcard(String question, String answer, int noCorrect, int noWrong) {
        this.question = question;
        this.answer = answer;
        this.noCorrect = noCorrect;
        this.noWrong = noWrong;
        this.currentType = QUESTION_TYPE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
