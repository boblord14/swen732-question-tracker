package tests;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import model.QuestionTracker;
import user.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for logging in and utilizing the functionality of the system
 */
class QuestionTrackerTest {

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

    @Test
    void testSignUpSuccess() {
        User existing = new User(1, "existing", "pass", false);
        User[] mockUsers = {existing};

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);
            mocked.when(() -> QuestionTracker.saveUser(any())).thenCallRealMethod();

            User newUser = QuestionTracker.signUp("newuser", "newpass", false);

            assertNotNull(newUser);
            assertEquals("newuser", newUser.getUsername());
            assertEquals(2, newUser.getId()); // next ID
        }
    }

    @Test
    void testSignUpDuplicateUsername() {
        User existing = new User(1, "user1", "pass", false);
        User[] mockUsers = {existing};

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getUsers).thenReturn(mockUsers);

            User result = QuestionTracker.signUp("user1", "newpass", false);

            assertNull(result);
        }
    }

    @Test
    void testCreateClassValidTeacher() {
        User teacher = new User(1, "teacher", "pass", true);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[0]);

            user.Classroom result = QuestionTracker.createClass("Math", "1234", teacher);

            assertNotNull(result);
            assertEquals("Math", result.getName());
            assertEquals("1234", result.getCode());
        }
    }

    @Test
    void testCreateClassNonTeacher() {
        User student = new User(2, "student", "pass", false);

        user.Classroom result = QuestionTracker.createClass("Science", "9999", student);

        assertNull(result);
    }

    @Test
    void testJoinClassSuccess() {
        User student = new User(1, "student", "pass", false);

        user.Classroom classroom = new user.Classroom("TestClass", "1234", new User(99, "teacher", "pass", true));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getClasses)
                .thenReturn(new user.Classroom[]{classroom});

            boolean result = QuestionTracker.joinClass(student, "1234");

            assertTrue(result);
            assertTrue(classroom.getStudents().contains(student));
        }
    }

    @Test
    void testAddQuestionToSet() {

        // REAL QuestionSet (no mocking)
        user.QuestionSet set = new user.QuestionSet(1, "Math", "teacher1");

        user.QuestionSet[] sets = { set };

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets).thenReturn(sets);

            boolean result = QuestionTracker.addQuestionToSet(
                    1,
                    "What is 2+2?",
                    "4",
                    java.util.List.of("math", "basic")
            );

            assertTrue(result);

            // verify real mutation happened
            assertEquals(1, set.getQuestions().size());
            assertEquals("What is 2+2?", set.getQuestions().get(0).getText());
        }
    }

    @Test
    void testEditQuestionInSet() {
        // REAL question (not mocked)
        user.Question question = new user.Question(10, "Old Q", "Old A");

        // REAL set with real behavior
        user.QuestionSet set = new user.QuestionSet(1, "set", "creator");

        set.addQuestion(question);

        user.QuestionSet[] sets = { set };

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets).thenReturn(sets);

            boolean result = QuestionTracker.editQuestionInSet(
                    1,
                    10,
                    "New Q",
                    "New A",
                    null
            );

            assertTrue(result);
            assertEquals("New Q", question.getText());
            assertEquals("New A", question.getAnswer());
        }
    }

    @Test
    void testGetUserById() {
        User user = new User(5, "test", "pass", false);
        User[] users = {user};

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getUsers).thenReturn(users);

            User result = QuestionTracker.getUserById(5);

            assertNotNull(result);
            assertEquals("test", result.getUsername());
        }
    }

    @Test
    void testGetClassByNameFound() {
        user.Classroom classroom = new user.Classroom("Math", "1234",
                new user.User(1, "teacher", "pass", true));

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getClasses)
                .thenReturn(new user.Classroom[]{classroom});

            user.Classroom result = QuestionTracker.getClassByName("Math");

            assertNotNull(result);
            assertEquals("Math", result.getName());
        }
    }

    @Test
    void testGetClassByNameNotFound() {
        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getClasses)
                .thenReturn(new user.Classroom[0]);

            assertNull(QuestionTracker.getClassByName("Nothing"));
        }
    }

    @Test
    void testRemoveQuestionFromSet() {
        user.QuestionSet set = new user.QuestionSet(1, "Set", "teacher");

        user.Question q = new user.Question(1, "Q", "A");
        set.addQuestion(q);

        user.QuestionSet[] sets = {set};

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets).thenReturn(sets);

            boolean result = QuestionTracker.removeQuestionFromSet(1, 1);

            assertTrue(result);
            assertTrue(set.getQuestions().isEmpty());
        }
    }

    @Test
    void testUpdateQuestionSetMetadataSuccess() {
        user.User creator = new user.User(1, "teacher", "pass", true);

        user.QuestionSet set = new user.QuestionSet(1, "OldName", "teacher");

        user.QuestionSet[] sets = {set};

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets).thenReturn(sets);

            boolean result = QuestionTracker.updateQuestionSetMetadata(
                    1,
                    creator,
                    "NewName",
                    java.util.List.of("tag1")
            );

            assertTrue(result);
            assertEquals("NewName", set.getName());
        }
    }

    @Test
    void testUpdateQuestionSetMetadataFailWrongUser() {
        user.User other = new user.User(2, "other", "pass", false);

        user.QuestionSet set = new user.QuestionSet(1, "Old", "creator");

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets)
                .thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.updateQuestionSetMetadata(
                    1,
                    other,
                    "New",
                    null
            );

            assertFalse(result);
            assertEquals("Old", set.getName());
        }
    }

    @Test
    void testAddQuestionToSetNullTags() {

        user.QuestionSet set = new user.QuestionSet(1, "Set", "creator");
        user.QuestionSet[] sets = {set};

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getQuestionSets).thenReturn(sets);

            boolean result = QuestionTracker.addQuestionToSet(
                    1,
                    "Q?",
                    "A",
                    null
            );

            assertTrue(result);
            assertEquals(1, set.getQuestions().size());
        }
    }

    @Test
    void testAssignStudySetToClass() {

        user.User teacher = new user.User(1, "teacher", "pass", true);

        user.Classroom classroom = new user.Classroom(
                "Math",
                "1234",
                teacher
        );

        user.Classroom[] classes = { classroom };

        try (MockedStatic<QuestionTracker> mocked =
                    mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {

            mocked.when(QuestionTracker::getClasses)
                .thenReturn(classes);

            try (MockedStatic<model.StudySetMaker> mocked2 =
                        mockStatic(model.StudySetMaker.class)) {

                mocked2.when(() -> model.StudySetMaker.getSetById(1))
                    .thenReturn(null);

                boolean result = QuestionTracker.assignStudySetToClass("Math", 1);

                assertTrue(result);
            }
        }
    }
}