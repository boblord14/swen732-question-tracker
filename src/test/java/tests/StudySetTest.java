package tests;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.QuestionMaker;
import model.QuestionTracker;
import model.SetSession;
import model.StudySetMaker;
import user.Question;
import teacher.StudySet;
import user.User;

class StudySetTest {

    //Resets the json file before each test
    @BeforeEach
    void clearJson() {
        try (FileWriter writer = new FileWriter("src/main/sets.json")) {
            writer.write("");   // or "[]" if you want an empty array
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //A test that creates a study set
    @Test
    void testCreateStudySet(){
        String username = "admin";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = StudySetMaker.createSet(list, user1, "Math test Prep", "Math");

        assertNotNull(set1);

        String answer1 = set1.getQuestions().get(0).getAnswer();
        String answer2 = set1.getQuestions().get(1).getAnswer();

        assertEquals("four", answer1);
        assertEquals("seven", answer2);
    }

    //Creates a study set with a list of tags
    @Test
    void testCreateStudySetWithTags(){
        String username = "admin";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Math");
        tags.add("test prep");

        StudySet set1 = StudySetMaker.createSet(list, user1, "Math test Prep", "Math", tags);

        ArrayList<String> tagList = (ArrayList<String>) set1.getTags();
        assertEquals(tagList, tags);
    }

    //Adds tags to a study set
    @Test
    void testCreateStudySetAddTags(){
        String username = "admin";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Student Aid");
        tags.add("test prep");

        StudySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySet set2 = StudySetMaker.addTags(user1.getUsername(), "Math test Prep", tags);
        ArrayList<String> tagList = (ArrayList<String>) set2.getTags();

        assertEquals(tagList, tags);
    }

    //Tests adding a tag to a list that already had tags
    @Test
    void testCreateStudySetAddTagToAnExistingList(){
        String username = "admin";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Student Aid");
        tags.add("test prep");

        ArrayList<String> newTag = new ArrayList<>();
        newTag.add("Math");

        StudySetMaker.createSet(list, user1, "Math test Prep", "Math", tags);
        StudySet set2 = StudySetMaker.addTags(user1.getUsername(), "Math test Prep", newTag);
        ArrayList<String> tagList = (ArrayList<String>) set2.getTags();

        System.out.println(tagList.get(2));

        assertEquals(tagList.get(2), newTag.get(0));
    }

    //Retrieves a users study set with a specific name
    @Test
    void testRetreiveStudySet(){
        String username = "admin";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySet set2 = StudySetMaker.getSet(user1.getUsername(), "Math test Prep");

        assertNotNull(set2);

        String answer1 = set2.getQuestions().get(0).getAnswer();
        String answer2 = set2.getQuestions().get(1).getAnswer();

        assertEquals("four", answer1);
        assertEquals("seven", answer2);
    }

    // A test that creates multiple study sets
    @Test
    void testCreateTwoStudySets(){
        String username = "phantom";
        String password = "admin123";
        QuestionTracker.signUp(username, password, true);
        User user1 = QuestionTracker.logIn(username, password);
        Question q1 = QuestionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = QuestionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySetMaker.createSet(list, user1, "Moth test Prep", "Math");

        long count = StudySetMaker.getSetCount(user1.getUsername());
        System.out.println(count);
        assertEquals(2, count);
    }

    @Test
    void testGetSetByIdNotFound() {
        StudySet result = StudySetMaker.getSetById(999);

        assertNull(result);
    }

    @Test
    void testGetAllSetsEmpty() {
        StudySet[] sets = StudySetMaker.getAllSets();

        assertNotNull(sets);
        assertEquals(0, sets.length);
    }
    
    @Test
    void testAddQuestionToStudySet() {

        String username = "u1";
        String password = "p1";

        QuestionTracker.signUp(username, password, true);
        User user = QuestionTracker.logIn(username, password);

        ArrayList<Question> qs = new ArrayList<>();
        StudySet set = StudySetMaker.createSet(qs, user, "Set1", "Math");

        boolean result = StudySetMaker.addQuestionToStudySet(
                set.getId(),
                "Q1",
                "A1",
                null
        );

        assertTrue(result);

        StudySet updated = StudySetMaker.getSetById(set.getId());
        assertEquals(1, updated.getQuestions().size());
    }

    @Test
    void testRemoveQuestionFromStudySet() {

        String username = "u2";
        String password = "p2";

        QuestionTracker.signUp(username, password, true);
        User user = QuestionTracker.logIn(username, password);

        ArrayList<Question> qs = new ArrayList<>();
        StudySet set = StudySetMaker.createSet(qs, user, "Set2", "Math");

        StudySetMaker.addQuestionToStudySet(set.getId(), "Q1", "A1", null);

        StudySet updated = StudySetMaker.getSetById(set.getId());
        int qId = updated.getQuestions().get(0).getId();

        boolean removed = StudySetMaker.removeQuestionFromStudySet(set.getId(), qId);

        assertTrue(removed);

        StudySet after = StudySetMaker.getSetById(set.getId());
        assertEquals(0, after.getQuestions().size());
    }

    @Test
    void testRemoveQuestionFromStudySetFail() {

        boolean result = StudySetMaker.removeQuestionFromStudySet(999, 999);

        assertFalse(result);
    }

    @Test
    void testCreateSetWithTags() {

        String username = "u3";
        String password = "p3";

        QuestionTracker.signUp(username, password, true);
        User user = QuestionTracker.logIn(username, password);

        ArrayList<Question> qs = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tags.add("math");

        StudySet set = StudySetMaker.createSet(qs, user, "TaggedSet", "Math", tags);

        assertNotNull(set);
        assertEquals(tags, set.getTags());
    }

    @Test
    void testGetSetCount() {

        String username = "u4";
        String password = "p4";

        QuestionTracker.signUp(username, password, true);
        User user = QuestionTracker.logIn(username, password);

        StudySetMaker.createSet(new ArrayList<>(), user, "S1", "Math");
        StudySetMaker.createSet(new ArrayList<>(), user, "S2", "Math");

        long count = StudySetMaker.getSetCount(username);

        assertEquals(2, count);
    }

    @Test
    void testCreateStudySetSessionNull() {
        SetSession session = StudySetMaker.createStudySetSession(999, new User(1, "x", "y", true));

        assertNull(session);
    }

    @Test
    void testCreateStudySetSessionSuccess() {

        String username = "u5";
        String password = "p5";

        QuestionTracker.signUp(username, password, true);
        User user = QuestionTracker.logIn(username, password);

        StudySet set = StudySetMaker.createSet(new ArrayList<>(), user, "SessionSet", "Math");

        SetSession session = StudySetMaker.createStudySetSession(set.getId(), user);

        assertNotNull(session);
    }

    @Test
    void testDefaultConstructorAndSetters() {
        StudySet set = new StudySet();

        set.setId(42);
        set.setName("Biology Review");
        set.setCreator("teacher1");
        set.setSubject("Biology");

        ArrayList<String> tags = new ArrayList<>();
        tags.add("bio");
        tags.add("review");
        set.setTags(tags);

        ArrayList<Question> questions = new ArrayList<>();
        Question q = new Question(1, "What is DNA?", "Deoxyribonucleic acid");
        questions.add(q);
        set.setQuestionSet(questions);

        assertEquals(42, set.getId());
        assertEquals("Biology Review", set.getName());
        assertEquals("teacher1", set.getCreator());
        assertEquals("Biology", set.getSubject());
        assertEquals(tags, set.getTags());
        assertEquals(questions, set.getQuestions());
    }

    @Test
    void testParameterizedConstructorInitializesLists() {
        StudySet set = new StudySet(10, "Set A", "creatorA");

        assertEquals(10, set.getId());
        assertEquals("Set A", set.getName());
        assertEquals("creatorA", set.getCreator());
        assertNotNull(set.getQuestions());
        assertNotNull(set.getTags());
        assertEquals(0, set.getQuestions().size());
        assertEquals(0, set.getTags().size());
    }

    @Test
    void testGetTagsReturnsEmptyListWhenTagsNull() {
        StudySet set = new StudySet();
        set.setTags(null);

        assertNotNull(set.getTags());
        assertTrue(set.getTags().isEmpty());
    }

    @Test
    void testAddQuestionReturnsFalseForNullQuestion() {
        StudySet set = new StudySet(1, "Test Set", "creator");

        boolean result = set.addQuestion(null);

        assertFalse(result);
        assertEquals(0, set.getQuestions().size());
    }

    @Test
    void testAddQuestionReturnsTrueForValidQuestion() {
        StudySet set = new StudySet(1, "Test Set", "creator");
        Question q = new Question(100, "What is 2+2?", "4");

        boolean result = set.addQuestion(q);

        assertTrue(result);
        assertEquals(1, set.getQuestions().size());
        assertEquals(q, set.getQuestions().get(0));
    }

    @Test
    void testRemoveQuestionByIdReturnsTrueWhenQuestionExists() {
        StudySet set = new StudySet(1, "Test Set", "creator");
        Question q1 = new Question(1, "Q1", "A1");
        Question q2 = new Question(2, "Q2", "A2");

        set.addQuestion(q1);
        set.addQuestion(q2);

        boolean removed = set.removeQuestionById(1);

        assertTrue(removed);
        assertEquals(1, set.getQuestions().size());
        assertEquals(2, set.getQuestions().get(0).getId());
    }

    @Test
    void testRemoveQuestionByIdReturnsFalseWhenQuestionDoesNotExist() {
        StudySet set = new StudySet(1, "Test Set", "creator");
        Question q1 = new Question(1, "Q1", "A1");

        set.addQuestion(q1);

        boolean removed = set.removeQuestionById(999);

        assertFalse(removed);
        assertEquals(1, set.getQuestions().size());
    }
}
