import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class signupTest {
    private final Path usersFile = Paths.get("src/main/users.json");

    @BeforeEach
    //delete users file before each test so we're testing on a fresh DB
    void setUp() throws IOException {
        Files.deleteIfExists(usersFile);
    }

    //Sign up test that creates a successful student account
    @org.junit.jupiter.api.Test
    void testSignupStudentSuccessful(){
        questionTracker qt = new questionTracker();

        String username = "User0";
        String password = "Password0";

        User signup_info = questionTracker.signUp(username, password, false);

        assertEquals(signup_info.getUsername(), "User0");
        assertEquals(signup_info.getPassword(), "Password0");
        assertFalse(signup_info.getIsTeacher());
    }

    //Sign up test that creates a successful student account
    @org.junit.jupiter.api.Test
    void testSignupTeacherSuccessful(){
        questionTracker qt = new questionTracker();

        String username = "Teacher1";
        String password = "TPassword1";

        
        User signup = questionTracker.signUp(username, password, true);

        assertEquals(signup.getUsername(), "Teacher1");
        assertEquals(signup.getPassword(), "TPassword1");
        assertTrue(signup.getIsTeacher());
    }

}
