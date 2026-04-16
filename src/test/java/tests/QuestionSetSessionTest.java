package tests;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import model.SetSession;
import model.QuestionTracker;
import user.Question;
import user.QuestionSet;
import user.User;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.any;

class QuestionSetSessionTest {

    @Test
    void testSessionFlow_markWrongLogsTagsAndPersists() {
        // prepare a student and a question set with one question
        User student = new User();
        student.setId(500);
        student.setUsername("student500");
        student.setIsTeacher(false);

        Question q1 = new Question(1, "What is 2+2?", "4");
        q1.setTags(Arrays.asList("math", "arithmetic"));

        QuestionSet set = new QuestionSet(1, "Simple Math", null);
        set.addQuestion(q1);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            // mock getQuestionSets to return our set
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new QuestionSet[]{set});
            // stub saveUser for this specific student to no-op so test does not write files
            mocked.when(() -> QuestionTracker.saveUser(any(User.class))).thenAnswer(invocation -> null);

            SetSession session = QuestionTracker.createQuestionSetSession(1, student);
            assertNotNull(session);
            assertTrue(session.hasNext());

            Question asked = session.nextQuestion();
            assertEquals("What is 2+2?", asked.getText());
            assertFalse(session.submitAnswer("3"));

            // verify that the student's wrong-question list now contains the tags
            assertEquals(1, student.getWrongQuestionData().size());
            assertEquals(Arrays.asList("math", "arithmetic"), student.getWrongQuestionData().get(0));

            // verify saveUser called for this student
            mocked.verify(() -> QuestionTracker.saveUser(any(User.class)));
        }
    }

    @Test
    void testSessionIterateRevealAndMarkCorrect() {
        User student = new User();
        student.setId(501);
        student.setUsername("student501");
        student.setIsTeacher(false);

        Question q1 = new Question(1, "Capital of France?", "Paris");
        q1.setTags(Arrays.asList("geography"));

        Question q2 = new Question(2, "5*6?", "30");
        q2.setTags(Arrays.asList("math"));

        QuestionSet set = new QuestionSet(2, "Mixed", null);
        set.addQuestion(q1);
        set.addQuestion(q2);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new QuestionSet[]{set});
            // do not expect saveUser to be called when marking correct
            mocked.when(() -> QuestionTracker.saveUser(any(User.class))).thenAnswer(invocation -> null);

            SetSession session = QuestionTracker.createQuestionSetSession(2, student);
            assertNotNull(session);

            Question a1 = session.nextQuestion();
            assertEquals("Capital of France?", a1.getText());
            assertTrue(session.submitAnswer("Paris"));

            Question a2 = session.nextQuestion();
            assertEquals("5*6?", a2.getText());
            assertTrue(session.submitAnswer("30"));

            assertEquals(2, session.getTotalQuestions());
            assertEquals(2, session.getCurrentIndex());
            assertEquals(2, session.getResults().size());
            assertTrue(session.getResults().get(1));
            assertTrue(session.getResults().get(2));

            // ensure no wrong questions were recorded
            assertEquals(0, student.getWrongQuestionData().size());
        }
    }

    @Test
    void testUserWrongQuestionCap() {
        User u = new User();
        u.setId(600);
        u.setUsername("u600");
        u.setIsTeacher(false);

        // add 105 wrong entries directly to the user and verify cap at 100
        for (int i = 0; i < 105; i++) {
            u.addWrongQuestion(Arrays.asList("tag" + i));
        }

        assertEquals(100, u.getWrongQuestionData().size());
        // most recent should be the last 100 entries (i = 5..104)
        assertEquals(Arrays.asList("tag104"), u.getWrongQuestionData().get(99));
    }
}
