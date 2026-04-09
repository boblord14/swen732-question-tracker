import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import user.Question;
import user.QuestionSet;
import user.User;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class QuestionSetTest {

    @Test
    void testCreateQuestionSetByTeacher() {
        User teacher = new User();
        teacher.setId(10);
        teacher.setUsername("teacher10");
        teacher.setIsTeacher(true);

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getQuestionSets).thenReturn(new QuestionSet[0]);

            QuestionSet set = questionTracker.createQuestionSet("Algebra", teacher);
            assertNotNull(set);
            assertEquals("Algebra", set.getName());
            assertEquals(1, set.getId()); // first created id should be 1
            assertEquals(teacher.getId(), set.getCreator().getId());
        }
    }

    @Test
    void testCreateQuestionSetByNonTeacherFails() {
        User user = new User();
        user.setId(20);
        user.setUsername("student20");
        user.setIsTeacher(false);

        QuestionSet set = questionTracker.createQuestionSet("Geometry", user);
        assertNull(set);
    }

    @Test
    void testAddQuestionToSet() {
        User creator = new User();
        creator.setId(5);
        creator.setUsername("creator5");
        creator.setIsTeacher(true);

        QuestionSet set = new QuestionSet(7, "Physics", creator);

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getQuestionSets).thenReturn(new QuestionSet[]{set});

            boolean added = questionTracker.addQuestionToSet(7, "What is velocity?", "distance/time", Arrays.asList("physics", "easy"));
            assertTrue(added);
            assertEquals(1, set.getQuestions().size());
            Question q = set.getQuestions().get(0);
            assertEquals(1, q.getId());
            assertEquals("What is velocity?", q.getText());
            assertEquals("distance/time", q.getAnswer());
            assertEquals(2, q.getTags().size());
        }
    }

    @Test
    void testEditQuestionInSet() {
        User creator = new User();
        creator.setId(9);
        creator.setUsername("creator9");
        creator.setIsTeacher(true);

        QuestionSet set = new QuestionSet(3, "Chemistry", creator);
        Question q = new Question(1, "What is H2O?", "water");
        set.addQuestion(q);

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getQuestionSets).thenReturn(new QuestionSet[]{set});

            boolean edited = questionTracker.editQuestionInSet(3, 1, "What is CO2?", "carbon dioxide", Arrays.asList("chemistry"));
            assertTrue(edited);
            Question updated = set.findQuestionById(1);
            assertNotNull(updated);
            assertEquals("What is CO2?", updated.getText());
            assertEquals("carbon dioxide", updated.getAnswer());
            assertEquals(1, updated.getTags().size());
        }
    }

    @Test
    void testUpdateQuestionSetMetadataPermissions() {
        User creator = new User();
        creator.setId(11);
        creator.setUsername("creator11");
        creator.setIsTeacher(true);

        User other = new User();
        other.setId(12);
        other.setUsername("other12");
        other.setIsTeacher(true);

        QuestionSet set = new QuestionSet(4, "Biology", creator);

        try (MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(questionTracker::getQuestionSets).thenReturn(new QuestionSet[]{set});

            // other user (not creator) should not be allowed
            boolean updatedByOther = questionTracker.updateQuestionSetMetadata(4, other, "New Biology", Arrays.asList("life"));
            assertFalse(updatedByOther);

            // creator can update
            boolean updatedByCreator = questionTracker.updateQuestionSetMetadata(4, creator, "Advanced Biology", Arrays.asList("life","advanced"));
            assertTrue(updatedByCreator);
            assertEquals("Advanced Biology", set.getName());
            assertEquals(2, set.getTags().size());
        }
    }
}
