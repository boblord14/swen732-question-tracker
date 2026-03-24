import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import user.User;

public class test_signup {
    @org.junit.jupiter.api.Test
    void testSignupStudentSuccessful(){
        questionTracker qt = new questionTracker();

        String username = "User0";
        String password = "Password0";

        User signup_info = qt.signUp(username, password, false);

        assertEquals(signup_info.getUsername(), "User0");
        assertEquals(signup_info.getPassword(), "Password0");
        assertFalse(signup_info.getIsTeacher());
    }

    @org.junit.jupiter.api.Test
    void testSignupTeacherSuccessful(){
        questionTracker qt = new questionTracker();

        String username = "Teacher1";
        String password = "TPassword1";

        User signup = qt.signUp(username, password, true);

        assertEquals(signup.getUsername(), "Teacher1");
        assertEquals(signup.getPassword(), "TPassword1");
        assertTrue(signup.getIsTeacher());
    }

    @org.junit.jupiter.api.Test
    void testSignupPreexistingUser(){
        questionTracker qt = new questionTracker();

        String username = "NewUser";
        String password = "Password3";

        User signup_info = qt.signUp(username, password, false);

        assertEquals(signup_info.getUsername(), "NewUser");
        assertEquals(signup_info.getPassword(), "Password3");
        assertFalse(signup_info.getIsTeacher());

        User repeated_signup = qt.signUp(username, password, false);
        assertNull(repeated_signup);
    }
}
