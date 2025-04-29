package org.hcmut.bkelbot;

import com.google.gson.Gson;
import lombok.Cleanup;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class State {

    private static final String PATHNAME = "moodlebotconfig.json";
    public static State instance = new State();

    public String moodleAddress = "https://e-learning.hcmut.edu.vn";
    public String moodleHost = "e-learning.hcmut.edu.vn";

    public Map<Integer, UserData> users = new HashMap<>();

    static void load() throws IOException {
        if (!new File(PATHNAME).exists()) {
            return;
        }
        @Cleanup final FileReader fileReader = new FileReader(PATHNAME);
        instance = new Gson().fromJson(fileReader, State.class);
        fileReader.close();
    }

    public static void save() throws IOException {
        @Cleanup final FileWriter fileWriter = new FileWriter(PATHNAME);
        fileWriter.write(new Gson().toJson(instance));
    }

}
