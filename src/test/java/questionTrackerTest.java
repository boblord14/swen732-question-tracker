//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import model.QuestionTracker;
import user.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for logging in and utilizing the functionality of the system
 */
class questionTrackerTest {

    QuestionTracker qt = new QuestionTracker();

    /**
     * Test whether logging in with correct credentials allows a user to access the system
     */
    @Test
    void testLoginValidCredentials(){
        // Simple teacher account
        User user1 = new User(1, "teacher1", "password1", true);

        User[] mockUsers = {user1};
        // Mockito to use real methods
        try(MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);

            // Try to log in with correct information
            User result1 = QuestionTracker.logIn("teacher1", "password1");
            // Expect to be let in to the system
            assertNotNull(result1);
            assertEquals("teacher1", result1.getUsername());
            assertTrue(result1.getIsTeacher());
        }
    }


    /**
     * Test whether the system can be accessed if invalid login credentials are input
     */
    @Test
    void testLoginInvalidCredentials(){
        // Simple user account
        User user2 = new User(2, "student2", "password2", false);
        User[] mockUsers = {user2};

        // Mock the real getUsers method
        try(MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);

            // Try to login with incorrect information
            User result =  QuestionTracker.logIn("student2", "wrongpassword2");
            // Expect to not be able to access the system
            assertNull(result);
        }
    }

    /**
     * Tests if a user can login with an account that has not yet been created
     */
    @Test
    void testUserDoesNotExist(){
        // Simple user account
        User user3 = new User(3, "student3", "password3", false);
        User[] mockUsers = {user3};

        try(MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);

            // Try to login with completely different user details
            User result =  QuestionTracker.logIn("newuser", "password3");
            // Expect to not be able to access the system
            assertNull(result);
        }
    }

    /**
     * Test if the system can accurately return each user with an account in the system
     */
    @Test
    void testGetUsers(){
        // Multiple user accounts
        User user1 = new User(1, "teacher1", "password1", true);
        User user2 = new User(2, "student2", "password2", false);
        User[] mockUsers = {user1, user2};

        try(MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);

            // Try to return each of the users that were created
            User[] result = QuestionTracker.getUsers();
            // Expect to return both created users with their correct usernames
            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(user1, result[0]);
            assertEquals(user2, result[1]);
        }
    }
}