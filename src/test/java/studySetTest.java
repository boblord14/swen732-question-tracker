import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import user.Question;
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
        String username = "admin";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
        String username = "admin";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
        String username = "admin";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
        String username = "admin";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
        String username = "admin";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
        String username = "phantom";
        String password = "admin123";
        questionTracker.signUp(username, password, true);
        User user1 = questionTracker.logIn(username, password);
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
}
