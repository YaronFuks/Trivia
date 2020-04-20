package com.yaronfuks.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yaronfuks.trivia.data.AnswerListAsyncResponse;
import com.yaronfuks.trivia.data.QuestionBank;
import com.yaronfuks.trivia.model.Question;
import com.yaronfuks.trivia.model.Score;
import com.yaronfuks.trivia.util.Prefs;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView questionTextView;
    private TextView questionCounterTextView;
    private Button trueButton;
    private Button falseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private int currentQuestionIndex = 0;
    private List<Question> questionList;
    private TextView scoreTextView;
    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;
    private TextView highestScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();
        prefs = new Prefs(this);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionCounterTextView = findViewById(R.id.counter_textView);
        questionTextView = findViewById(R.id.question_TextView);
        scoreTextView = findViewById(R.id.score_TextView);
        highestScore = findViewById(R.id.bestScoreTextView);


        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);
        scoreTextView.setText("Current Score: " + String.valueOf(score.getScore()) + " Points");
        currentQuestionIndex = prefs.getState();
        highestScore.setText("Highest Score: " + String.valueOf(prefs.getHighScore()) + " Points");
        questionList = new QuestionBank().geQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                questionTextView.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounterTextView.setText("Question: " + currentQuestionIndex + " / " + questionArrayList.size());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prev_button:
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) % questionList.size();

                    updateQuestion();
                }
                break;

            case R.id.next_button:

                goNext();
                // updateQuestion();
                break;

            case R.id.true_button:
                checkAnswer(true);
                updateQuestion();
                break;

            case R.id.false_button:
                checkAnswer(false);
                updateQuestion();
                break;


        }
    }

    private void addPoints() {
        scoreCounter += 10;
        score.setScore(scoreCounter);
        scoreTextView.setText("Current Score: " + String.valueOf(score.getScore()) + " Points");
    }

    private void removePoints() {
        scoreCounter -= 10;
        if (scoreCounter > 0) {
            score.setScore(scoreCounter);
            scoreTextView.setText("Current Score: " + String.valueOf(score.getScore()) + " Points");
        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
            scoreTextView.setText("Current Score: " + String.valueOf(score.getScore()) + " Points");
        }
    }

    private void checkAnswer(boolean userChooseCurrect) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;
        if (userChooseCurrect == answerIsTrue) {
            fadeView();
            addPoints();
            toastMessageId = R.string.correct_answer;

        } else {
            shakeAnimation();
            removePoints();
            toastMessageId = R.string.wrong_answer;

        }
        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show();

    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextView.setText(question);
        questionCounterTextView.setText("Question: " + currentQuestionIndex + " / " + questionList.size());
    }

    private void fadeView() {
        final CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.2f);
        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void goNext() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    @Override
    protected void onPause() {
        prefs.saveHighScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        super.onPause();
    }
}
