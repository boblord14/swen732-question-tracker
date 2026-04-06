import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import question.Question;

/**
 * Tests for the various getters and setters of the Question class
 * Tests each individual attribute separately and everything together
 */
class QuestionTest {

    /**
     * Tests if getting/setting id works correctly
     */
    @Test
    void testSetAndGetId() {
        Question q = new Question();
        q.setId(101);
        assertEquals(101, q.getId());
    }

    /**
     * Tests if getting/setting subject works correctly
     */
    @Test
    void testSetAndGetSubject() {
        Question q = new Question();
        q.setSubject("Math");
        assertEquals("Math", q.getSubject());
    }

    /**
     * Tests if getting/setting tags works correctly
     */
    @Test
    void testSetAndGetTags() {
        Question q = new Question();
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("algebra", "equations"));
        q.setTag(tags);

        assertEquals(tags, q.getTags());
        assertEquals(2, q.getTags().size());
        assertTrue(q.getTags().contains("algebra"));
        assertTrue(q.getTags().contains("equations"));
    }

    /**
     * Tests if getting/setting question works correctly
     */
    @Test
    void testSetAndGetQuestion() {
        Question q = new Question();
        q.setQuestion("What is 2 + 2?");
        assertEquals("What is 2 + 2?", q.getQuestion());
    }

    /**
     * Tests if getting/setting answer works correctly
     */
    @Test
    void testSetAndGetAnswer() {
        Question q = new Question();
        q.setAnswer("4");
        assertEquals("4", q.getAnswer());
    }

    /**
     * Tests setting and then getting each attribute of the Question
     */
    @Test
    void testAllFieldsTogether() {
        Question q = new Question();

        ArrayList<String> tags = new ArrayList<>(Arrays.asList("Software Architecture", "Software Design"));

        q.setId(73);
        q.setSubject("Software Engineering");
        q.setTag(tags);
        q.setQuestion("What does MVC stand for?");
        q.setAnswer("Model-View-Controller");

        assertEquals(73, q.getId());
        assertEquals("Software Engineering", q.getSubject());
        assertEquals(tags, q.getTags());
        assertEquals("What does MVC stand for?", q.getQuestion());
        assertEquals("Model-View-Controller", q.getAnswer());
    }

    @Test
    void testDefaultValues() {
        Question q = new Question();

        assertEquals(0, q.getId());
        assertNull(q.getSubject());
        assertNull(q.getTags());
        assertNull(q.getQuestion());
        assertNull(q.getAnswer());
    }
}