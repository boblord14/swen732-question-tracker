package tests;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import app.ClassListController;
import app.QuestionSetListController;
import app.QuestionSetViewController;
import user.User;
import user.QuestionSet;
import user.Question;
import teacher.StudySet;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerUnitTests {

    @BeforeAll
    public static void initJfx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                latch.countDown();
            });
            latch.await();
        } catch (IllegalStateException ex) {
            // Toolkit already initialized; that's fine for tests
        }
    }

    @AfterAll
    public static void teardown() {
        // no-op; JavaFX platform cannot be stopped easily in tests
    }

    @Test
    public void classListControllerSetsActionButtonTextBasedOnRole() throws Exception {
        ClassListController ctrl = new ClassListController();

        // inject fake FXML fields
        setPrivateField(ctrl, "classListBox", new VBox());
        Button actionBtn = new Button();
        setPrivateField(ctrl, "actionButton", actionBtn);

        User teacher = new User(1, "t1", "pw", true);
        ctrl.setUser(teacher);
        assertEquals("Create Class", actionBtn.getText());

        User student = new User(2, "s1", "pw", false);
        ctrl.setUser(student);
        assertEquals("Join Class", actionBtn.getText());
    }

    @Test
    public void questionSetListControllerLoadsEmptyListGracefully() throws Exception {
        QuestionSetListController ctrl = new QuestionSetListController();
        VBox vbox = new VBox();
        TextField tf = new TextField();
        Button btn = new Button();
        setPrivateField(ctrl, "setListVBox", vbox);
        setPrivateField(ctrl, "newSetNameField", tf);
        setPrivateField(ctrl, "createSetBtn", btn);

        User u = new User(99, "u99", "pw", false);
        // should not throw
        ctrl.setUser(u);
        assertNotNull(vbox.getChildren());
    }

    @Test
    public void questionSetViewControllerDisplaysQuestionsInList() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();
        setPrivateField(ctrl, "takeButton", take);
        setPrivateField(ctrl, "addQuestionButton", add);
        setPrivateField(ctrl, "setTitleLabel", title);
        setPrivateField(ctrl, "questionListVBox", list);

        // create a question set
        QuestionSet s = new QuestionSet(3, "My Set", "owner");
        Question q1 = new Question(); q1.setId(201); q1.setText("Q1"); q1.setAnswer("A1");
        Question q2 = new Question(); q2.setId(202); q2.setText("Q2"); q2.setAnswer("A2");
        s.addQuestion(q1); s.addQuestion(q2);

        User owner = new User(10, "owner", "pw", false);
        ctrl.setData(s, owner);

        assertEquals("My Set", title.getText());
        // two children representing questions
        assertEquals(2, list.getChildren().size());
        // since owner equals creator, add button should be visible
        assertTrue(add.isVisible());
    }

    @Test
    public void questionSetViewControllerStudySetMode() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();
        setPrivateField(ctrl, "takeButton", take);
        setPrivateField(ctrl, "addQuestionButton", add);
        setPrivateField(ctrl, "setTitleLabel", title);
        setPrivateField(ctrl, "questionListVBox", list);

        StudySet ss = new StudySet(5, "Study", "teach1");
        Question q = new Question(); q.setId(301); q.setText("S1"); q.setAnswer("Ans");
        ss.setQuestionSet(Arrays.asList(q));

        User teacher = new User(20, "teach1", "pw", true);
        ctrl.setDataStudySet(ss, teacher, "ClassA");

        assertEquals("Study", title.getText());
        assertEquals(1, list.getChildren().size());
        assertFalse(take.isVisible());
    }

    // helper to set private fields
    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
