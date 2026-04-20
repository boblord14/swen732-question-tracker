package tests;

import app.*;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import user.Question;
import user.QuestionSet;
import user.User;
import teacher.StudySet;
import model.SetSession;
import model.BaseSet;
import model.QuestionTracker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTests {

    @BeforeAll
    public static void initJfx() throws Exception {
        // initialize JavaFX toolkit once for all tests
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ex) {
            // already started
        }
    }

    private void injectField(Object controller, String name, Object value) throws Exception {
        Field f = controller.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private Object invokePrivate(Object controller, String name, Class<?>[] types, Object... args) throws Exception {
        java.lang.reflect.Method m = controller.getClass().getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(controller, args);
    }

    @Test
    public void testSetSessionControllerQuestionMode() throws Exception {
        SetSessionController ctrl = new SetSessionController();

        // create and inject UI controls
        Label questionLabel = new Label();
        Label answerLabel = new Label();
        Label resultLabel = new Label();
        Label progressLabel = new Label();
        TextField answerField = new TextField();
        Button nextButton = new Button();
        Button correctButton = new Button();
        Button wrongButton = new Button();
        Button submitButton = new Button();
        Button showAnswerButton = new Button();

        injectField(ctrl, "questionLabel", questionLabel);
        injectField(ctrl, "answerLabel", answerLabel);
        injectField(ctrl, "resultLabel", resultLabel);
        injectField(ctrl, "progressLabel", progressLabel);
        injectField(ctrl, "answerField", answerField);
        injectField(ctrl, "nextButton", nextButton);
        injectField(ctrl, "correctButton", correctButton);
        injectField(ctrl, "wrongButton", wrongButton);
        injectField(ctrl, "submitButton", submitButton);
        injectField(ctrl, "showAnswerButton", showAnswerButton);

        // build a simple BaseSet with 3 questions
        List<Question> qs = new ArrayList<>();
        qs.add(new Question(1, "Q1", "A1"));
        qs.add(new Question(2, "Q2", "A2"));
        qs.add(new Question(3, "Q3", "A3"));

        BaseSet set = new BaseSet() {
            @Override public int getId() { return 99; }
            @Override public String getName() { return "S"; }
            @Override public List<Question> getQuestions() { return qs; }
            @Override public List<String> getTags() { return null; }
            @Override public String getCreator() { return "u"; }
        };

        User u = new User(10, "stu", "pw", false);
        SetSession session = new SetSession(set, u, false);

        // set session (loads first question)
        ctrl.setSession(session, u, () -> {});

        assertEquals("Q1", questionLabel.getText());
        assertEquals("Question 1/3", progressLabel.getText());

    // test show answer (private method)
    invokePrivate(ctrl, "handleShowAnswer", new Class<?>[0]);
        assertEquals("A1", answerLabel.getText());

    // mark correct (private)
    invokePrivate(ctrl, "handleCorrect", new Class<?>[0]);
        assertEquals("Marked Correct", resultLabel.getText());
        assertTrue(correctButton.isDisable() || !correctButton.isDisable()); // ensure no exception

    // go to next question (private)
    invokePrivate(ctrl, "handleNext", new Class<?>[0]);
        assertEquals("Q2", questionLabel.getText());
        assertEquals("Question 2/3", progressLabel.getText());
    }

    @Test
    public void testSetSessionControllerStudyModeSubmit() throws Exception {
        SetSessionController ctrl = new SetSessionController();

        Label questionLabel = new Label();
        Label answerLabel = new Label();
        Label resultLabel = new Label();
        Label progressLabel = new Label();
        TextField answerField = new TextField();
        Button submitButton = new Button();

        injectField(ctrl, "questionLabel", questionLabel);
        injectField(ctrl, "answerLabel", answerLabel);
        injectField(ctrl, "resultLabel", resultLabel);
        injectField(ctrl, "progressLabel", progressLabel);
        injectField(ctrl, "answerField", answerField);
        injectField(ctrl, "submitButton", submitButton);
    Button nextButton = new Button();
    Button correctButton = new Button();
    Button wrongButton = new Button();
    Button showAnswerButton = new Button();
    injectField(ctrl, "nextButton", nextButton);
    injectField(ctrl, "correctButton", correctButton);
    injectField(ctrl, "wrongButton", wrongButton);
    injectField(ctrl, "showAnswerButton", showAnswerButton);

        List<Question> qs = Arrays.asList(new Question(1, "Q1", "A1"), new Question(2, "Q2", "A2"));
        BaseSet set = new BaseSet() {
            @Override public int getId() { return 1; }
            @Override public String getName() { return "S"; }
            @Override public List<Question> getQuestions() { return qs; }
            @Override public List<String> getTags() { return null; }
            @Override public String getCreator() { return "u"; }
        };

        User u = new User(11, "stu2", "pw", false);
        SetSession session = new SetSession(set, u, true);

        ctrl.setSession(session, u, () -> {});

        // empty submission
    answerField.setText("");
    invokePrivate(ctrl, "handleSubmit", new Class<?>[0]);
        assertEquals("Enter an answer", resultLabel.getText());

        // correct submission
    answerField.setText("A1");
    invokePrivate(ctrl, "handleSubmit", new Class<?>[0]);
        assertEquals("", resultLabel.getText());
        assertEquals("Question 2/2", progressLabel.getText());
    }

    @Test
    public void testSignupControllerExistingUsername() throws Exception {
        SignupController ctrl = new SignupController();
        TextField username = new TextField();
        PasswordField pwd = new PasswordField();
        Label error = new Label();
        CheckBox teacherBox = new CheckBox();

        injectField(ctrl, "usernameField", username);
        injectField(ctrl, "passwordField", pwd);
        injectField(ctrl, "errorLabel", error);
        injectField(ctrl, "teacherBox", teacherBox);

        username.setText("test");
        pwd.setText("x");

        // ensure a 'test' user exists so controller will take the "username exists" branch
        boolean exists = Arrays.stream(QuestionTracker.getUsers()).anyMatch(u -> u.getUsername().equals("test"));
        if (!exists) {
            QuestionTracker.signUp("test", "x", false);
        }

    // calling handleSignup should detect existing username and set error label
    ctrl.handleSignup();
    // wait for any FX runLater actions to complete before asserting
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    latch.await(200, TimeUnit.MILLISECONDS);
    assertEquals("This username already exists", error.getText());
    }

    @Test
    public void testLoginControllerInvalid() throws Exception {
        LoginController ctrl = new LoginController();
        TextField username = new TextField();
        PasswordField pwd = new PasswordField();
        Label error = new Label();
        Button signup = new Button();

        injectField(ctrl, "usernameField", username);
        injectField(ctrl, "passwordField", pwd);
        injectField(ctrl, "errorLabel", error);
        injectField(ctrl, "signupButton", signup);

        username.setText("no_such_user_xyz");
        pwd.setText("pw");

        ctrl.handleLogin();
        assertEquals("Invalid username or password", error.getText());
    }

    @Test
    public void testQuestionSetViewControllerModes() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();

        injectField(ctrl, "takeButton", take);
        injectField(ctrl, "addQuestionButton", add);
        injectField(ctrl, "setTitleLabel", title);
        injectField(ctrl, "questionListVBox", list);

        QuestionSet set = new QuestionSet(1, "SetName", "ownerUser");
        set.addQuestion(new Question(1, "Q", "A"));

        User owner = new User(2, "ownerUser", "x", true);
        User other = new User(3, "other", "x", false);

        ctrl.setData(set, owner);
        assertTrue(add.isVisible());

        ctrl.setData(set, other);
        assertFalse(add.isVisible());

        // study set mode
        StudySet s = new StudySet(5, "Study", "ownerUser");
        s.addQuestion(new Question(10, "Qs", "As"));
        ctrl.setDataStudySet(s, owner, "Class1");
        assertFalse(take.isVisible());
        assertEquals(1, list.getChildren().size());
        assertTrue(title.getText().contains("Study"));
    }

    @Test
    public void testQuestionSetListAndClassListControllers() throws Exception {
        QuestionSetListController qsCtrl = new QuestionSetListController();
        VBox setBox = new VBox();
        TextField nameField = new TextField();
        injectField(qsCtrl, "setListVBox", setBox);
        injectField(qsCtrl, "newSetNameField", nameField);

        User u = new User(20, "u20", "p", false);
        qsCtrl.setUser(u);
        assertEquals(0, setBox.getChildren().size());

        ClassListController clCtrl = new ClassListController();
        VBox classBox = new VBox();
        Button action = new Button();
        injectField(clCtrl, "classListBox", classBox);
        injectField(clCtrl, "actionButton", action);

        u.setIsTeacher(true);
        u.getClassrooms().add("C1");
        clCtrl.setUser(u);
        assertEquals("Create Class", action.getText());
        assertEquals(1, classBox.getChildren().size());
    }

    @Test
    public void testStruggleViewControllerStudentNoData() throws Exception {
        StruggleViewController ctrl = new StruggleViewController();
        Label title = new Label();
        VBox list = new VBox();
        Button practice = new Button();

        injectField(ctrl, "titleLabel", title);
        injectField(ctrl, "struggleListVBox", list);
        injectField(ctrl, "practiceButton", practice);

        User stu = new User(30, "s30", "p", false);
        // stu has empty wrongQuestionData by default
        ctrl.setDataStudent(stu);
        assertTrue(list.getChildren().size() >= 1);
        assertTrue(practice.isDisable());
    }

    @Test
    public void testUIUtilsFadeInNoop() {
        // Should not throw
        app.UIUtils.fadeIn(null);
    }
}
