package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.QuestionTracker;
import user.User;
import user.Classroom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassroomTest {
    private final Path classFile = Paths.get("src/main/classes.json");
    private final Path usersFile = Paths.get("src/main/users.json");

    @BeforeEach
        //delete users and class file before each test so we're testing on a fresh DB
    void setUp() throws IOException {
        Files.deleteIfExists(usersFile);
        Files.deleteIfExists(classFile);
    }

    //Create a classroom normally, expect success
    @org.junit.jupiter.api.Test
    void testCreateClassroom() {

        User user = new User(1, "test", "test", true);


        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = QuestionTracker.createClass(name, code, user);

        assertEquals(name, classroom.getName());
        assertEquals(code, classroom.getCode());
        assertEquals(user, classroom.getTeacher());
    }

    //Create a classroom from a nonteacher account, expect null return
    @org.junit.jupiter.api.Test
    void testNonteacherCreateClassroom() {

        User user = new User(1, "test", "test", false);

        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = QuestionTracker.createClass(name, code, user);

        assertNull(classroom);
    }

    //Create a classroom with the same code as another class, expect null return
    @org.junit.jupiter.api.Test
    void testDuplicateCodeCreateClassroom() {

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = QuestionTracker.createClass(name, code, user);
        Classroom classroom2 = QuestionTracker.createClass(name, code, user);

        assertNotNull(classroom);
        assertNull(classroom2);
    }

    //Test class list get being empty
    @org.junit.jupiter.api.Test
    void testEmptyClassList() {

        Classroom[] classList = QuestionTracker.getClasses();

        assertEquals(0, classList.length); //no saved classes, should be empty
    }

    //Test class list fetch
    @org.junit.jupiter.api.Test
    void testFetchClassList() {

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        QuestionTracker.createClass(name, code, user);

        Classroom[] classList = QuestionTracker.getClasses();

        assertEquals(1, classList.length);
        assertEquals(name, classList[0].getName());
        assertEquals(code, classList[0].getCode());
        assertEquals(user.getId(), classList[0].getTeacher().getId());
    }

    //Test join class success
    @org.junit.jupiter.api.Test
    void testJoinClass() {

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        QuestionTracker.createClass(name, code, user);

        User student = new User(2, "Student", "test_stu", false);

        boolean success = QuestionTracker.joinClass(student, code);

        Classroom[] classList = QuestionTracker.getClasses();

        assertTrue(success);
        assertEquals(1, classList[0].getStudents().size());
        assertEquals("Student", classList[0].getStudents().get(0).getUsername());
    }

    //Test join class failure on bad code
    @org.junit.jupiter.api.Test
    void testJoinClassBadCode() {

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        QuestionTracker.createClass(name, code, user);

        String username = "Student";
        User student = new User(2, username, "test_stu", false);

        String badCode = "nope!";

        boolean success = QuestionTracker.joinClass(student, badCode);

        Classroom[] classList = QuestionTracker.getClasses();

        assertFalse(success);
        assertEquals(0, classList[0].getStudents().size());
    }

    //Test join class failure on student already present
    @org.junit.jupiter.api.Test
    void testJoinClassAlreadyPresent() {

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        QuestionTracker.createClass(name, code, user);

        String username = "Student";
        User student = new User(2, "Student", "test_stu", false);

        boolean firstSuccess = QuestionTracker.joinClass(student, code);
        boolean secondSuccess = QuestionTracker.joinClass(student, code);

        Classroom[] classList = QuestionTracker.getClasses();

        assertTrue(firstSuccess);
        assertFalse(secondSuccess);
        assertEquals(1, classList[0].getStudents().size());
        assertEquals(username, classList[0].getStudents().get(0).getUsername());
    }

    @Test
    void testRemoveStudent() {

        User teacher = new User(1, "teacher", "pass", true);
        Classroom classroom = new Classroom("Math", "1234", teacher);

        User student = new User(2, "student", "pass", false);

        classroom.addStudent(student);
        boolean removed = classroom.removeStudent(student);

        assertTrue(removed);
        assertEquals(0, classroom.getStudents().size());
    }

    @Test
    void testAddStudySetNull() {

        Classroom classroom = new Classroom();

        boolean result = classroom.addStudySet(null);

        assertFalse(result);
        assertEquals(0, classroom.getAssignedStudySets().size());
    }

    @Test
    void testAddAssignedStudySetIdDuplicate() {

        Classroom classroom = new Classroom();

        boolean first = classroom.addAssignedStudySetId(1);
        boolean second = classroom.addAssignedStudySetId(1);

        assertTrue(first);
        assertFalse(second);
    }

    @Test
    void testCheckCodeNullCases() {

        Classroom classroom = new Classroom("Math", "1234", new User(1, "t", "p", true));

        assertFalse(classroom.checkCode(null));

        Classroom nullCodeClass = new Classroom();
        assertFalse(nullCodeClass.checkCode("1234"));
    }

    @Test
    void testStudentStruggleVectorNotFound() {

        Classroom classroom = new Classroom("Math", "1234", new User(1, "t", "p", true));

        Map<String, Double> result = classroom.studentStruggleVector(999);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStudentsSetScores() {

        Classroom classroom = new Classroom("Math", "1234", new User(1, "t", "p", true));

        User student = new User(2, "s", "p", false);

        Map<Integer, Double> mockScores = new HashMap<>();
        mockScores.put(1, 90.0);

        student.setStudySetAvg(mockScores);
        classroom.addStudent(student);

        List<Map<Integer, Double>> result = classroom.studentsSetScores();

        assertEquals(1, result.size());
        assertEquals(90.0, result.get(0).get(1));
    }

    @Test
    void testDefaultConstructor() {

        Classroom classroom = new Classroom();

        assertNotNull(classroom.getStudents());
        assertNotNull(classroom.getAssignedStudySets());
        assertNotNull(classroom.getAssignedStudySetIds());
    }

    @Test
    void testAddNullStudent() {

        Classroom classroom = new Classroom();

        boolean result = classroom.addStudent(null);

        assertFalse(result);
    }
}
