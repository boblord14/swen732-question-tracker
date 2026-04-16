package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;

import model.QuestionTracker;
import user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class SignupTest {
    private final Path usersFile = Paths.get("src/main/resources/users.json");

    @BeforeEach
    //delete users file before each test so we're testing on a fresh DB
    void setUp() throws IOException {
        Files.deleteIfExists(usersFile);
    }

    //Sign up test that creates a successful student account
    @org.junit.jupiter.api.Test
    void testSignupStudentSuccessful(){
        String username = "User0";
        String password = "Password0";

        User signupInfo = QuestionTracker.signUp(username, password, false);

        assertEquals("User0", signupInfo.getUsername());
        assertEquals("Password0", signupInfo.getPassword());
        assertFalse(signupInfo.getIsTeacher());
    }

    //Sign up test that creates a successful student account
    @org.junit.jupiter.api.Test
    void testSignupTeacherSuccessful(){
        String username = "Teacher1";
        String password = "TPassword1";

        
        User signup = QuestionTracker.signUp(username, password, true);

        assertEquals("Teacher1", signup.getUsername());
        assertEquals("TPassword1", signup.getPassword());
        assertTrue(signup.getIsTeacher());
    }

}
