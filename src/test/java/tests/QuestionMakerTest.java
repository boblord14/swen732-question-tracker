package tests;

import model.QuestionMaker;
import org.junit.jupiter.api.Test;
import user.Question;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionMakerTest {

    @Test
    public void testAddTagAddsUniqueTags() {
        Question q = new Question(5, "Q", "A");
        q.setTags(new ArrayList<>(Arrays.asList("java")));

        QuestionMaker maker = new QuestionMaker();
        maker.addTag(q, Arrays.asList("oop", "java"));

        assertTrue(q.getTags().contains("java"));
        assertTrue(q.getTags().contains("oop"));
        // ensure no duplicates
        long countJava = q.getTags().stream().filter(t -> "java".equals(t)).count();
        assertEquals(1, countJava);
    }
}
