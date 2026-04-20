package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SetSession;
import model.StudySetMaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import teacher.StudySet;
import user.Question;
import user.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudySetMakerTest {

    private static final String SETS_PATH = "src/main/sets.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetSetsFile() throws IOException {
        File file = new File(SETS_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            assertTrue(parent.mkdirs() || parent.exists());
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("");
        }
    }

    private StudySet makeSet(int id, String name, String creator, String subject,
                             List<String> tags, List<Question> questions) {
        StudySet set = new StudySet();
        set.setId(id);
        set.setName(name);
        set.setCreator(creator);
        set.setSubject(subject);
        set.setTags(tags);
        set.setQuestionSet(questions);
        return set;
    }

    private Question makeQuestion(int id, String text, String answer, String... tags) {
        Question q = new Question(id, text, answer);
        if (tags != null) {
            q.setTags(new ArrayList<>(Arrays.asList(tags)));
        }
        return q;
    }

    private void writeSets(StudySet... sets) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SETS_PATH), sets);
    }

    @Test
    void testGetAllSetsEmptyWhenFileBlank() {
        StudySet[] result = StudySetMaker.getAllSets();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetAllSetsReturnsSavedSets() throws IOException {
        StudySet s1 = makeSet(1, "Math", "teacher1", "Math",
                new ArrayList<>(List.of("algebra")), new ArrayList<>());
        StudySet s2 = makeSet(2, "Science", "teacher2", "Science",
                new ArrayList<>(List.of("bio")), new ArrayList<>());

        writeSets(s1, s2);

        StudySet[] result = StudySetMaker.getAllSets();

        assertEquals(2, result.length);
        assertEquals("Math", result[0].getName());
        assertEquals("Science", result[1].getName());
    }

    @Test
    void testGetSetReturnsMatchingCreatorAndTitle() throws IOException {
        StudySet s1 = makeSet(1, "Exam Prep", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        StudySet s2 = makeSet(2, "Exam Prep", "teacher2", "Math",
                new ArrayList<>(), new ArrayList<>());

        writeSets(s1, s2);

        StudySet result = StudySetMaker.getSet("teacher2", "Exam Prep");

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("teacher2", result.getCreator());
    }

    @Test
    void testGetSetReturnsNullWhenNoMatch() throws IOException {
        StudySet s1 = makeSet(1, "Exam Prep", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());

        writeSets(s1);

        StudySet result = StudySetMaker.getSet("teacherX", "Nope");

        assertNull(result);
    }

    @Test
    void testGetSetByIdFound() throws IOException {
        StudySet s1 = makeSet(1, "Set One", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        StudySet s2 = makeSet(2, "Set Two", "teacher2", "Science",
                new ArrayList<>(), new ArrayList<>());

        writeSets(s1, s2);

        StudySet result = StudySetMaker.getSetById(2);

        assertNotNull(result);
        assertEquals("Set Two", result.getName());
    }

    @Test
    void testGetSetByIdNotFound() throws IOException {
        StudySet s1 = makeSet(1, "Set One", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());

        writeSets(s1);

        StudySet result = StudySetMaker.getSetById(999);

        assertNull(result);
    }

    @Test
    void testGetSetCountReturnsOnlyMatchingCreatorCount() throws IOException {
        StudySet s1 = makeSet(1, "A", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        StudySet s2 = makeSet(2, "B", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        StudySet s3 = makeSet(3, "C", "teacher2", "Math",
                new ArrayList<>(), new ArrayList<>());

        writeSets(s1, s2, s3);

        long count = StudySetMaker.getSetCount("teacher1");

        assertEquals(2, count);
    }

    @Test
    void testCreateSetAssignsNextId() throws IOException {
        StudySet existing = makeSet(7, "Old", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        writeSets(existing);

        User user = new User(1, "teacher2", "pass", true);
        StudySet created = StudySetMaker.createSet(new ArrayList<>(), user, "New Set", "Science");

        assertNotNull(created);
        assertEquals(8, created.getId());
        assertEquals("teacher2", created.getCreator());
        assertEquals("New Set", created.getName());
        assertEquals("Science", created.getSubject());
    }

    @Test
    void testCreateSetWithTagsPersistsTags() throws IOException {
        User user = new User(1, "teacher1", "pass", true);
        List<String> tags = new ArrayList<>(List.of("math", "review"));

        StudySet created = StudySetMaker.createSet(new ArrayList<>(), user, "Tagged Set", "Math", tags);
        StudySet loaded = StudySetMaker.getSet("teacher1", "Tagged Set");

        assertNotNull(created);
        assertNotNull(loaded);
        assertEquals(tags, loaded.getTags());
    }

    @Test
    void testAddTagsAppendsToExistingTags() throws IOException {
        StudySet original = makeSet(
                1,
                "Math Set",
                "teacher1",
                "Math",
                new ArrayList<>(List.of("algebra", "review")),
                new ArrayList<>()
        );
        writeSets(original);

        StudySet updated = StudySetMaker.addTags("teacher1", "Math Set",
                new ArrayList<>(List.of("final", "practice")));

        assertNotNull(updated);
        assertEquals(4, updated.getTags().size());
        assertTrue(updated.getTags().contains("algebra"));
        assertTrue(updated.getTags().contains("review"));
        assertTrue(updated.getTags().contains("final"));
        assertTrue(updated.getTags().contains("practice"));
    }

    @Test
    void testAddQuestionToStudySetSuccessWithTags() throws IOException {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(makeQuestion(3, "Old Q", "Old A", "old"));

        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), questions);
        writeSets(set);

        boolean result = StudySetMaker.addQuestionToStudySet(
                1,
                "New Q",
                "New A",
                new ArrayList<>(List.of("new", "tag"))
        );

        StudySet updated = StudySetMaker.getSetById(1);

        assertTrue(result);
        assertNotNull(updated);
        assertEquals(2, updated.getQuestions().size());

        Question added = updated.getQuestions().get(1);
        assertEquals(4, added.getId());
        assertEquals("New Q", added.getText());
        assertEquals("New A", added.getAnswer());
        assertEquals(List.of("new", "tag"), added.getTags());
    }

    @Test
    void testAddQuestionToStudySetSuccessWhenQuestionListNull() throws IOException {
        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), null);
        writeSets(set);

        boolean result = StudySetMaker.addQuestionToStudySet(1, "Q1", "A1", null);

        StudySet updated = StudySetMaker.getSetById(1);

        assertTrue(result);
        assertNotNull(updated);
        assertNotNull(updated.getQuestions());
        assertEquals(1, updated.getQuestions().size());
        assertEquals(1, updated.getQuestions().get(0).getId());
    }

    @Test
    void testAddQuestionToStudySetReturnsFalseWhenSetNotFound() throws IOException {
        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        writeSets(set);

        boolean result = StudySetMaker.addQuestionToStudySet(999, "Q", "A", null);

        assertFalse(result);
        assertEquals(0, StudySetMaker.getSetById(1).getQuestions().size());
    }

    @Test
    void testRemoveQuestionFromStudySetSuccess() throws IOException {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(makeQuestion(1, "Q1", "A1"));
        questions.add(makeQuestion(2, "Q2", "A2"));

        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), questions);
        writeSets(set);

        boolean result = StudySetMaker.removeQuestionFromStudySet(1, 1);

        StudySet updated = StudySetMaker.getSetById(1);

        assertTrue(result);
        assertNotNull(updated);
        assertEquals(1, updated.getQuestions().size());
        assertEquals(2, updated.getQuestions().get(0).getId());
    }

    @Test
    void testRemoveQuestionFromStudySetReturnsFalseWhenQuestionMissing() throws IOException {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(makeQuestion(1, "Q1", "A1"));

        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), questions);
        writeSets(set);

        boolean result = StudySetMaker.removeQuestionFromStudySet(1, 999);

        assertFalse(result);
        assertEquals(1, StudySetMaker.getSetById(1).getQuestions().size());
    }

    @Test
    void testRemoveQuestionFromStudySetReturnsFalseWhenSetNotFound() throws IOException {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(makeQuestion(1, "Q1", "A1"));

        StudySet set = makeSet(1, "Set", "teacher1", "Math",
                new ArrayList<>(), questions);
        writeSets(set);

        boolean result = StudySetMaker.removeQuestionFromStudySet(999, 1);

        assertFalse(result);
    }

    @Test
    void testCreateStudySetSessionSuccess() throws IOException {
        StudySet set = makeSet(5, "Session Set", "teacher1", "Math",
                new ArrayList<>(), new ArrayList<>());
        writeSets(set);

        User user = new User(1, "student1", "pass", false);
        SetSession session = StudySetMaker.createStudySetSession(5, user);

        assertNotNull(session);
        assertEquals(5, session.getSetId());
        assertTrue(session.getIsStudySet());
    }

    @Test
    void testCreateStudySetSessionReturnsNullWhenSetMissing() {
        User user = new User(1, "student1", "pass", false);

        SetSession session = StudySetMaker.createStudySetSession(999, user);

        assertNull(session);
    }
}