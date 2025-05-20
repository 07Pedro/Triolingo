package ch.petrce.triolingo;

import android.content.Context;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class JsonLoader {
    public static List<Vocab> loadVocabulary(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("Vocabs.json"); // get JSON dataset
            InputStreamReader reader = new InputStreamReader(inputStream); // reader

            Type listType = new TypeToken<List<Vocab>>(){}.getType(); // helper to let GSON know to put JSON-data to java-objects
            List<Vocab> fullList = new Gson().fromJson(reader, listType); // create List of Vocabs

            if (fullList == null || fullList.size() <= 30) { // if list is smaller than 30 return all
                return fullList;
            }
            Collections.shuffle(fullList); // shuffel full list
            return fullList.subList(0, 30); // get first 30 from shuffled list

        } catch (Exception e) {
            e.printStackTrace(); // exeption
            return null;
        }
    }
}
