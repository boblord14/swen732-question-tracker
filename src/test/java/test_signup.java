import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import user.User;

public class test_signup {
    //Sign up test that creates a successful student account
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

    //Sign up test that creates a successful student account
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

    //A fail test used to show what happens when you sign up using a pre-existing username
    @org.junit.jupiter.api.Test
    void testSignupPreexistingUser(){
        questionTracker qt = new questionTracker();

        String username = "NewUser";
        String password = "Password3";

        User signup_info = qt.signUp(username, password, false);

        assertEquals(signup_info.getUsername(), "NewUser");
        assertEquals(signup_info.getPassword(), "Password3");
        assertFalse(signup_info.getIsTeacher());

        //If a user name is used that's already in the system this function will return null
        User repeated_signup = qt.signUp(username, password, false);
        assertNull(repeated_signup);
    }
}
