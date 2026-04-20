package tests;

import app.ClassListController;
import app.ClassViewController;
import app.QuestionSetViewController;
import app.QuestionSetListController;
import app.StruggleViewController;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.SearchService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import teacher.StudySet;
import user.Question;
import user.QuestionSet;
import user.User;
import user.Classroom;
import model.QuestionTracker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AdditionalControllerTests {

    @BeforeAll
    public static void initJfx() throws Exception {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ex) {
            // already started
        }
    }

    private static void injectField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static Object getField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static Method getMethod(Object obj, String name, Class<?>... params) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    // ─── ClassListController ───────────────────────────────────────────

    @Test
    public void testClassListControllerSetUser_TeacherAndStudent() throws Exception {
        ClassListController ctrl = new ClassListController();
        VBox classBox = new VBox();
        Button action = new Button();
        injectField(ctrl, "classListBox", classBox);
        injectField(ctrl, "actionButton", action);

        User teacher = new User(1, "t", "x", true);
        teacher.getClassrooms().addAll(Arrays.asList("C1", "C2"));
        ctrl.setUser(teacher);
        assertEquals(2, classBox.getChildren().size());
        assertEquals("Create Class", action.getText());

        User student = new User(2, "s", "x", false);
        student.getClassrooms().addAll(Arrays.asList("C3"));
        ctrl.setUser(student);
        assertEquals("Join Class", action.getText());
        assertEquals(1, classBox.getChildren().size());
    }

    @Test
    public void testClassListControllerSetUserNoClassrooms() throws Exception {
        ClassListController ctrl = new ClassListController();
        VBox classBox = new VBox();
        Button action = new Button();
        injectField(ctrl, "classListBox", classBox);
        injectField(ctrl, "actionButton", action);

        User student = new User(10, "empty", "x", false);
        ctrl.setUser(student);
        assertEquals(0, classBox.getChildren().size());
        assertEquals("Join Class", action.getText());
    }

    // ─── QuestionSetViewController ─────────────────────────────────────

    @Test
    public void testQuestionSetViewControllerSetDataVisibility() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();

        injectField(ctrl, "takeButton", take);
        injectField(ctrl, "addQuestionButton", add);
        injectField(ctrl, "setTitleLabel", title);
        injectField(ctrl, "questionListVBox", list);

        QuestionSet s = new QuestionSet(99, "SetName", "ownerUser");
        s.addQuestion(new Question(1, "Q", "A"));

        User owner = new User(2, "ownerUser", "x", true);
        User other = new User(3, "other", "x", false);

        ctrl.setData(s, owner);
        assertTrue(add.isVisible());
        assertTrue(take.isVisible());
        assertEquals("SetName", title.getText());
        assertFalse(list.getChildren().isEmpty());

        ctrl.setData(s, other);
        assertFalse(add.isVisible());
    }

    @Test
    public void testQuestionSetViewControllerSetDataStudySet() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();

        injectField(ctrl, "takeButton", take);
        injectField(ctrl, "addQuestionButton", add);
        injectField(ctrl, "setTitleLabel", title);
        injectField(ctrl, "questionListVBox", list);

        StudySet ss = new StudySet();
        ss.setId(50);
        ss.setName("Study1");
        ss.setCreator("teacherX");
        ss.setSubject("Math");
        ss.setQuestionSet(Arrays.asList(new Question(1, "2+2", "4")));

        User owner = new User(10, "teacherX", "x", true);

        ctrl.setDataStudySet(ss, owner, "ClassA");

        assertFalse(take.isVisible()); // take hidden in study set mode
        assertTrue(add.isVisible());   // owner sees add
        assertTrue(title.getText().contains("Study1"));
        assertTrue(title.getText().contains("Math"));
        assertEquals(1, list.getChildren().size());
    }

    @Test
    public void testQuestionSetViewControllerSetDataStudySetNullQuestions() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();

        injectField(ctrl, "takeButton", take);
        injectField(ctrl, "addQuestionButton", add);
        injectField(ctrl, "setTitleLabel", title);
        injectField(ctrl, "questionListVBox", list);

        StudySet ss = new StudySet();
        ss.setId(51);
        ss.setName("Empty");
        ss.setCreator("t");
        ss.setQuestionSet(null);

        User owner = new User(10, "t", "x", true);
        ctrl.setDataStudySet(ss, owner, "ClassB");
        assertEquals(0, list.getChildren().size());
    }

    @Test
    public void testQuestionSetViewControllerRefreshViewShowsTags() throws Exception {
        QuestionSetViewController ctrl = new QuestionSetViewController();
        Button take = new Button();
        Button add = new Button();
        Label title = new Label();
        VBox list = new VBox();

        injectField(ctrl, "takeButton", take);
        injectField(ctrl, "addQuestionButton", add);
        injectField(ctrl, "setTitleLabel", title);
        injectField(ctrl, "questionListVBox", list);

        Question q = new Question(1, "What?", "This");
        q.setTags(Arrays.asList("tag1", "tag2"));
        QuestionSet qs = new QuestionSet(77, "Tagged", "me");
        qs.setQuestions(Arrays.asList(q));

        User me = new User(1, "me", "x", false);
        ctrl.setData(qs, me);

        // The list should contain one row (HBox) with a label that includes tags
        HBox row = (HBox) list.getChildren().get(0);
        Label lbl = (Label) row.getChildren().get(0);
        assertTrue(lbl.getText().contains("tag1"));
        assertTrue(lbl.getText().contains("tag2"));
    }

    // ─── QuestionSetListController ─────────────────────────────────────

    @Test
    public void testQuestionSetListHandleCreateSetBlankNoop() throws Exception {
        QuestionSetListController ctrl = new QuestionSetListController();
        VBox setBox = new VBox();
        TextField nameField = new TextField();
        injectField(ctrl, "setListVBox", setBox);
        injectField(ctrl, "newSetNameField", nameField);

        nameField.setText("");
        Method m = getMethod(ctrl, "handleCreateSet");
        m.invoke(ctrl);
        assertEquals(0, setBox.getChildren().size());
    }

    @Test
    public void testQuestionSetListHandleCreateSetNullNoop() throws Exception {
        QuestionSetListController ctrl = new QuestionSetListController();
        VBox setBox = new VBox();
        TextField nameField = new TextField();
        injectField(ctrl, "setListVBox", setBox);
        injectField(ctrl, "newSetNameField", nameField);

        nameField.setText(null);
        Method m = getMethod(ctrl, "handleCreateSet");
        // null text should return without creating
        m.invoke(ctrl);
        assertEquals(0, setBox.getChildren().size());
    }

    // ─── ClassViewController ───────────────────────────────────────────

    @Test
    public void testClassViewControllerLoadClassDataNullClassroom() throws Exception {
        ClassViewController ctrl = new ClassViewController();
        Label nameLabel = new Label();
        Label codeLabel = new Label();
        VBox studentList = new VBox();
        VBox teacherSection = new VBox();
        VBox studySetList = new VBox();

        injectField(ctrl, "classNameLabel", nameLabel);
        injectField(ctrl, "classCodeLabel", codeLabel);
        injectField(ctrl, "studentListVBox", studentList);
        injectField(ctrl, "teacherSection", teacherSection);
        injectField(ctrl, "studySetListVBox", studySetList);

        // classroom is null by default — loadClassData should short-circuit
        Method m = getMethod(ctrl, "loadClassData");
        m.invoke(ctrl);

        assertEquals("", nameLabel.getText());
    }

    @Test
    public void testClassViewControllerLoadStudentsPopulatesLabels() throws Exception {
        ClassViewController ctrl = new ClassViewController();
        Label nameLabel = new Label();
        Label codeLabel = new Label();
        VBox studentList = new VBox();
        VBox teacherSection = new VBox();
        VBox studySetList = new VBox();

        injectField(ctrl, "classNameLabel", nameLabel);
        injectField(ctrl, "classCodeLabel", codeLabel);
        injectField(ctrl, "studentListVBox", studentList);
        injectField(ctrl, "teacherSection", teacherSection);
        injectField(ctrl, "studySetListVBox", studySetList);

        User teacherUser = new User(100, "teacher1", "x", true);
        Classroom cls = new Classroom("TestClass", "CODE1", teacherUser);
        User s1 = new User(10, "alice", "x", false);
        User s2 = new User(11, "bob", "x", false);
        cls.addStudent(s1);
        cls.addStudent(s2);

        injectField(ctrl, "classroom", cls);

        Method m = getMethod(ctrl, "loadStudents");
        m.invoke(ctrl);

        assertEquals(2, studentList.getChildren().size());
        Label first = (Label) studentList.getChildren().get(0);
        assertTrue(first.getText().contains("alice"));
    }

    @Test
    public void testClassViewControllerBuildGradesBoxNoStudents() throws Exception {
        ClassViewController ctrl = new ClassViewController();
        Label nameLabel = new Label();
        Label codeLabel = new Label();
        VBox studentList = new VBox();
        VBox teacherSection = new VBox();
        VBox studySetList = new VBox();

        injectField(ctrl, "classNameLabel", nameLabel);
        injectField(ctrl, "classCodeLabel", codeLabel);
        injectField(ctrl, "studentListVBox", studentList);
        injectField(ctrl, "teacherSection", teacherSection);
        injectField(ctrl, "studySetListVBox", studySetList);

        User teacherUser2 = new User(101, "teacher1", "x", true);
        Classroom cls = new Classroom("Empty", "CODE2", teacherUser2);
        injectField(ctrl, "classroom", cls);

        VBox gradesBox = new VBox();
        Method m = getMethod(ctrl, "buildGradesBox", VBox.class, int.class);
        m.invoke(ctrl, gradesBox, 1);

        assertEquals(1, gradesBox.getChildren().size());
        Label lbl = (Label) gradesBox.getChildren().get(0);
        assertTrue(lbl.getText().contains("No students enrolled"));
    }

    @Test
    public void testClassViewControllerLoadStudentStudySetViewNullIds() throws Exception {
        ClassViewController ctrl = new ClassViewController();
        VBox studySetList = new VBox();
        injectField(ctrl, "studySetListVBox", studySetList);
        injectField(ctrl, "user", new User(1, "u", "x", false));

        ctrl.loadStudentStudySetView(null);
        assertEquals(0, studySetList.getChildren().size());

        ctrl.loadStudentStudySetView(new ArrayList<>());
        assertEquals(0, studySetList.getChildren().size());
    }

    // ─── StruggleViewController ────────────────────────────────────────

    @Test
    public void testStruggleViewControllerSetDataStudentNoStruggles() throws Exception {
        StruggleViewController ctrl = new StruggleViewController();
        Label titleLabel = new Label();
        VBox struggleList = new VBox();
        Button practiceButton = new Button();

        injectField(ctrl, "titleLabel", titleLabel);
        injectField(ctrl, "struggleListVBox", struggleList);
        injectField(ctrl, "practiceButton", practiceButton);

        User student = new User(99, "nostruggles", "x", false);
        // wrongQuestionData is empty by default
        ctrl.setDataStudent(student);

        assertTrue(titleLabel.getText().contains("nostruggles"));
        assertTrue(practiceButton.isDisabled());
    }

    @Test
    public void testStruggleViewControllerSetDataTeacherEmptyStudents() throws Exception {
        StruggleViewController ctrl = new StruggleViewController();
        Label titleLabel = new Label();
        VBox struggleList = new VBox();
        Button practiceButton = new Button();

        injectField(ctrl, "titleLabel", titleLabel);
        injectField(ctrl, "struggleListVBox", struggleList);
        injectField(ctrl, "practiceButton", practiceButton);

        User teacher = new User(1, "teach", "x", true);
        ctrl.setDataTeacher(teacher, "MathClass", new ArrayList<>());

        assertTrue(titleLabel.getText().contains("No Students Enrolled"));
        assertTrue(practiceButton.isDisabled());
    }

    @Test
    public void testStruggleViewControllerBuildStruggleListFiltersLowScores() throws Exception {
        StruggleViewController ctrl = new StruggleViewController();
        Label titleLabel = new Label();
        VBox struggleList = new VBox();
        Button practiceButton = new Button();

        injectField(ctrl, "titleLabel", titleLabel);
        injectField(ctrl, "struggleListVBox", struggleList);
        injectField(ctrl, "practiceButton", practiceButton);

        Map<String, Double> vector = new HashMap<>();
        vector.put("algebra", 0.8);
        vector.put("geometry", 0.5);
        vector.put("tiny", 0.005); // below 0.01 threshold, should be filtered

        injectField(ctrl, "struggleVector", vector);

        Method m = getMethod(ctrl, "buildStruggleList");
        m.invoke(ctrl);

        // should show 2 rows (algebra and geometry), not 3
        assertEquals(2, struggleList.getChildren().size());
    }

    @Test
    public void testStruggleViewControllerBuildStruggleListAllLowScores() throws Exception {
        StruggleViewController ctrl = new StruggleViewController();
        Label titleLabel = new Label();
        VBox struggleList = new VBox();
        Button practiceButton = new Button();

        injectField(ctrl, "titleLabel", titleLabel);
        injectField(ctrl, "struggleListVBox", struggleList);
        injectField(ctrl, "practiceButton", practiceButton);

        Map<String, Double> vector = new HashMap<>();
        vector.put("tiny1", 0.001);
        vector.put("tiny2", 0.005);

        injectField(ctrl, "struggleVector", vector);

        Method m = getMethod(ctrl, "buildStruggleList");
        m.invoke(ctrl);

        // all filtered out → "No major struggles found" label
        assertEquals(1, struggleList.getChildren().size());
        assertTrue(practiceButton.isDisabled());
    }

    // ─── SearchService ─────────────────────────────────────────────────

    @Test
    public void testSearchServiceScoreStudySetNullOrEmpty() {
        SearchService svc = new SearchService("nonexistent.json", "nonexistent", new com.fasterxml.jackson.databind.ObjectMapper());
        Map<String, Double> vector = new HashMap<>();
        vector.put("math", 0.5);

        assertEquals(0.0, svc.scoreStudySet(null, vector));

        StudySet noTags = new StudySet();
        noTags.setTags(null);
        assertEquals(0.0, svc.scoreStudySet(noTags, vector));

        StudySet emptyTags = new StudySet();
        emptyTags.setTags(new ArrayList<>());
        assertEquals(0.0, svc.scoreStudySet(emptyTags, vector));
    }

    @Test
    public void testSearchServiceScoreStudySetWithTags() {
        SearchService svc = new SearchService("nonexistent.json", "nonexistent", new com.fasterxml.jackson.databind.ObjectMapper());
        Map<String, Double> vector = new HashMap<>();
        vector.put("math", 0.8);
        vector.put("science", 0.4);

        StudySet s = new StudySet();
        s.setTags(Arrays.asList("Math", "Science"));
        double score = svc.scoreStudySet(s, vector);
        assertEquals(0.6, score, 0.01); // (0.8 + 0.4) / 2
    }

    @Test
    public void testSearchServiceNormalizeStruggleVector() throws Exception {
        SearchService svc = new SearchService("nonexistent.json", "nonexistent", new com.fasterxml.jackson.databind.ObjectMapper());
        Map<String, Double> input = new HashMap<>();
        input.put("  Math ", 0.5);
        input.put("SCIENCE", 0.3);
        input.put(null, 0.1);

        Method m = getMethod(svc, "normalizeStruggleVector", Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Double> result = (Map<String, Double>) m.invoke(svc, input);

        assertTrue(result.containsKey("math"));
        assertTrue(result.containsKey("science"));
        assertFalse(result.containsKey(null));
        assertEquals(0.5, result.get("math"), 0.01);
    }

    @Test
    public void testSearchServiceIsValidSet() throws Exception {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        Method m = getMethod(svc, "isValidSet", model.BaseSet.class);

        assertTrue((Boolean) m.invoke(svc, (Object) null));

        StudySet noQ = new StudySet();
        noQ.setQuestionSet(null);
        assertTrue((Boolean) m.invoke(svc, noQ));

        StudySet withQ = new StudySet();
        withQ.setQuestionSet(Arrays.asList(new Question(1, "q", "a")));
        assertFalse((Boolean) m.invoke(svc, withQ));
    }

    @Test
    public void testSearchServiceSearchStudySetsByTitleNullQuery() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        assertTrue(svc.searchStudySetsByTitle(null).isEmpty());
        assertTrue(svc.searchStudySetsByTitle("").isEmpty());
    }

    @Test
    public void testSearchServiceSearchQuestionsByTagsNullOrEmpty() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        assertTrue(svc.searchQuestionsByTags(null).isEmpty());
        assertTrue(svc.searchQuestionsByTags(new ArrayList<>()).isEmpty());
    }

    @Test
    public void testSearchServiceSearchStudySetsByTagsNullOrEmpty() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        assertTrue(svc.searchStudySetsByTags(null).isEmpty());
        assertTrue(svc.searchStudySetsByTags(new ArrayList<>()).isEmpty());
    }

    @Test
    public void testSearchServiceGetUserStruggleVectorNull() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        assertTrue(svc.getUserStruggleVector(null).isEmpty());
    }

    @Test
    public void testSearchServiceRecommendTopQuestionsZeroCount() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        User u = new User(1, "u", "x", false);
        assertTrue(svc.recommendTopQuestionsForUser(u, 0).isEmpty());
        assertTrue(svc.recommendTopQuestionsForUser(u, -1).isEmpty());
    }

    @Test
    public void testSearchServiceRecommendTopStudySetsZeroCount() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        User u = new User(1, "u", "x", false);
        assertTrue(svc.recommendTopStudySetsForUser(u, 0).isEmpty());
        assertTrue(svc.recommendTopStudySetsForUser(u, -1).isEmpty());
    }

    @Test
    public void testSearchServiceRecommendTopQuestionsGivenVectorZeroCount() {
        SearchService svc = new SearchService("x", "x", new com.fasterxml.jackson.databind.ObjectMapper());
        Map<String, Double> v = new HashMap<>();
        assertTrue(svc.recommendTopQuestionsGivenVector(v, 0).isEmpty());
        assertTrue(svc.recommendTopQuestionsGivenVector(v, -1).isEmpty());
    }

    @Test
    public void testSearchServiceLoadAllStudySetsNonexistentFolder() {
        SearchService svc = new SearchService("x", "nonexistent_folder_abc", new com.fasterxml.jackson.databind.ObjectMapper());
        assertTrue(svc.loadAllStudySets().isEmpty());
    }

    // ─── QuestionTracker edge cases ────────────────────────────────────

    @Test
    public void testQuestionTrackerCreateClassOnlyTeachers() {
        User notTeacher = new User(5, "u5", "x", false);
        assertNull(QuestionTracker.createClass("Name", "CODE", notTeacher));
        assertNull(QuestionTracker.createClass("N", "C", null));
    }

    @Test
    public void testQuestionTrackerGetQuestionSetByIdNotFound() {
        QuestionSet result = QuestionTracker.getQuestionSetById(-999);
        assertNull(result);
    }

    @Test
    public void testQuestionTrackerGetQuestionSetsForUserEmpty() {
        User ghost = new User(9999, "ghost", "x", false);
        QuestionSet[] result = QuestionTracker.getQuestionSetsForUser(ghost);
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
