package tests;

import org.junit.jupiter.api.Test;
import user.User;
import user.QuestionSet;
import user.Question;
import teacher.StudySet;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndUserTests {

    @Test
    public void userAddRemoveQuestionSetIdAndWrongQuestions() {
        User u = new User(10, "bob", "pw", false);
        assertTrue(u.getQuestionSetIds().isEmpty());

        u.addQuestionSetId(5);
        u.addQuestionSetId(5); // duplicate should be ignored
        assertEquals(1, u.getQuestionSetIds().size());
        assertTrue(u.getQuestionSetIds().contains(5));

        u.removeQuestionSetId(5);
        assertFalse(u.getQuestionSetIds().contains(5));

        // wrong question tracking
        u.addWrongQuestion(Arrays.asList("algebra", "equation"));
        u.addWrongQuestion(Arrays.asList("geometry"));
        assertEquals(2, u.getWrongQuestionData().size());
    }

    @Test
    public void questionSetAddRemoveFindAndTags() {
        QuestionSet s = new QuestionSet(1, "Set A", "alice");
        Question q1 = new Question();
        q1.setId(101);
        q1.setText("What is 2+2?");
        q1.setAnswer("4");
        q1.setTags(Arrays.asList("math","easy"));

        assertTrue(s.addQuestion(q1));
        assertEquals(1, s.getQuestions().size());
        assertNotNull(s.findQuestionById(101));

        assertTrue(s.removeQuestionById(101));
        assertNull(s.findQuestionById(101));

        // tags
        s.addTag("algebra");
        s.addTag("algebra"); // duplicate ignored
        assertEquals(1, s.getTags().size());
        s.removeTag("algebra");
        assertFalse(s.getTags().contains("algebra"));
    }

    @Test
    public void studySetAddRemoveQuestion() {
        StudySet ss = new StudySet(7, "Study 1", "teacher1");
        Question q = new Question(); q.setId(55);
        assertTrue(ss.addQuestion(q));
        assertEquals(1, ss.getQuestions().size());
        assertTrue(ss.removeQuestionById(55));
        assertEquals(0, ss.getQuestions().size());
    }
}
