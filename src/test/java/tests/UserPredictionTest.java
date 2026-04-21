package tests;

import org.junit.jupiter.api.BeforeEach;

import user.Question;
import user.User;
import user.UserPrediction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPredictionTest {
    private final Path classFile = Paths.get("src/main/classes.json");
    private final Path usersFile = Paths.get("src/main/users.json");

    @BeforeEach
        //delete users and class file before each test so we're testing on a fresh DB
    void setUp() throws IOException {
        Files.deleteIfExists(usersFile);
        Files.deleteIfExists(classFile);
    }

    //Generate a user struggle vector and ensure its correctly built
    @org.junit.jupiter.api.Test
    void testStruggleVectorResult() {
        Question q1 = new Question(1, "question 1");
        q1.addTag("tag 1");
        q1.addTag("tag 2");

        Question q2 = new Question(2, "question 2");
        q2.addTag("tag 2");
        q2.addTag("tag 3");

        User user = new User(1, "test", "test", false);
        user.addWrongQuestion(q1.getTags());
        user.addWrongQuestion(q2.getTags());

        UserPrediction up = new UserPrediction(user);
        Map<String, Double> outputVector = up.generateUserStruggleVector();

        assertEquals(3, outputVector.size());

        //incredibly dirty rounding trick to fix some floating point shenanigans
        assertEquals(0.33, Math.round(outputVector.get("tag 1")* 100.0) / 100.0);
        assertEquals(0.33, Math.round(outputVector.get("tag 2")* 100.0) / 100.0);
        assertEquals(0.33, Math.round(outputVector.get("tag 3")* 100.0) / 100.0);

    }

    //Generate a user struggle vector for a group of users and ensure its correctly built
    @org.junit.jupiter.api.Test
    void testGroupStruggleVectorResult() {
        Question q1 = new Question(1, "question 1");
        q1.addTag("tag 1");
        q1.addTag("tag 4");

        Question q2 = new Question(2, "question 2");
        q2.addTag("tag 3");
        q2.addTag("tag 4");

        Question q3 = new Question(3, "question 3");
        q3.addTag("tag 3");
        q3.addTag("tag 5");

        Question q4 = new Question(4, "question 4");
        q4.addTag("tag 3");
        q4.addTag("tag 6");

        User user1 = new User(1, "test", "test", false);
        user1.addWrongQuestion(q1.getTags());

        User user2 = new User(2, "test2", "test2", false);
        user2.addWrongQuestion(q2.getTags());
        user2.addWrongQuestion(q3.getTags());
        user2.addWrongQuestion(q4.getTags());

        List<User> users = new java.util.ArrayList<>();
        users.add(user1);
        users.add(user2);
        Map<String, Double> outputVector = UserPrediction.generateUserStruggleVector(users);

        assertEquals(5, outputVector.size()); // trust me there's 5 tags i just named the data poorly

        //now we get into the realm of awful math so i cant just check it with the raw numbers, instead let's just
        // verify that the things that are supposed to be bigger, are actually bigger
        assertTrue(outputVector.get("tag 4") > outputVector.get("tag 1"));
        assertTrue(outputVector.get("tag 1") > outputVector.get("tag 3"));
        assertTrue(outputVector.get("tag 3") > outputVector.get("tag 5"));
        assertEquals(Math.round(outputVector.get("tag 6")), Math.round(outputVector.get("tag 5")));


        //but with that said we should have normalized, so our the sum of all the tag scores should be 1.0
        double sum = outputVector.values().stream().mapToDouble(d -> d).sum();
        assertEquals(1.0, Math.round(sum * 100.0) / 100.0);

    }

    //Take a dummy question and see how well it matches up to the struggle vector
    @org.junit.jupiter.api.Test
    void scoreQuestionOnVector() {
        Question q1 = new Question(1, "question 1");
        q1.addTag("tag 1");
        q1.addTag("tag 2");

        Question q2 = new Question(2, "question 2");
        q2.addTag("tag 2");
        q2.addTag("tag 3");

        User user = new User(3, "test3", "test3", false);
        user.addWrongQuestion(q1.getTags());
        user.addWrongQuestion(q2.getTags());

        UserPrediction up = new UserPrediction(user);
        Map<String, Double> outputVector = up.generateUserStruggleVector();
        //tag 1- 0.25, tag 2- 0.50, tag 3- 0.25
        //reminder question scoring is (sum of matched tag scores)/(# tags)

        //match of 2 found tags, one med one strong, result med-strong
        Question testQ1 = new Question(3, "test question 1");
        testQ1.addTag("tag 1"); //0.25
        testQ1.addTag("tag 2"); //0.5
        //(0.25 + 0.5) / 2 = 0.375

        //match of a single tag, strong, result strong
        Question testQ2 = new Question(4, "test question 2");
        testQ2.addTag("tag 2");//0.5
        //0.5 / 1 = 0.5

        //match of neither tag, nothing there
        Question testQ3 = new Question(5, "test question 3");
        testQ3.addTag("tag 4");//0.0
        testQ3.addTag("tag 5");//0.0
        //0 / 1 = 0

        //match of one tag but one no match, one strong one n/a, result medium
        Question testQ4 = new Question(6, "test question 4");
        testQ4.addTag("tag 2");//0.5
        testQ4.addTag("tag 4");//0.0
        //0.5 / 2 = 0.25

        assertEquals(0.3333333333333333, UserPrediction.scoreQuestion(testQ1, outputVector));
        assertEquals(0.3333333333333333, UserPrediction.scoreQuestion(testQ2, outputVector));
        assertEquals(0.0, UserPrediction.scoreQuestion(testQ3, outputVector));
        assertEquals(0.16666666666666666, UserPrediction.scoreQuestion(testQ4, outputVector));
    }

}
