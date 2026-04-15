package model;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import user.Question;
import teacher.StudySet;
import user.QuestionSet;
import user.User;
import user.UserPrediction;

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
        Map<String, Question> seen = new LinkedHashMap<>();

        //questions.json search
        try {
            List<Question> flat = objectMapper.readValue(
                    new File(questionsPath),
                    new TypeReference<List<Question>>() {}
            );
            for (Question q : flat) {
                if (q != null) seen.put(Integer.toString(q.getId()), q);
            }
        } catch (IOException e) {
            // if the file dies or something
        }

        //search study sets too
        for (StudySet s : studySetMaker.getAllSets()) {
            if (s == null || s.getQuestions() == null) continue;
            for (Question q : s.getQuestions()) {
                String key = "study-" + s.getId() + "-" + q.getId();
                seen.putIfAbsent(key, q);
            }
        }

        //search question sets too
        for (QuestionSet s : questionTracker.getQuestionSets()) {
            if (s == null || s.getQuestions() == null) continue;
            for (Question q : s.getQuestions()) {
                String key = "question-" + s.getId() + "-" + q.getId();
                seen.putIfAbsent(key, q);
            }
        }

        return new ArrayList<>(seen.values());
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
                .filter(set -> set.getName() != null &&
                        set.getName().toLowerCase().contains(lowerQuery))
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

    /**
     * Returns the user's struggle vector in a map
     * Helper instead of calling UserPrediction.generate... over again
     */
    public Map<String, Double> getUserStruggleVector(User user) {
        if (user == null) {
            return new HashMap<>();
        }
        return new UserPrediction(user).generateUserStruggleVector();
    }

    /**
     * Recommends questions for a user based on their struggle tags - highest scoring questions come first
     */
    public List<Question> recommendQuestionsForUser(User user) {
        Map<String, Double> struggleVector = getUserStruggleVector(user);

        return loadAllQuestions().stream()
                .filter(q -> q.getTags() != null && !q.getTags().isEmpty())
                .sorted(Comparator.comparingDouble(
                        (Question q) -> UserPrediction.scoreQuestion(q, struggleVector)
                ).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Recommends only the top N questions for a user
     */
    public List<Question> recommendTopQuestionsForUser(User user, int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        return recommendQuestionsForUser(user).stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Scores a StudySet by averaging the struggle scores of its tags
     */
    public double scoreStudySet(StudySet set, Map<String, Double> struggleVector) {
        if (set == null || set.getTags() == null || set.getTags().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int count = 0;

        for (String tag : set.getTags()) {
            if (tag != null) {
                score += struggleVector.getOrDefault(tag.trim().toLowerCase(), 0.0);
                count++;
            }
        }

        if(count > 0){
            return score / count;
        }else{
            return 0;
        }
    }

    /**
     * Recommends study sets for a user based on their struggle tags
     */
    public List<StudySet> recommendStudySetsForUser(User user) {
        Map<String, Double> struggleVector = normalizeStruggleVector(getUserStruggleVector(user));

        return loadAllStudySets().stream()
                .filter(set -> set.getTags() != null && !set.getTags().isEmpty())
                .sorted(Comparator.comparingDouble(
                        (StudySet set) -> scoreStudySet(set, struggleVector)
                ).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Recommends top N study sets for a user
     */
    public List<StudySet> recommendTopStudySetsForUser(User user, int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        return recommendStudySetsForUser(user).stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Combines text search with personalized ranking
     * Example: user searches with a biology tag but results are sorted by what they struggle with most
     */
    public List<Question> searchQuestionsByTagsForUser(User user, List<String> searchTags) {
        Map<String, Double> struggleVector = getUserStruggleVector(user);

        return searchQuestionsByTags(searchTags).stream()
                .sorted(Comparator.comparingDouble(
                        (Question q) -> UserPrediction.scoreQuestion(q, struggleVector)
                ).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Helper to make struggle vectors more consistent
     */
    private Map<String, Double> normalizeStruggleVector(Map<String, Double> struggleVector) {
        Map<String, Double> normalized = new HashMap<>();

        for (Map.Entry<String, Double> entry : struggleVector.entrySet()) {
            if (entry.getKey() != null) {
                normalized.put(entry.getKey().trim().toLowerCase(), entry.getValue());
            }
        }
        return normalized;
    }

    /**
     * Recommends only the top N questions for a struggle vector
     */
    public List<Question> recommendTopQuestionsGivenVector(Map<String, Double> vector, int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        return recommendQuestionsGivenVector(vector).stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Recommends questions for a given struggle vector w/ tags - highest scoring questions come first
     */
    public List<Question> recommendQuestionsGivenVector(Map<String, Double> vector) {

        return loadAllQuestions().stream()
                .filter(q -> q.getTags() != null && !q.getTags().isEmpty())
                .sorted(Comparator.comparingDouble(
                        (Question q) -> UserPrediction.scoreQuestion(q, vector)
                ).reversed())
                .collect(Collectors.toList());
    }

}