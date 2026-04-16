package tests;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.SearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import user.Question;
import teacher.StudySet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    @TempDir
    Path tempDir;

    private File questionsFile;
    private File studySetsFolder;
    private SearchService searchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Path questionFile = Paths.get("src/main/questions.json");
    private final Path studySetFile = Paths.get("src/main/sets.json");
    private final Path questionSetFile = Paths.get("src/main/questionSets.json");


    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(questionFile);
        Files.deleteIfExists(studySetFile);
        Files.deleteIfExists(questionSetFile);

        questionsFile = tempDir.resolve("questions.json").toFile();
        studySetsFolder = tempDir.resolve("StudySets").toFile();
        assertTrue(studySetsFolder.mkdirs());

        writeQuestionsFile();
        writeStudySetFiles();

        searchService = new SearchService(
                questionsFile.getAbsolutePath(),
                studySetsFolder.getAbsolutePath(),
                objectMapper
        );
    }

    @Test
    void loadAllQuestions_returnsAllQuestionsFromJson() {
        List<Question> questions = searchService.loadAllQuestions();

        assertEquals(3, questions.size());
        assertEquals("What is polymorphism?", questions.get(0).getText());
    }

    @Test
    void loadAllStudySets_returnsAllStudySetsFromFolder() {
        List<StudySet> studySets = searchService.loadAllStudySets();

        assertEquals(3, studySets.size());
    }

    @Test
    void searchStudySetsByTitle_returnsPartialCaseInsensitiveMatches() {
        List<StudySet> results = searchService.searchStudySetsByTitle("java");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(s -> "Java Basics".equals(s.getName())));
        assertTrue(results.stream().anyMatch(s -> "Advanced JAVA Collections".equals(s.getName())));
    }

    @Test
    void searchStudySetsByTitle_returnsEmptyListWhenNoMatches() {
        List<StudySet> results = searchService.searchStudySetsByTitle("biology");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchStudySetsByTitle_returnsEmptyListForBlankQuery() {
        List<StudySet> results = searchService.searchStudySetsByTitle("   ");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchQuestionsByTags_returnsQuestionsWithAnyMatchingTag() {
        List<Question> results = searchService.searchQuestionsByTags(
                Arrays.asList("oop", "sorting")
        );

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(q -> q.getText().equals("What is polymorphism?")));
        assertTrue(results.stream().anyMatch(q -> q.getText().equals("Explain merge sort.")));
    }

    @Test
    void searchQuestionsByTags_isCaseInsensitive() {
        List<Question> results = searchService.searchQuestionsByTags(
                Arrays.asList("JaVa")
        );

        assertEquals(1, results.size());
    }

    @Test
    void searchQuestionsByTags_returnsEmptyListWhenNoTagMatches() {
        List<Question> results = searchService.searchQuestionsByTags(
                Arrays.asList("chemistry")
        );

        assertTrue(results.isEmpty());
    }

    @Test
    void searchQuestionsByTags_ignoresQuestionsWithNullTags() {
        List<Question> results = searchService.searchQuestionsByTags(
                Arrays.asList("oop", "java", "sorting")
        );

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(q -> q.getText().equals("What is two + two?")));
    }

    @Test
    void searchQuestionsByTags_returnsEmptyListForEmptySearchTags() {
        List<Question> results = searchService.searchQuestionsByTags(
                new ArrayList<>()
        );

        assertTrue(results.isEmpty());
    }

    /**
     * Temporary file writes
     */
    private void writeQuestionsFile() throws IOException {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setId(1);
        q1.setTags(new ArrayList<>(Arrays.asList("Java", "OOP")));
        q1.setText("What is polymorphism?");
        q1.setAnswer("The ability of objects to take many forms.");

        Question q2 = new Question();
        q2.setId(2);
        q2.setTags(new ArrayList<>(Arrays.asList("Sorting", "Algorithms")));
        q2.setText("Explain merge sort.");
        q2.setAnswer("A divide-and-conquer sorting algorithm.");

        Question q3 = new Question();
        q3.setId(3);
        q3.setTags(null);
        q3.setText("What is two + two?");
        q3.setAnswer("four");

        questions.add(q1);
        questions.add(q2);
        questions.add(q3);

        objectMapper.writeValue(questionsFile, questions);
    }

    /**
     * Temporary file writes
     */
    private void writeStudySetFiles() throws IOException {
        StudySet s1 = new StudySet();
        s1.setName("Java Basics");
        s1.setSubject("CS");
        s1.setCreator("teacher1");
        s1.setTags(new ArrayList<>(Arrays.asList("java", "intro")));
        s1.setQuestionSet(new ArrayList<>());

        StudySet s2 = new StudySet();
        s2.setName("Advanced JAVA Collections");
        s2.setSubject("CS");
        s2.setCreator("teacher2");
        s2.setTags(new ArrayList<>(Arrays.asList("java", "collections")));
        s2.setQuestionSet(new ArrayList<>());

        StudySet s3 = new StudySet();
        s3.setName("Discrete Math Review");
        s3.setSubject("Math");
        s3.setCreator("teacher3");
        s3.setTags(new ArrayList<>(Arrays.asList("math", "logic")));
        s3.setQuestionSet(new ArrayList<>());

        objectMapper.writeValue(new File(studySetsFolder, "set1.json"), s1);
        objectMapper.writeValue(new File(studySetsFolder, "set2.json"), s2);
        objectMapper.writeValue(new File(studySetsFolder, "set3.json"), s3);
    }
}