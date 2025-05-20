package ch.petrce.triolingo;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Integer currentIndex = 0;
    private List<Vocab> vocabList;
    private TextView wordText;
    private TextView translationText;
    private LinearLayout linearLayout;
    private TextView progressText;
    private ProgressBar progressBar;
    private View buttonMoreExercises;
    private View buttonRemindMeLater;

    private View rootView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean showingWord = true; // visible word otherwise translation
    private boolean waitingForFlip = false; // wait for flip after tapping screen
    private boolean flipDetected = false; // stops multiple flipping


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rootView = findViewById(R.id.main); // get layout from xml
        wordText = findViewById(R.id.wordText); // get wordText from xml
        linearLayout = findViewById(R.id.cardLayout);
        translationText = findViewById(R.id.translationText); // get translationText from xml
        vocabList = JsonLoader.loadVocabulary(this); // call loadfunction for vocablist
        progressText = findViewById(R.id.progressText); // get progress text
        progressBar = findViewById(R.id.progressBar); // get progress bar
        buttonMoreExercises = findViewById(R.id.button2); // get button restart
        buttonRemindMeLater = findViewById(R.id.button4); // get button notify

        buttonMoreExercises.setOnClickListener(v -> restartExercise()); // on restart button click call restartExercise func.

        progressBar.setMax(vocabList.size()); // set progressbar size
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // initialize sensorManager
        if(sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get accelerometer sensor
        }


        translationText.setVisibility(View.INVISIBLE); // hide translation element

        showCurrentVocab(); // call function to show first vocab

        // Touch Listener to toggel translation (chatgpt)
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!waitingForFlip) {
                    toggleWordTranslation();
                    rootView.setBackgroundColor(Color.parseColor("#3c3e55")); // turn blue
                    linearLayout.setBackgroundColor(Color.parseColor("#3c3e55")); // turn blue
                    waitingForFlip = true; // wait for the flip!
                }
                return true;
            }
            return false;
        });
    }

    private void showCurrentVocab() {
        if (vocabList != null && !vocabList.isEmpty() && currentIndex < vocabList.size()) { // check if list not empty
            Vocab vocab = vocabList.get(currentIndex); // get correct vocab (number)
            wordText.setText(vocab.getValue()); // set wordText to vocab value
            translationText.setText(vocab.getTranslation()); // set translaitonText to vocab value
            wordText.setVisibility(View.VISIBLE); // make text visible
            translationText.setVisibility(View.INVISIBLE); // make translation invisible
            showingWord = true;

            updateProgress();
            Log.d("Triolingo", "Wort geladen: " + vocab.getValue());
        }
    }

    // update progres
    private void updateProgress() {
        progressText.setText((currentIndex + 1) + " / " + vocabList.size());
        progressBar.setMax(vocabList.size());
        progressBar.setProgress(currentIndex + 1);
    }

    // change visibility of the translation
    private void toggleWordTranslation() {
        if (showingWord) {
            wordText.setVisibility(View.INVISIBLE);
            translationText.setVisibility(View.VISIBLE);
            showingWord = false;
        } else {
            wordText.setVisibility(View.VISIBLE);
            translationText.setVisibility(View.INVISIBLE);
            showingWord = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this); // unregister sensor listener
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!waitingForFlip) return; // only when waiting for the filp

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Left/Right flip
            float threshold = 3.0f;

            if (x > -1.5f && x < 1.5f) { // define range so no vocabs are skipped
                flipDetected = false; // ready for another flip
            }

            if (!flipDetected) {

                if (x < -threshold) {
                    // Flip to right (correct)
                    flipDetected = true;
                    rootView.setBackgroundColor(Color.parseColor("#3d553d")); // set background color to green
                    linearLayout.setBackgroundColor(Color.parseColor("#3d553d"));
                    Log.d("Triolingo", "Flip nach rechts: korrekt");
                    markCurrentVocabCorrect();
                    goToNextVocab();
                } else if (x > threshold) {
                    // flip to left (wrong)
                    flipDetected = true;
                    rootView.setBackgroundColor(Color.parseColor("#553b3c")); // set background color to red
                    linearLayout.setBackgroundColor(Color.parseColor("#553b3c"));
                    Log.d("Triolingo", "Flip nach links: falsch");
                    goToNextVocab();
                } else {
                    rootView.setBackgroundColor(Color.parseColor("#3c3e55")); // keep background color
                    linearLayout.setBackgroundColor(Color.parseColor("#3c3e55")); // keep background color
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void markCurrentVocabCorrect() {} // TODO implemetn the increase in the JSON file

    // switch to next vocab
    private void goToNextVocab() {
        rootView.postDelayed(() -> {
            currentIndex++; // go to next index
            if (currentIndex >= vocabList.size()) {
                Log.d("Triolingo", "Ende der Liste");
                showCompletionScreen(); // show the competion screen
            }
            showCurrentVocab();
            rootView.setBackgroundColor(Color.parseColor("#434343")); // keep default color
            linearLayout.setBackgroundColor(Color.parseColor("#434343")); // keep default color
            waitingForFlip = false;
        }, 800);
    }

    private void restartExercise() {
        vocabList = JsonLoader.loadVocabulary(this);
        currentIndex = 0;
        flipDetected = false;
        waitingForFlip = false;
        showingWord = true;

        findViewById(R.id.button2).setVisibility(View.INVISIBLE);
        findViewById(R.id.button4).setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        wordText.setVisibility(View.VISIBLE);
        translationText.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        rootView.setBackgroundColor(Color.parseColor("#434343"));
        linearLayout.setBackgroundColor(Color.parseColor("#434343"));

        showCurrentVocab();
    }

    private void showCompletionScreen() {
        wordText.setVisibility(View.INVISIBLE);
        translationText.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);

        findViewById(R.id.button2).setVisibility(View.VISIBLE); // More Exercises
        findViewById(R.id.button4).setVisibility(View.VISIBLE); // Remind me later
    }
}
