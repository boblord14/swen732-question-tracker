import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import question.Question;
import teacher.StudySet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchService {

    private final String questionsPath;
    private final String studySetFolder;
    private final ObjectMapper objectMapper;

    public SearchService() {
        this("src/main/questions.json", "src/main/studysets", new ObjectMapper());
    }

    /**
     * Constructor to allow a specific filepath
     */
    public SearchService(String questionsPath, String studySetFolder, ObjectMapper objectMapper) {
        this.questionsPath = questionsPath;
        this.studySetFolder = studySetFolder;
        this.objectMapper = objectMapper;
    }

    /**
     * Reads questions from the questions.json file
     */
    public List<Question> loadAllQuestions() {
        try {
            return objectMapper.readValue(
                    new File(questionsPath),
                    new TypeReference<List<Question>>() {}
            );
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Reads the StudySets from folder of jsons
     */
    public List<StudySet> loadAllStudySets() {
        List<StudySet> sets = new ArrayList<>();
        File folder = new File(studySetFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            return sets;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return sets;
        }

        for (File file : files) {
            try {
                StudySet set = objectMapper.readValue(file, StudySet.class);
                sets.add(set);
            } catch (IOException e) {
                // skip bad file
            }
        }

        return sets;
    }

    /**
     * Searches the StudySet folder for each StudySet json file
     * Works by partial match and is case-insensitive
     */
    public List<StudySet> searchStudySetsByTitle(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        String lowerQuery = query.toLowerCase();

        return loadAllStudySets().stream()
                .filter(set -> set.getTitle() != null &&
                        set.getTitle().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Searches the questions.json file for all questions with the given tags
     * Works if any tags match and is case-insensitive
     */
    public List<Question> searchQuestionsByTags(List<String> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> normalizedTags = searchTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return loadAllQuestions().stream()
                .filter(q -> q.getTags() != null && q.getTags().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .anyMatch(normalizedTags::contains))
                .collect(Collectors.toList());
    }

    /**
     * Searches the StudySet folder for each StudySet json file
     * Works by matching tags to StudySet
     */
    public List<StudySet> searchStudySetsByTags(List<String> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> normalizedTags = searchTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return loadAllStudySets().stream()
                .filter(set -> set.getTags() != null && set.getTags().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .anyMatch(normalizedTags::contains))
                .collect(Collectors.toList());
    }
}