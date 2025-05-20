package ch.petrce.triolingo;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class JsonLoader {
    private static final String FILE_NAME = "Vocabs.json"; // get asset file

    // either creat or get writable copy of vocab JSON file
    private static File getWritableVocabFile(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME); // set as file
        if (!file.exists()) {
            try (InputStream is = context.getAssets().open(FILE_NAME); // read asset file
                 OutputStream os = new FileOutputStream(file)) { // write to internal file

                byte[] buffer = new byte[1024]; // buffer for copy reason (chatgpt)
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length); // copy asset file to internal file
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // Loads 1 random vocab from internal file
    public static List<Vocab> loadVocabulary(Context context) {
        try {
            File file = getWritableVocabFile(context); // get internal file
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file)); // read file
            Type listType = new TypeToken<List<Vocab>>() {}.getType(); // define type for gson
            List<Vocab> fullList = new Gson().fromJson(reader, listType); // parse json -> put to list
            reader.close();

            if (fullList == null || fullList.size() <= 1) {
                return fullList; // return list
            }
            Collections.shuffle(fullList); // shuffle for random value
            return fullList.subList(0, 30); // return list fo items
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Save the asset list to internal storage
    public static void saveVocabulary(Context context, List<Vocab> vocabList) {
        try {
            File file = getWritableVocabFile(context); // get file
            FileWriter writer = new FileWriter(file); // write to file
            new Gson().toJson(vocabList, writer); // convert list to json
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // return full vocab list from internal storage
    public static List<Vocab> loadFullVocabulary(Context context) {
        try {
            File file = getWritableVocabFile(context); // get file
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file)); // read file
            Type listType = new TypeToken<List<Vocab>>() {}.getType(); // type for gson
            List<Vocab> fullList = new Gson().fromJson(reader, listType); // parse json -> put to list
            reader.close();
            return fullList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // return empty list if fail
        }
    }
}
