package tests;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.SearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import user.Question;
import teacher.StudySet;
import user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static tests.SearchServiceTest.TestableSearchService.makeQuestion;
import static tests.SearchServiceTest.TestableSearchService.makeStudySet;

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

    /**
     * Mini class to help test everything in search service
     */
    static class TestableSearchService extends SearchService {
        private List<Question> questions = new ArrayList<>();
        private List<StudySet> studySets = new ArrayList<>();
        private Map<String, Double> struggleVector = new HashMap<>();

        TestableSearchService() {
            super("", "", new ObjectMapper());
        }

        void setQuestions(List<Question> questions) {
            this.questions = questions;
        }

        void setStudySets(List<StudySet> studySets) {
            this.studySets = studySets;
        }

        void setStruggleVector(Map<String, Double> struggleVector) {
            this.struggleVector = struggleVector;
        }

        @Override
        public List<Question> loadAllQuestions() {
            return questions;
        }

        @Override
        public List<StudySet> loadAllStudySets() {
            return studySets;
        }

        @Override
        public Map<String, Double> getUserStruggleVector(User user) {
            return struggleVector;
        }

        static Question makeQuestion(int id, String text, String... tags) {
            Question q = new Question();
            q.setId(id);
            q.setText(text);
            q.setAnswer("answer");
            q.setTags(tags == null ? null : new ArrayList<>(Arrays.asList(tags)));
            return q;
        }

        static StudySet makeStudySet(String name, String... tags) {
            StudySet s = new StudySet();
            s.setName(name);
            s.setSubject("CS");
            s.setCreator("teacher");
            s.setTags(tags == null ? null : new ArrayList<>(Arrays.asList(tags)));
            s.setQuestionSet(new ArrayList<>());
            return s;
        }
    }

    @Test
    void searchStudySetsByTags_returnsMatchingStudySetsCaseInsensitive() {
        List<StudySet> results = searchService.searchStudySetsByTags(Arrays.asList("JAVA"));

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(s -> "Java Basics".equals(s.getName())));
        assertTrue(results.stream().anyMatch(s -> "Advanced JAVA Collections".equals(s.getName())));
    }

    @Test
    void searchStudySetsByTags_returnsEmptyListForNullOrEmptyInput() {
        assertTrue(searchService.searchStudySetsByTags(null).isEmpty());
        assertTrue(searchService.searchStudySetsByTags(new ArrayList<>()).isEmpty());
    }

    @Test
    void getUserStruggleVector_returnsEmptyMapForNullUser() {
        Map<String, Double> result = searchService.getUserStruggleVector(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void scoreStudySet_returnsZeroForNullSet() {
        assertEquals(0.0, searchService.scoreStudySet(null, Map.of("java", 2.0)));
    }

    @Test
    void scoreStudySet_returnsZeroForSetWithNoTags() {
        StudySet set = makeStudySet("Empty");
        set.setTags(new ArrayList<>());

        assertEquals(0.0, searchService.scoreStudySet(set, Map.of("java", 2.0)));
    }

    @Test
    void scoreStudySet_averagesMatchingTagsAndNormalizesCaseWhitespace() {
        StudySet set = makeStudySet("Java + OOP", " Java ", "OOP", "missing");
        double score = searchService.scoreStudySet(
                set,
                Map.of("java", 3.0, "oop", 1.0)
        );

        assertEquals(4.0 / 3.0, score, 0.0001);
    }

    @Test
    void recommendTopQuestionsForUser_returnsEmptyWhenCountIsZeroOrLess() {
        TestableSearchService service = new TestableSearchService();

        assertTrue(service.recommendTopQuestionsForUser(new User(), 0).isEmpty());
        assertTrue(service.recommendTopQuestionsForUser(new User(), -1).isEmpty());
    }

    @Test
    void recommendTopStudySetsForUser_returnsEmptyWhenCountIsZeroOrLess() {
        TestableSearchService service = new TestableSearchService();

        assertTrue(service.recommendTopStudySetsForUser(new User(), 0).isEmpty());
        assertTrue(service.recommendTopStudySetsForUser(new User(), -1).isEmpty());
    }

    @Test
    void recommendTopQuestionsGivenVector_returnsEmptyWhenCountIsZeroOrLess() {
        TestableSearchService service = new TestableSearchService();

        assertTrue(service.recommendTopQuestionsGivenVector(Map.of("java", 1.0), 0).isEmpty());
        assertTrue(service.recommendTopQuestionsGivenVector(Map.of("java", 1.0), -5).isEmpty());
    }

    @Test
    void recommendQuestionsGivenVector_sortsHighestScoringQuestionsFirst_andSkipsNullOrEmptyTags() {
        TestableSearchService service = new TestableSearchService();

        Question javaQuestion = makeQuestion(1, "Java Q", "java");
        Question oopQuestion = makeQuestion(2, "OOP Q", "oop");
        Question noTags = makeQuestion(3, "No Tags");
        noTags.setTags(new ArrayList<>());

        service.setQuestions(Arrays.asList(oopQuestion, noTags, javaQuestion));

        List<Question> results = service.recommendQuestionsGivenVector(
                Map.of("java", 5.0, "oop", 2.0)
        );

        assertEquals(2, results.size());
        assertEquals("Java Q", results.get(0).getText());
        assertEquals("OOP Q", results.get(1).getText());
    }

    @Test
    void recommendTopQuestionsGivenVector_limitsResultCount() {
        TestableSearchService service = new TestableSearchService();

        service.setQuestions(Arrays.asList(
                makeQuestion(1, "Java Q", "java"),
                makeQuestion(2, "OOP Q", "oop"),
                makeQuestion(3, "Algo Q", "algorithms")
        ));

        List<Question> results = service.recommendTopQuestionsGivenVector(
                Map.of("java", 5.0, "oop", 2.0, "algorithms", 1.0),
                2
        );

        assertEquals(2, results.size());
    }

    @Test
    void recommendQuestionsForUser_usesUserStruggleVectorToSortQuestions() {
        TestableSearchService service = new TestableSearchService();

        service.setQuestions(Arrays.asList(
                makeQuestion(1, "OOP Q", "oop"),
                makeQuestion(2, "Java Q", "java"),
                makeQuestion(3, "No Tags")
        ));
        service.setStruggleVector(Map.of("java", 4.0, "oop", 1.0));

        List<Question> results = service.recommendQuestionsForUser(new User());

        assertEquals(2, results.size());
        assertEquals("Java Q", results.get(0).getText());
        assertEquals("OOP Q", results.get(1).getText());
    }

    @Test
    void recommendTopQuestionsForUser_limitsSortedQuestionResults() {
        TestableSearchService service = new TestableSearchService();

        service.setQuestions(Arrays.asList(
                makeQuestion(1, "OOP Q", "oop"),
                makeQuestion(2, "Java Q", "java"),
                makeQuestion(3, "Algo Q", "algorithms")
        ));
        service.setStruggleVector(Map.of("java", 5.0, "oop", 2.0, "algorithms", 1.0));

        List<Question> results = service.recommendTopQuestionsForUser(new User(), 2);

        assertEquals(2, results.size());
        assertEquals("Java Q", results.get(0).getText());
    }

    @Test
    void recommendStudySetsForUser_sortsByNormalizedStruggleVector() {
        TestableSearchService service = new TestableSearchService();

        service.setStudySets(Arrays.asList(
                makeStudySet("OOP Set", "oop"),
                makeStudySet("Java Set", "JAVA"),
                makeStudySet("No Tags")
        ));
        service.getClass(); // no-op, just keeping style consistent
        service.setStruggleVector(Map.of(" java ", 4.0, "oop", 1.0));

        List<StudySet> results = service.recommendStudySetsForUser(new User());

        assertEquals(2, results.size());
        assertEquals("Java Set", results.get(0).getName());
        assertEquals("OOP Set", results.get(1).getName());
    }

    @Test
    void recommendTopStudySetsForUser_limitsSortedStudySets() {
        TestableSearchService service = new TestableSearchService();

        service.setStudySets(Arrays.asList(
                makeStudySet("Java Set", "java"),
                makeStudySet("OOP Set", "oop"),
                makeStudySet("Algo Set", "algorithms")
        ));
        service.setStruggleVector(Map.of("java", 5.0, "oop", 2.0, "algorithms", 1.0));

        List<StudySet> results = service.recommendTopStudySetsForUser(new User(), 2);

        assertEquals(2, results.size());
        assertEquals("Java Set", results.get(0).getName());
    }

    @Test
    void searchQuestionsByTagsForUser_filtersByTagsThenSortsByUserStruggle() {
        TestableSearchService service = new TestableSearchService();

        service.setQuestions(Arrays.asList(
                makeQuestion(1, "OOP Q", "oop"),
                makeQuestion(2, "Java Q", "java"),
                makeQuestion(3, "Algo Q", "algorithms")
        ));
        service.setStruggleVector(Map.of("java", 5.0, "oop", 2.0));

        List<Question> results = service.searchQuestionsByTagsForUser(
                new User(),
                Arrays.asList("java", "oop")
        );

        assertEquals(2, results.size());
        assertEquals("Java Q", results.get(0).getText());
        assertEquals("OOP Q", results.get(1).getText());
    }
}