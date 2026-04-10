import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import question.Question;
import teacher.StudySet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchService {

    private static final String QUESTIONS_PATH = "src/main/questions.json";
    private static final String STUDYSET_FOLDER = "src/main/StudySets/";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load all questions from questions.json
     */
    public List<Question> loadAllQuestions() {
        try {
            return objectMapper.readValue(
                    new File(QUESTIONS_PATH),
                    new TypeReference<List<Question>>() {}
            );
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Load all StudySets from folder
     */
    public List<StudySet> loadAllStudySets() {
        List<StudySet> sets = new ArrayList<>();

        File folder = new File(STUDYSET_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            return sets;
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".json")) {
                try {
                    StudySet set = objectMapper.readValue(file, StudySet.class);
                    sets.add(set);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sets;
    }

    /**
     * 🔍 Search StudySets by title (partial + case-insensitive)
     */
    public List<StudySet> searchStudySetsByTitle(String query) {
        String lowerQuery = query.toLowerCase();

        return loadAllStudySets().stream()
                .filter(set -> set.getTitle() != null &&
                        set.getTitle().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * 🔍 Search Questions by tags (ANY match, case-insensitive)
     */
    public List<Question> searchQuestionsByTags(List<String> searchTags) {
        List<Question> allQuestions = loadAllQuestions();

        // Normalize search tags
        Set<String> normalizedTags = searchTags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allQuestions.stream()
                .filter(q -> q.getTags() != null && q.getTags().stream()
                        .map(String::toLowerCase)
                        .anyMatch(normalizedTags::contains))
                .collect(Collectors.toList());
    }
}