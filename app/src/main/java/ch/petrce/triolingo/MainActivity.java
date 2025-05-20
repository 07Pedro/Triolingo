package ch.petrce.triolingo;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private View rootView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

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
        translationText = findViewById(R.id.translationText); // get translationText from xml
        vocabList = JsonLoader.loadVocabulary(this); // call loadfunction for vocablist

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // initialize sensorManager
        if(sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // get accelerometer sensor
        }

        showCurrentVocab(); // call function to show first vocab
    }

    private void showCurrentVocab() {
        if (vocabList != null && !vocabList.isEmpty() && currentIndex < vocabList.size()) { // check if list not empty
            Vocab vocab = vocabList.get(currentIndex); // get correct vocab (number)
            wordText.setText(vocab.getValue()); // set wordText to vocab value
            translationText.setText(vocab.getTranslation()); // set translaitonText to vocab value
            Log.d("Triolingo", "Wort geladen: " + vocab.getValue()); // log
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Links/Rechts Kippen

            if (x > 3) {
                rootView.setBackgroundColor(Color.parseColor("#553b3c")); // set background color to red
                Log.d("Triolingo", "Kippe nach links erkannt");
            } else if (x < -3) {
                rootView.setBackgroundColor(Color.parseColor("#3d553d")); // set background color to green
                Log.d("Triolingo", "Kippe nach rechts erkannt");
            } else {
                rootView.setBackgroundColor(Color.parseColor("#3c3e55")); // keep background color
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
