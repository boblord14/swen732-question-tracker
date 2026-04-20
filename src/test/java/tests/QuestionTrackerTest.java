package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import model.QuestionTracker;
import user.Classroom;
import user.QuestionSet;
import user.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @TempDir
    Path tempDir;

    private user.QuestionSet[] invokeBackwardsCompatibility(File dir) throws Exception {
        Method method = QuestionTracker.class.getDeclaredMethod("backwardsCompatabilityGetQuestionSets", File.class);
        method.setAccessible(true);
        return(user.QuestionSet[]) method.invoke(null, dir);
    }

    /**
     * Tests backwards compatibility if the file doesn't exist
     */
    @Test
    void testBackwardsCompatibilityGetQuestionSetsEmptyDirectory() throws Exception {
        File dir = tempDir.toFile();

        user.QuestionSet[] result = invokeBackwardsCompatibility(dir);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     *  Tests loading questions from json files
     */
    @Test
    void testBackwardsCompatibilityGetQuestionSetsLoadsValidJsonFiles() throws Exception {
        File dir = tempDir.toFile();

        String json1 = """
                {
                  "id": 1,
                  "name": "Set One",
                  "creator": "teacher1",
                  "questions": [],
                  "tags": []
                }
                """;

        String json2 = """
                {
                  "id": 2,
                  "name": "Set Two",
                  "creator": "teacher2",
                  "questions": [],
                  "tags": []
                }
                """;

        Files.writeString(tempDir.resolve("set1.json"), json1);
        Files.writeString(tempDir.resolve("set2.json"), json2);

        user.QuestionSet[] result = invokeBackwardsCompatibility(dir);

        assertNotNull(result);
        assertEquals(2, result.length);

        boolean found1 = false;
        boolean found2 = false;

        for (user.QuestionSet set : result) {
            if (set.getId() == 1 &&
                    "Set One".equals(set.getName()) &&
                    "teacher1".equals(set.getCreator())) {
                found1 = true;
            }
            if (set.getId() == 2 &&
                    "Set Two".equals(set.getName()) &&
                    "teacher2".equals(set.getCreator())) {
                found2 = true;
            }
        }

        assertTrue(found1);
        assertTrue(found2);
    }

    /**
     * Tests if invalid json formats will be skipped
     */
    @Test
    void testBackwardsCompatibilityGetQuestionSetsSkipsMalformedJson() throws Exception {
        File dir = tempDir.toFile();

        String validJson = """
                {
                  "id": 10,
                  "name": "Valid Set",
                  "creator": "teacher1",
                  "questions": [],
                  "tags": []
                }
                """;

        String invalidJson = "{ not valid json }";

        Files.writeString(tempDir.resolve("valid.json"), validJson);
        Files.writeString(tempDir.resolve("broken.json"), invalidJson);

        user.QuestionSet[] result = invokeBackwardsCompatibility(dir);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(10, result[0].getId());
        assertEquals("Valid Set", result[0].getName());
    }

    /**
     * Makes sure backwards compatibility works properly and removes old files
     */
    @Test
    void testBackwardsCompatibilityGetQuestionSetsDeletesOldFilesAndDirectory() throws Exception {
        Path oldDir = Files.createDirectory(tempDir.resolve("oldQuestionSets"));

        String json = """
                {
                  "id": 5,
                  "name": "Cleanup Set",
                  "creator": "teacherX",
                  "questions": [],
                  "tags": []
                }
                """;

        Path oldFile = oldDir.resolve("cleanup.json");
        Files.writeString(oldFile, json);

        user.QuestionSet[] result = invokeBackwardsCompatibility(oldDir.toFile());

        assertEquals(1, result.length);
        assertFalse(Files.exists(oldFile));
        assertFalse(Files.exists(oldDir));
    }

    @Test
    void testGetQuestionSetsForUserNullUser() {
        user.QuestionSet[] result = QuestionTracker.getQuestionSetsForUser(null);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetQuestionSetsForUserNoIds() {
        User user = new User(1, "student", "pass", false);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{
                    new user.QuestionSet(1, "Set1", "teacher")
            });

            user.QuestionSet[] result = QuestionTracker.getQuestionSetsForUser(user);

            assertNotNull(result);
            assertEquals(0, result.length);
        }
    }

    @Test
    void testGetQuestionSetsForUserFiltersOnlyOwnedSets() {
        User user = new User(1, "student", "pass", false);
        user.addQuestionSetId(2);

        user.QuestionSet set1 = new user.QuestionSet(1, "Set1", "teacher");
        user.QuestionSet set2 = new user.QuestionSet(2, "Set2", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set1, null, set2});

            user.QuestionSet[] result = QuestionTracker.getQuestionSetsForUser(user);

            assertEquals(1, result.length);
            assertEquals(2, result[0].getId());
        }
    }

    @Test
    void testCreateQuestionSetAssignsNextIdAndAddsIdToCreator() {
        User creator = new User(10, "teacherA", "pass", true);
        user.QuestionSet existing = new user.QuestionSet(7, "Existing", "teacherA");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{existing});
            mocked.when(() -> QuestionTracker.saveUser(any(User.class))).thenAnswer(invocation -> null);

            user.QuestionSet created = QuestionTracker.createQuestionSet("New Set", creator);

            assertNotNull(created);
            assertEquals(8, created.getId());
            assertEquals("New Set", created.getName());
            assertTrue(creator.getQuestionSetIds().contains(8));

            mocked.verify(() -> QuestionTracker.saveUser(any(User.class)));
        }
    }

    @Test
    void testGetQuestionSetByIdFound() {
        user.QuestionSet set = new user.QuestionSet(5, "Target", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            user.QuestionSet result = QuestionTracker.getQuestionSetById(5);

            assertNotNull(result);
            assertEquals("Target", result.getName());
        }
    }

    @Test
    void testGetQuestionSetByIdNotFound() {
        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{
                    new user.QuestionSet(1, "Other", "teacher")
            });

            user.QuestionSet result = QuestionTracker.getQuestionSetById(999);

            assertNull(result);
        }
    }

    @Test
    void testAddQuestionToSetReturnsFalseWhenSetNotFound() {
        user.QuestionSet set = new user.QuestionSet(1, "Set", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.addQuestionToSet(999, "Q", "A", null);

            assertFalse(result);
            assertEquals(0, set.getQuestions().size());
        }
    }

    @Test
    void testAddQuestionToSetUsesNextQuestionId() {
        user.QuestionSet set = new user.QuestionSet(1, "Set", "teacher");
        set.addQuestion(new user.Question(3, "Old", "A"));
        set.addQuestion(new user.Question(8, "Older", "B"));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.addQuestionToSet(1, "Newest", "C", java.util.List.of("tag"));

            assertTrue(result);
            assertEquals(3, set.getQuestions().size());
            assertEquals(9, set.getQuestions().get(2).getId());
        }
    }

    @Test
    void testRemoveQuestionFromSetReturnsFalseWhenSetNotFound() {
        user.QuestionSet set = new user.QuestionSet(1, "Set", "teacher");
        set.addQuestion(new user.Question(1, "Q", "A"));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.removeQuestionFromSet(999, 1);

            assertFalse(result);
            assertEquals(1, set.getQuestions().size());
        }
    }

    @Test
    void testEditQuestionInSetReturnsFalseWhenQuestionMissing() {
        user.QuestionSet set = new user.QuestionSet(1, "Set", "creator");
        set.addQuestion(new user.Question(10, "Old", "OldA"));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.editQuestionInSet(1, 999, "New", "NewA", java.util.List.of("x"));

            assertFalse(result);
            assertEquals("Old", set.getQuestions().get(0).getText());
        }
    }

    @Test
    void testEditQuestionInSetUpdatesTagsOnlyWhenProvided() {
        user.Question q = new user.Question(10, "Old Q", "Old A");
        user.QuestionSet set = new user.QuestionSet(1, "set", "creator");
        set.addQuestion(q);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.editQuestionInSet(
                    1,
                    10,
                    null,
                    null,
                    java.util.List.of("tag1", "tag2")
            );

            assertTrue(result);
            assertEquals("Old Q", q.getText());
            assertEquals("Old A", q.getAnswer());
            assertEquals(java.util.List.of("tag1", "tag2"), q.getTags());
        }
    }

    @Test
    void testUpdateQuestionSetMetadataReturnsFalseForNullRequester() {
        user.QuestionSet set = new user.QuestionSet(1, "Old", "creator");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.updateQuestionSetMetadata(1, null, "New", java.util.List.of("tag"));

            assertFalse(result);
            assertEquals("Old", set.getName());
        }
    }

    @Test
    void testUpdateQuestionSetMetadataUpdatesTagsOnly() {
        user.User creator = new user.User(1, "teacher", "pass", true);
        user.QuestionSet set = new user.QuestionSet(1, "OldName", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new user.QuestionSet[]{set});

            boolean result = QuestionTracker.updateQuestionSetMetadata(
                    1,
                    creator,
                    null,
                    java.util.List.of("tag1", "tag2")
            );

            assertTrue(result);
            assertEquals("OldName", set.getName());
            assertEquals(java.util.List.of("tag1", "tag2"), set.getTags());
        }
    }

    @Test
    void testJoinClassReturnsFalseForNullStudent() {
        boolean result = QuestionTracker.joinClass(null, "1234");

        assertFalse(result);
    }

    @Test
    void testJoinClassReturnsFalseForBadCode() {
        User student = new User(1, "student", "pass", false);
        user.Classroom classroom = new user.Classroom("TestClass", "1234", new User(99, "teacher", "pass", true));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[]{classroom});

            boolean result = QuestionTracker.joinClass(student, "9999");

            assertFalse(result);
            assertFalse(classroom.getStudents().contains(student));
        }
    }

    @Test
    void testJoinClassReturnsFalseIfAlreadyJoined() {
        User student = new User(1, "student", "pass", false);
        user.Classroom classroom = new user.Classroom("TestClass", "1234", new User(99, "teacher", "pass", true));
        classroom.addStudent(student);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[]{classroom});

            boolean result = QuestionTracker.joinClass(student, "1234");

            assertFalse(result);
        }
    }

    @Test
    void testCreateClassReturnsNullForDuplicateCode() {
        User teacher = new User(1, "teacher", "pass", true);
        user.Classroom existing = new user.Classroom("Math", "1234", teacher);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[]{existing});

            user.Classroom result = QuestionTracker.createClass("Science", "1234", teacher);

            assertNull(result);
        }
    }

    @Test
    void testCreateQuestionSetSessionSuccess() {
        User user = new User(1, "student", "pass", false);
        user.QuestionSet set = new user.QuestionSet(7, "Session Set", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> QuestionTracker.getQuestionSetById(7)).thenReturn(set);

            model.SetSession session = QuestionTracker.createQuestionSetSession(7, user);

            assertNotNull(session);
            assertEquals(7, session.getSetId());
            assertFalse(session.getIsStudySet());
        }
    }

    @Test
    void testAssignStudySetToClassReturnsFalseWhenClassMissing() {
        user.Classroom classroom = new user.Classroom("Math", "1234", new User(1, "teacher", "pass", true));

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[]{classroom});

            boolean result = QuestionTracker.assignStudySetToClass("Science", 1);

            assertFalse(result);
        }
    }

    @Test
    void testAssignStudySetToClassAddsRuntimeStudySetWhenFound() {
        user.User teacher = new user.User(1, "teacher", "pass", true);
        user.Classroom classroom = new user.Classroom("Math", "1234", teacher);
        teacher.StudySet studySet = new teacher.StudySet(1, "Algebra", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new user.Classroom[]{classroom});

            try (MockedStatic<model.StudySetMaker> mockedStudySetMaker = mockStatic(model.StudySetMaker.class)) {
                mockedStudySetMaker.when(() -> model.StudySetMaker.getSetById(1)).thenReturn(studySet);

                boolean result = QuestionTracker.assignStudySetToClass("Math", 1);

                assertTrue(result);
                assertTrue(classroom.getAssignedStudySetIds().contains(1));
                assertTrue(classroom.getAssignedStudySets().contains(studySet));
            }
        }
    }

    private void invokeSaveQuestionSets(user.QuestionSet[] sets) throws Exception {
        Method method = QuestionTracker.class.getDeclaredMethod("saveQuestionSets", user.QuestionSet[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) sets);
    }

    private void invokeSaveUsers(User[] users) throws Exception {
        Method method = QuestionTracker.class.getDeclaredMethod("saveUsers", User[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) users);
    }

    private void invokeSaveClasses(user.Classroom[] classes) throws Exception {
        Method method = QuestionTracker.class.getDeclaredMethod("saveClasses", user.Classroom[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) classes);
    }

    @Test
    void testGetQuestionSetsReturnsEmptyWhenCentralFileMissingAndOldDirMissing() throws IOException {
        Files.deleteIfExists(Path.of("src", "main", "questionSets.json"));
        Path oldDir = Path.of("src", "main", "questionSets");
        if (Files.exists(oldDir)) {
            try (var walk = Files.walk(oldDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                        });
            }
        }

        QuestionSet[] result = QuestionTracker.getQuestionSets();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetQuestionSetsReadsCentralizedFile() throws IOException {
        Path file = Path.of("src", "main", "questionSets.json");
        Files.createDirectories(file.getParent());

        String json = """
        [
          {
            "id": 11,
            "name": "Central Set",
            "creator": "teacher1",
            "questions": [],
            "tags": []
          }
        ]
        """;

        Files.writeString(file, json);

        QuestionSet[] result = QuestionTracker.getQuestionSets();

        assertEquals(1, result.length);
        assertEquals(11, result[0].getId());
        assertEquals("Central Set", result[0].getName());
    }

    @Test
    void testGetQuestionSetsReturnsEmptyWhenCentralizedFileMalformed() throws IOException {
        Path file = Path.of("src", "main", "questionSets.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{ not valid json }");

        QuestionSet[] result = QuestionTracker.getQuestionSets();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetClassesReturnsEmptyWhenMalformedJson() throws IOException {
        Path file = Path.of("src", "main", "classes.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{ bad json }");

        Classroom[] result = QuestionTracker.getClasses();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetClassesReadsSavedFile() throws Exception {
        User teacher = new User(1, "teacher", "pass", true);
        Classroom classroom = new Classroom("Physics", "7777", teacher);

        invokeSaveClasses(new Classroom[]{classroom});

        Classroom[] result = QuestionTracker.getClasses();

        assertEquals(1, result.length);
        assertEquals("Physics", result[0].getName());
        assertEquals("7777", result[0].getCode());
    }

    @Test
    void testCreateClassReturnsNullForNullTeacher() {
        Classroom result = QuestionTracker.createClass("Math", "1234", null);

        assertNull(result);
    }

    @Test
    void testGetClassByNameSkipsNullAndNullNameEntries() {
        Classroom good = new Classroom("Biology", "2222", new User(1, "teacher", "pass", true));
        Classroom unnamed = new Classroom();
        unnamed.setName(null);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getClasses).thenReturn(new Classroom[]{null, unnamed, good});

            Classroom result = QuestionTracker.getClassByName("Biology");

            assertNotNull(result);
            assertEquals("Biology", result.getName());
        }
    }

    @Test
    void testCreateQuestionSetSkipsNullEntriesWhenFindingNextId() {
        User creator = new User(10, "teacherA", "pass", true);
        QuestionSet existing = new QuestionSet(4, "Existing", "teacherA");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new QuestionSet[]{null, existing});
            mocked.when(() -> QuestionTracker.saveUser(any(User.class))).thenAnswer(invocation -> null);

            QuestionSet created = QuestionTracker.createQuestionSet("Brand New", creator);

            assertNotNull(created);
            assertEquals(5, created.getId());
            assertTrue(creator.getQuestionSetIds().contains(5));
        }
    }

    @Test
    void testGetQuestionSetByIdReturnsNullWhenArrayIsNull() {
        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(null);

            QuestionSet result = QuestionTracker.getQuestionSetById(1);

            assertNull(result);
        }
    }

    @Test
    void testGetQuestionSetByIdSkipsNullEntries() {
        QuestionSet set = new QuestionSet(3, "Target", "teacher");

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(QuestionTracker::getQuestionSets).thenReturn(new QuestionSet[]{null, set});

            QuestionSet result = QuestionTracker.getQuestionSetById(3);

            assertNotNull(result);
            assertEquals("Target", result.getName());
        }
    }

    @Test
    void testCreateQuestionSetSessionReturnsNullWhenSetMissing() {
        User user = new User(1, "student", "pass", false);

        try (MockedStatic<QuestionTracker> mocked = mockStatic(QuestionTracker.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> QuestionTracker.getQuestionSetById(77)).thenReturn(null);

            model.SetSession session = QuestionTracker.createQuestionSetSession(77, user);

            assertNull(session);
        }
    }
}