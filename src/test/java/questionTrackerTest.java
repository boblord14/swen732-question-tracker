import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import user.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class questionTrackerTest {

    questionTracker qt = new questionTracker();

    @Test
    void testLoginValidCredentials(){
        // Simple teacher and student accounts
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("teacher1");
        user1.setPassword("password1");
        user1.setIsTeacher(true);

        User[] mockUsers = {user1};
        // Mockito to use real methods
        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            User result1 = questionTracker.logIn("teacher1", "password1");
            assertNotNull(result1);
            assertEquals("teacher1", result1.getUsername());
            assertTrue(result1.getIsTeacher());
        }
    }

    @Test
    void testLoginInvalidCredentials(){
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("student2");
        user2.setPassword("password2");
        user2.setIsTeacher(false);
        User[] mockUsers = {user2};
        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            User result =  questionTracker.logIn("student2", "wrongpassword2");
            assertNull(result);
        }
    }

    @Test
    void testUserDoesNotExist(){
        User user3 = new User();
        user3.setId(3);
        user3.setUsername("student3");
        user3.setPassword("password3");
        user3.setIsTeacher(false);
        User[] mockUsers = {user3};

        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            User result =  questionTracker.logIn("newuser", "password3");
            assertNull(result);
        }
    }

    @Test
    void testGetUsers(){
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

            User[] result = questionTracker.getUsers();
            assertNotNull(result);
            assertEquals(2, result.length);
            assertEquals(user1, result[0]);
            assertEquals(user2, result[1]);
        }
    }
}