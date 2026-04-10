import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import question.Question;
import teacher.StudySet;
import user.User;

public class studySetTest {
    private final Path usersFile = Paths.get("src/main/users.json");
    private final Path setsFile = Paths.get("src/main/sets.json");

    //A test that creates a study set
    @Test
    public void testCreateStudySet() {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher1");
        user1.setPassword("password1");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher1", "password1");
        }

        assertNotNull(user2);

        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        StudySet set1 = studySetMaker.createSet(list, user2, "Math test Prep", "Math");
        
        assertNotNull(set1);
        assertEquals("four", set1.getQuestionSet().get(0).getAnswer());
        assertEquals("seven", set1.getQuestionSet().get(1).getAnswer());
        
    }


    //Creates a study set with a list of tags
    @Test
    public void testCreateStudySetWithTags(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher2");
        user1.setPassword("password2");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher2", "password2");
        }

        assertNotNull(user2);

        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tagList = new ArrayList<>();
        tagList.add("Student Help");

        StudySet set1 = studySetMaker.createSet(list, user2, "Math test Prep", "Math", tagList);
        
        assertNotNull(set1);
        assertEquals("Student Help", set1.getTags().get(0));
        //assertEquals("seven", set1.getQuestionSet().get(1).getAnswer());
    }

    //Adds tags to a study set
    @Test
    public void testCreateStudySetAddTags(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher3");
        user1.setPassword("password3");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            user2 = questionTracker.logIn("teacher3", "password3");
        }

        assertNotNull(user2);
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Student Aid");
        tags.add("test prep");

        studySetMaker.createSet(list, user2, "Math test Prep", "Math");
        StudySet set2 = studySetMaker.addTags(user2.getUsername(), "Math test Prep", tags);
        ArrayList<String> tagList = set2.getTags();

        assertEquals(tagList, tags);
    }

    //Tests adding a tag to a list that already had tags
    @Test
    public void testCreateStudySetAddTagToAnExistingList(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher4");
        user1.setPassword("password4");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher4", "password4");
        }

        assertNotNull(user2);
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Student Aid");
        tags.add("test prep");

        ArrayList<String> newTag = new ArrayList<>();
        newTag.add("Math");
        
        StudySet mockSet = new StudySet();
        mockSet.setCreator(user2.getUsername());
        mockSet.setTitle("Math test Prep");
        mockSet.setSubject("Math");
        mockSet.setQuestionSet(list);
        mockSet.setTags(tags);

        try (MockedStatic<studySetMaker> mocked = mockStatic(studySetMaker.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> studySetMaker.getSet("teacher4", "Math test Prep"))
                .thenReturn(mockSet);

            StudySet updated = studySetMaker.addTags("teacher4", "Math test Prep", newTag);

            ArrayList<String> tagList = updated.getTags();

            assertEquals("Math", tagList.get(2));
        }   
    }

    //Retrieves a users study set with a specific name
    @Test
    public void testRetreiveStudySet(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher5");
        user1.setPassword("password5");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher5", "password5");
        }

        assertNotNull(user2);
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        
        studySetMaker.createSet(list, user2, "Math test Prep", "Math");
        StudySet set2 = studySetMaker.getSet(user2.getUsername(), "Math test Prep");
        
        assertNotNull(set2);

        
        String answer1 = set2.getQuestionSet().get(0).getAnswer();
        String answer2 = set2.getQuestionSet().get(1).getAnswer();

        assertEquals(answer1, "four");
        assertEquals(answer2, "seven");
    }

    // A test that creates multiple study sets
    @Test
    public void testCreateTwoStudySets(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher5");
        user1.setPassword("password5");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };

        User user2;
        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher5", "password5");
        }

        assertNotNull(user2);
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        
        studySetMaker.createSet(list, user2, "Math test Prep", "Math");
        studySetMaker.createSet(list, user2, "Moth test Prep", "Math");
        
        //studySetMaker.getSet(user2.getUsername(), "Math test Prep");
        //studySetMaker.getSet(user2.getUsername(), "Moth test Prep");
        
        long count = studySetMaker.getSetCount(user2.getUsername());
        System.out.println(count);
        assertEquals(count, 2);
    }

    //Runs a test on the studySetQuiz function where all the answers given are correct.
    @Test
    public void testReviewStudySetPerfectScore(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher7");
        user1.setPassword("password7");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };
        User user2;

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher7", "password7");
        }

        assertNotNull(user2);

        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        StudySet mockSet = new StudySet();
        mockSet.setTitle("Math test Prep");
        mockSet.setSubject("Math");
        mockSet.setQuestionSet(list);

        String answers = "four\nseven\n";
        System.setIn(new ByteArrayInputStream(answers.getBytes()));

        try (MockedStatic<studySetMaker> mocked = mockStatic(studySetMaker.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> studySetMaker.getSet("teacher7", "Math test Prep"))
                .thenReturn(mockSet);

            double testScore = studySetMaker.studySetQuiz("teacher7", "Math test Prep");

            assertEquals(1.0, testScore);
        }
    }

    //Runs a test on the studySetQuiz function where all the answers given are incorrect.
    @Test
    public void testReviewStudySetAllIncorect(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher8");
        user1.setPassword("password8");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };
        User user2;

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher8", "password8");
        }

        assertNotNull(user2);

        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        StudySet mockSet = new StudySet();
        mockSet.setTitle("Math test Prep");
        mockSet.setSubject("Math");
        mockSet.setQuestionSet(list);

        String answers = "five\nfour\n";
        System.setIn(new ByteArrayInputStream(answers.getBytes()));

        try (MockedStatic<studySetMaker> mocked = mockStatic(studySetMaker.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> studySetMaker.getSet("teacher8", "Math test Prep"))
                .thenReturn(mockSet);

            double testScore = studySetMaker.studySetQuiz("teacher8", "Math test Prep");

            assertEquals(0.0, testScore);
        }
    }

    //Runs a test on the studySetQuiz function where only some of the answers given are correct.
    @Test
    public void testReviewStudySetNonPerfectScore(){
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher9");
        user1.setPassword("password9");
        user1.setIsTeacher(true);

        User[] mockUsers = { user1 };
        User user2;

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);
            user2 = questionTracker.logIn("teacher9", "password9");
        }

        assertNotNull(user2);

        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        StudySet mockSet = new StudySet();
        mockSet.setTitle("Math test Prep");
        mockSet.setSubject("Math");
        mockSet.setQuestionSet(list);

        String answers = "three\nseven\n";
        System.setIn(new ByteArrayInputStream(answers.getBytes()));

        try (MockedStatic<studySetMaker> mocked = mockStatic(studySetMaker.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> studySetMaker.getSet("teacher9", "Math test Prep"))
                .thenReturn(mockSet);

            double testScore = studySetMaker.studySetQuiz("teacher9", "Math test Prep");

            assertEquals(0.5, testScore);
        }
    }
}
