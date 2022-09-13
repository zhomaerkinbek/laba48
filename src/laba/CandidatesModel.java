package laba;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CandidatesModel {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public CandidatesModel() {
        this.candidates = readCandidates();
        for(var candidate: candidates){
            candidate.setId(candidates.indexOf(candidate)+ 1);
            candidate.setVotes(0);
        }
    }

    public static List<Candidate> readCandidates() {
        Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        Path PATH = Paths.get("candidates.json");
        var type = new TypeToken<List<Candidate>>() {}.getType();
        String json = "";
        try {
            json = Files.readString(PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return GSON.fromJson(json, type);
    }
}
