import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import question.Question;
import teacher.StudySet;
import user.User;

public class studySetTest {
    private final Path usersFile = Paths.get("src/main/users.json");
    private final Path setsFile = Paths.get("src/main/sets.json");

    //Resets the json file before each test
    @BeforeEach
    public void clearJson() {
        try (FileWriter writer = new FileWriter("src/main/sets.json")) {
            writer.write("");   // or "[]" if you want an empty array
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //A test that creates a study set
    @Test
    public void testCreateStudySet(){
        User user1 = questionTracker.logIn("admin", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");

        assertNotNull(set1);

        String answer1 = set1.getQuestionSet().get(0).getAnswer();
        String answer2 = set1.getQuestionSet().get(1).getAnswer();

        assertEquals(answer1, "four");
        assertEquals(answer2, "seven");
    }

    //Creates a study set with a list of tags
    @Test
    public void testCreateStudySetWithTags(){
        User user1 = questionTracker.logIn("admin", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Math");
        tags.add("test prep");

        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math", tags);
        
        ArrayList<String> tagList = set1.getTags();
        assertEquals(tagList, tags);    
    }

    //Adds tags to a study set
    @Test
    public void testCreateStudySetAddTags(){
        User user1 = questionTracker.logIn("admin", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Student Aid");
        tags.add("test prep");

        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySet set2 = studySetMaker.addTags(user1.getUsername(), "Math test Prep", tags);
        ArrayList<String> tagList = set2.getTags();

        assertEquals(tagList, tags);    
    }

    //Tests adding a tag to a list that already had tags
    @Test
    public void testCreateStudySetAddTagToAnExistingList(){
        User user1 = questionTracker.logIn("admin", "admin123");
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
        
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math", tags);
        StudySet set2 = studySetMaker.addTags(user1.getUsername(), "Math test Prep", newTag);
        ArrayList<String> tagList = set2.getTags();

        System.out.println(tagList.get(2));

        assertEquals(tagList.get(2), newTag.get(0));    
    }

    //Retrieves a users study set with a specific name
    @Test
    public void testRetreiveStudySet(){
        User user1 = questionTracker.logIn("admin", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySet set2 = studySetMaker.getSet(user1.getUsername(), "Math test Prep");
        
        assertNotNull(set2);

        String answer1 = set2.getQuestionSet().get(0).getAnswer();
        String answer2 = set2.getQuestionSet().get(1).getAnswer();

        assertEquals(answer1, "four");
        assertEquals(answer2, "seven");
    }

    // A test that creates multiple study sets
    @Test
    public void testCreateTwoStudySets(){
        questionTracker.signUp("phantom", "admin123", true);
        User user1 = questionTracker.logIn("phantom", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");
        StudySet set2 = studySetMaker.createSet(list, user1, "Moth test Prep", "Math");
        
        long count = studySetMaker.getSetCount(user1.getUsername());
        System.out.println(count);
        assertEquals(count, 2);
    }

    //Runs a test on the studySetQuiz function where all the answers given are correct.
    @Test
    public void testReviewStudySetPerfectScore(){
        questionTracker.signUp("phantom", "admin123", true);
        User user1 = questionTracker.logIn("phantom", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");

        String answers = "four\n" + "seven\n";
        ByteArrayInputStream testInput = new ByteArrayInputStream(answers.getBytes());
        System.setIn(testInput);

        double testScore = studySetMaker.studySetQuiz("phantom", "Math test Prep");
        assertEquals(1.0, testScore);
    }

    //Runs a test on the studySetQuiz function where all the answers given are incorrect.
    @Test
    public void testReviewStudySetAllIncorect(){
        questionTracker.signUp("phantom", "admin123", true);
        User user1 = questionTracker.logIn("phantom", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");

        String answers = "six\n" + "five\n";
        ByteArrayInputStream testInput = new ByteArrayInputStream(answers.getBytes());
        System.setIn(testInput);

        double testScore = studySetMaker.studySetQuiz("phantom", "Math test Prep");
        assertEquals(0.0, testScore);
    }

    //Runs a test on the studySetQuiz function where only some of the answers given are correct.
    @Test
    public void testReviewStudySetNonPerfectScore(){
        questionTracker.signUp("phantom", "admin123", true);
        User user1 = questionTracker.logIn("phantom", "admin123");
        Question q1 = questionMaker.createQuestion("What's 2 + 2?", "four");
        Question q2 = questionMaker.createQuestion("What's 5 + 2?", "seven");

        ArrayList<Question> list = new ArrayList<>();
        list.add(q1);
        list.add(q2);
        StudySet set1 = studySetMaker.createSet(list, user1, "Math test Prep", "Math");

        String answers = "four\n" + "five\n";
        ByteArrayInputStream testInput = new ByteArrayInputStream(answers.getBytes());
        System.setIn(testInput);

        double testScore = studySetMaker.studySetQuiz("phantom", "Math test Prep");
        assertEquals(0.5, testScore);
    }
}
