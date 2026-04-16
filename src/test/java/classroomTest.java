import org.junit.jupiter.api.BeforeEach;

import model.questionTracker;
import user.User;
import user.Classroom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class classroomTest {
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
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);


        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = questionTracker.createClass(name, code, user);

        assertEquals(name, classroom.getName());
        assertEquals(code, classroom.getCode());
        assertEquals(user, classroom.getTeacher());
    }

    //Create a classroom from a nonteacher account, expect null return
    @org.junit.jupiter.api.Test
    void testNonteacherCreateClassroom() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", false);

        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = questionTracker.createClass(name, code, user);

        assertNull(classroom);
    }

    //Create a classroom with the same code as another class, expect null return
    @org.junit.jupiter.api.Test
    void testDuplicateCodeCreateClassroom() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        Classroom classroom = questionTracker.createClass(name, code, user);
        Classroom classroom2 = questionTracker.createClass(name, code, user);

        assertNotNull(classroom);
        assertNull(classroom2);
    }

    //Test class list get being empty
    @org.junit.jupiter.api.Test
    void testEmptyClassList() {
        //questionTracker qt = new questionTracker();

        Classroom[] classList = questionTracker.getClasses();

        assertEquals(0, classList.length); //no saved classes, should be empty
    }

    //Test class list fetch
    @org.junit.jupiter.api.Test
    void testFetchClassList() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        questionTracker.createClass(name, code, user);

        Classroom[] classList = questionTracker.getClasses();

        assertEquals(1, classList.length);
        assertEquals(name, classList[0].getName());
        assertEquals(code, classList[0].getCode());
        assertEquals(user.getId(), classList[0].getTeacher().getId());
    }

    //Test join class success
    @org.junit.jupiter.api.Test
    void testJoinClass() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        questionTracker.createClass(name, code, user);

        User student = new User(2, "Student", "test_stu", false);

        boolean success = questionTracker.joinClass(student, code);

        Classroom[] classList = questionTracker.getClasses();

        assertTrue(success);
        assertEquals(1, classList[0].getStudents().size());
        assertEquals("Student", classList[0].getStudents().get(0).getUsername());
    }

    //Test join class failure on bad code
    @org.junit.jupiter.api.Test
    void testJoinClassBadCode() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        questionTracker.createClass(name, code, user);

        String username = "Student";
        User student = new User(2, username, "test_stu", false);

        String badCode = "nope!";

        boolean success = questionTracker.joinClass(student, badCode);

        Classroom[] classList = questionTracker.getClasses();

        assertFalse(success);
        assertEquals(0, classList[0].getStudents().size());
    }

    //Test join class failure on student already present
    @org.junit.jupiter.api.Test
    void testJoinClassAlreadyPresent() {
        //questionTracker qt = new questionTracker();

        User user = new User(1, "test", "test", true);

        String name = "Classroom Name";
        String code = "1234";

        questionTracker.createClass(name, code, user);

        String username = "Student";
        User student = new User(2, "Student", "test_stu", false);

        boolean firstSuccess = questionTracker.joinClass(student, code);
        boolean secondSuccess = questionTracker.joinClass(student, code);

        Classroom[] classList = questionTracker.getClasses();

        assertTrue(firstSuccess);
        assertFalse(secondSuccess);
        assertEquals(1, classList[0].getStudents().size());
        assertEquals(username, classList[0].getStudents().get(0).getUsername());
    }
}
