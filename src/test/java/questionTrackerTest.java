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
        try(MockedStatic<questionTracker> mocked = mockStatic(questionTracker.class, CALLS_REAL_METHODS)){
            mocked.when(questionTracker::getUsers).thenReturn(mockUsers);

            User result1 = questionTracker.logIn("teacher1", "password1");
            assertNotNull(result1);
            assertEquals("teacher1", result1.getUsername());
            assertTrue(result1.getIsTeacher());
        }
    }
}