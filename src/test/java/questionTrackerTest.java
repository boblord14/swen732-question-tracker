import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import user.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for logging in and utilizing the functionality of the system
 */
class questionTrackerTest {

    questionTracker qt = new questionTracker();

    /**
     * Test whether logging in with correct credentials allows a user to access the system
     */
    @Test
    void testLoginValidCredentials(){
        // Simple teacher account
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher1");
        user1.setPassword("password1");
        user1.setIsTeacher(true);

        User[] mockUsers = {user1};
        // Mockito to use real methods
        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            // Try to log in with correct information
            User result1 = questionTracker.logIn("teacher1", "password1");
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
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("student2");
        user2.setPassword("password2");
        user2.setIsTeacher(false);
        User[] mockUsers = {user2};

        // Mock the real getUsers method
        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            // Try to login with incorrect information
            User result =  questionTracker.logIn("student2", "wrongpassword2");
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
        User user3 = new User();
        user3.setId(3);
        user3.setUsername("student3");
        user3.setPassword("password3");
        user3.setIsTeacher(false);
        User[] mockUsers = {user3};

        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            // Try to login with completely different user details
            User result =  questionTracker.logIn("newuser", "password3");
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
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher1");
        user1.setPassword("password1");
        user1.setIsTeacher(true);
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("student2");
        user2.setPassword("password2");
        user2.setIsTeacher(false);
        User[] mockUsers = {user1, user2};

        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            // Try to return each of the users that were created
            User[] result = questionTracker.getUsers();
            // Expect to return both created users with their correct usernames
            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(user1, result[0]);
            assertEquals(user2, result[1]);
        }
    }
}