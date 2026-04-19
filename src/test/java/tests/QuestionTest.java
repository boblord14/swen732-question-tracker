package tests;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import user.Question;

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
     * Tests if getting/setting tags works correctly
     */
    @Test
    void testSetAndGetTags() {
        Question q = new Question();
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("algebra", "equations"));
        q.setTags(tags);

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
        q.setText("What is 2 + 2?");
        assertEquals("What is 2 + 2?", q.getText());
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
        q.setTags(tags);
        q.setText("What does MVC stand for?");
        q.setAnswer("Model-View-Controller");

        assertEquals(73, q.getId());
        assertEquals(tags, q.getTags());
        assertEquals("What does MVC stand for?", q.getText());
        assertEquals("Model-View-Controller", q.getAnswer());
    }

    @Test
    void testDefaultValues() {
        Question q = new Question();

        assertEquals(0, q.getId());
        assertNull(q.getTags());
        assertNull(q.getText());
        assertNull(q.getAnswer());
    }
    
    @Test
    void testConstructorIdText() {
        Question q = new Question(5, "What is Java?");

        assertEquals(5, q.getId());
        assertEquals("What is Java?", q.getText());
        assertEquals("", q.getAnswer());
        assertNotNull(q.getTags());
        assertTrue(q.getTags().isEmpty());
    }

    @Test
    void testConstructorIdTextAnswer() {
        Question q = new Question(10, "2+2?", "4");

        assertEquals(10, q.getId());
        assertEquals("2+2?", q.getText());
        assertEquals("4", q.getAnswer());
        assertNotNull(q.getTags());
        assertTrue(q.getTags().isEmpty());
    }

    @Test
    void testAddTag() {
        Question q = new Question(10, "2+2?", "4");

        q.addTag("math");
        q.addTag("algebra");

        assertEquals(2, q.getTags().size());
        assertTrue(q.getTags().contains("math"));
        assertTrue(q.getTags().contains("algebra"));
    }

    @Test
    void testRemoveTag() {
        Question q = new Question(10, "2+2?", "4");

        q.addTag("math");
        q.addTag("algebra");

        q.removeTag("math");

        assertEquals(1, q.getTags().size());
        assertFalse(q.getTags().contains("math"));
        assertTrue(q.getTags().contains("algebra"));
    }

    @Test
    void testRemoveNonexistentTag() {
        Question q = new Question(10, "2+2?", "4");

        q.addTag("math");
        q.removeTag("science");

        assertEquals(1, q.getTags().size());
    }

    @Test
    void testToString() {
        Question q = new Question(1, "Q?", "A");
        q.addTag("tag1");

        String result = q.toString();

        assertTrue(result.contains("1"));
        assertTrue(result.contains("Q?"));
        assertTrue(result.contains("A"));
        assertTrue(result.contains("tag1"));
    }

    @Test
    void testTagIndependence() {
        Question q = new Question(1, "Q?", "A");

        ArrayList<String> tags = new ArrayList<>();
        tags.add("math");

        q.setTags(tags);
        tags.add("physics");

        assertEquals(2, q.getTags().size());
    }
}