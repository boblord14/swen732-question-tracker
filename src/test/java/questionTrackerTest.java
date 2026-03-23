import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import user.User;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class questionTrackerTest {

    questionTracker qt = new questionTracker();
    private static final File USERS_FILE = new File("src/main/users.json");
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
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

        User[] users = {user1,user2};

        mapper.writerWithDefaultPrettyPrinter().writeValue(USERS_FILE, users);
    }
}