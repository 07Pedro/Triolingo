package ch.petrce.triolingo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Integer currentIndex = 0;
    private List<Vocab> vocabList;
    private TextView wordText;
    private TextView translationText;


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
        wordText = findViewById(R.id.wordText); // get wordText from xml
        translationText = findViewById(R.id.translationText); // get translationText from xml
        vocabList = JsonLoader.loadVocabulary(this); // call loadfunction for vocablist

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

}