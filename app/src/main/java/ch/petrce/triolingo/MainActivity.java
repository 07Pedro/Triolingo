package ch.petrce.triolingo;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Collections;
import java.util.List;

import ch.petrce.triolingo.notifications.AlarmScheduler;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PERMISSION_CODE = 101;
    private static final int REQUEST_NOTIFICATIONS = 101;
    private Integer currentIndex = 0;
    private List<Vocab> vocabList;
    private TextView wordText;
    private TextView translationText;
    private FrameLayout frameLayout;
    private TextView progressText;
    private ProgressBar progressBar;
    private Button buttonMoreExercises;
    private Button buttonRemindMeLater;
    private ImageButton closeButton;
    private View rootView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean showingWord = true; // visible word otherwise translation
    private boolean waitingForFlip = false; // wait for flip after tapping screen
    private boolean flipDetected = false; // stops multiple flipping


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
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
        frameLayout = findViewById(R.id.cardLayout);
        translationText = findViewById(R.id.translationText); // get translationText from xml
        vocabList = JsonLoader.loadVocabulary(this); // call loadfunction for vocablist
        progressText = findViewById(R.id.progressText); // get progress text
        progressBar = findViewById(R.id.progressBar); // get progress bar
        buttonMoreExercises = findViewById(R.id.exerciseButton); // get button restart
        buttonRemindMeLater = findViewById(R.id.remindButton); // get button notify

        buttonMoreExercises.setOnClickListener(v -> restartExercise()); // on restart button click call restartExercise func.

        progressBar.setMax(vocabList.size()); // set progressbar size
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // initialize sensorManager
        if(sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get accelerometer sensor
        }

        translationText.setVisibility(View.INVISIBLE);
        loadFilteredVocabulary(); // get filtered vocab

        showCurrentVocab(); // call function to show first vocab

        // Touch Listener to toggel translation (chatgpt)
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!waitingForFlip) {
                    toggleWordTranslation();
                    rootView.setBackgroundColor(Color.parseColor("#3c3e55")); // turn blue
                    frameLayout.setBackgroundColor(Color.parseColor("#3c3e55")); // turn blue
                    waitingForFlip = true; // wait for the flip!
                }
                return true;
            }
            return false;
        });

        // If Android 13+, check if POST_NOTIFICATIONS permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Ask the user for permission to send notification
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_NOTIFICATIONS
                );
                return;
            }
        }

        AlarmScheduler.scheduleDailyNotification(this); // start schedule


        buttonRemindMeLater.setOnClickListener(v -> {
            // schedule notification in 2 hours
            ch.petrce.triolingo.notifications.AlarmScheduler
                    .scheduleReminderInHours(MainActivity.this, 2);

            // close app
            finish();
        });
        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            AlarmScheduler.scheduleDailyNotification(this); // start schedule if permission granted
        }
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
                    frameLayout.setBackgroundColor(Color.parseColor("#3d553d"));
                    Log.d("Triolingo", "Flip nach rechts: korrekt");
                    markCurrentVocabCorrect();
                    goToNextVocab();
                } else if (x > threshold) {
                    // flip to left (wrong)
                    flipDetected = true;
                    rootView.setBackgroundColor(Color.parseColor("#553b3c")); // set background color to red
                    frameLayout.setBackgroundColor(Color.parseColor("#553b3c"));
                    Log.d("Triolingo", "Flip nach links: falsch");
                    goToNextVocab();
                } else {
                    rootView.setBackgroundColor(Color.parseColor("#3c3e55")); // keep background color
                    frameLayout.setBackgroundColor(Color.parseColor("#3c3e55")); // keep background color
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // mark vocab as correct when guessed right
    private void markCurrentVocabCorrect() {
        Vocab current = vocabList.get(currentIndex); // get currect vocab
        current.setCorrect(current.getCorrect() + 1); // increase correct

        List<Vocab> fullList = JsonLoader.loadFullVocabulary(this); // load full list
        for (Vocab v : fullList) { // loop throu list and update current vocab
            if (v.getValue().equals(current.getValue()) &&
                    v.getTranslation().equals(current.getTranslation())) {
                v.setCorrect(current.getCorrect());
                break;
            }
        }

        JsonLoader.saveVocabulary(this, fullList); // save list with new values
    }

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
            frameLayout.setBackgroundColor(Color.parseColor("#434343")); // keep default color
            waitingForFlip = false;
        }, 800);
    }

    private void restartExercise() {
        loadFilteredVocabulary(); // get only filtered vocabs
        currentIndex = 0;
        flipDetected = false;
        waitingForFlip = false;
        showingWord = true;

        findViewById(R.id.exerciseButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.remindButton).setVisibility(View.INVISIBLE);
        frameLayout.setVisibility(View.VISIBLE);
        wordText.setVisibility(View.VISIBLE);
        translationText.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        rootView.setBackgroundColor(Color.parseColor("#434343"));
        frameLayout.setBackgroundColor(Color.parseColor("#434343"));

        showCurrentVocab();
    }

    private void showCompletionScreen() {
        wordText.setVisibility(View.INVISIBLE);
        translationText.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);

        findViewById(R.id.exerciseButton).setVisibility(View.VISIBLE); // More Exercises
        findViewById(R.id.remindButton).setVisibility(View.VISIBLE); // Remind me later
    }

    private void loadFilteredVocabulary() {
        List<Vocab> fullList = JsonLoader.loadFullVocabulary(this);
        fullList.removeIf(vocab -> vocab.getCorrect() >= 5); // remove vocabs where correct >= 5

        JsonLoader.saveVocabulary(this, fullList); // Save filtered list which removes "learned" vocabs permanently
        Collections.shuffle(fullList); // Shuffle and select 30 vocabs
        vocabList = fullList.subList(0, Math.min(vocabList.size(), fullList.size()));
    }

}
