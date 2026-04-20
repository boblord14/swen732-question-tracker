package tests;

import org.junit.jupiter.api.Test;
import user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructorInitializesCollections() {
        User user = new User();

        assertNotNull(user.getWrongQuestionData());
        assertNotNull(user.getStudySetAvg());
        assertNotNull(user.getClassrooms());
        assertNotNull(user.getQuestionSetIds());

        assertTrue(user.getWrongQuestionData().isEmpty());
        assertTrue(user.getStudySetAvg().isEmpty());
        assertTrue(user.getClassrooms().isEmpty());
        assertTrue(user.getQuestionSetIds().isEmpty());
    }

    @Test
    void testParameterizedConstructorAndGetters() {
        User user = new User(7, "alice", "secret", true);

        assertEquals(7, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertTrue(user.getIsTeacher());
        assertNotNull(user.getStudySetAvg());
        assertNotNull(user.getClassrooms());
        assertNotNull(user.getQuestionSetIds());
    }

    @Test
    void testSetters() {
        User user = new User();

        Map<Integer, Double> scores = new HashMap<>();
        scores.put(1, 95.5);

        List<String> classrooms = new ArrayList<>();
        classrooms.add("Math");

        List<Integer> questionSetIds = new ArrayList<>();
        questionSetIds.add(10);

        user.setId(5);
        user.setUsername("bob");
        user.setPassword("pw");
        user.setIsTeacher(false);
        user.setStudySetAvg(scores);
        user.setClassrooms(classrooms);
        user.setQuestionSetIds(questionSetIds);

        assertEquals(5, user.getId());
        assertEquals("bob", user.getUsername());
        assertEquals("pw", user.getPassword());
        assertFalse(user.getIsTeacher());
        assertEquals(scores, user.getStudySetAvg());
        assertEquals(classrooms, user.getClassrooms());
        assertEquals(questionSetIds, user.getQuestionSetIds());
    }

    @Test
    void testAddQuestionSetIdAddsUniqueId() {
        User user = new User(1, "u", "p", false);

        user.addQuestionSetId(12);

        assertEquals(1, user.getQuestionSetIds().size());
        assertTrue(user.getQuestionSetIds().contains(12));
    }

    @Test
    void testAddQuestionSetIdDoesNotAddDuplicate() {
        User user = new User(1, "u", "p", false);

        user.addQuestionSetId(12);
        user.addQuestionSetId(12);

        assertEquals(1, user.getQuestionSetIds().size());
        assertEquals(12, user.getQuestionSetIds().get(0));
    }

    @Test
    void testAddQuestionSetIdInitializesListWhenNull() {
        User user = new User(1, "u", "p", false);
        user.setQuestionSetIds(null);

        user.addQuestionSetId(99);

        assertNotNull(user.getQuestionSetIds());
        assertEquals(1, user.getQuestionSetIds().size());
        assertTrue(user.getQuestionSetIds().contains(99));
    }

    @Test
    void testRemoveQuestionSetIdRemovesExistingId() {
        User user = new User(1, "u", "p", false);
        user.setQuestionSetIds(new ArrayList<>(List.of(1, 2, 3)));

        user.removeQuestionSetId(2);

        assertEquals(2, user.getQuestionSetIds().size());
        assertFalse(user.getQuestionSetIds().contains(2));
        assertTrue(user.getQuestionSetIds().contains(1));
        assertTrue(user.getQuestionSetIds().contains(3));
    }

    @Test
    void testRemoveQuestionSetIdDoesNothingWhenIdMissing() {
        User user = new User(1, "u", "p", false);
        user.setQuestionSetIds(new ArrayList<>(List.of(1, 2, 3)));

        user.removeQuestionSetId(999);

        assertEquals(3, user.getQuestionSetIds().size());
        assertEquals(List.of(1, 2, 3), user.getQuestionSetIds());
    }

    @Test
    void testRemoveQuestionSetIdDoesNothingWhenListNull() {
        User user = new User(1, "u", "p", false);
        user.setQuestionSetIds(null);

        user.removeQuestionSetId(5);

        assertNull(user.getQuestionSetIds());
    }

    @Test
    void testAddWrongQuestionStoresEntries() {
        User user = new User(1, "u", "p", false);

        List<String> tags = List.of("math", "algebra");
        user.addWrongQuestion(tags);

        List<List<String>> result = user.getWrongQuestionData();
        assertEquals(1, result.size());
        assertEquals(tags, result.get(0));
    }

    @Test
    void testAddWrongQuestionCapsAt100AndDropsOldest() {
        User user = new User(1, "u", "p", false);

        for (int i = 0; i < 105; i++) {
            user.addWrongQuestion(List.of("tag" + i));
        }

        List<List<String>> data = user.getWrongQuestionData();

        assertEquals(100, data.size());
        assertEquals(List.of("tag5"), data.get(0));
        assertEquals(List.of("tag104"), data.get(99));
    }

    @Test
    void testGetWrongQuestionDataReturnsCopy() {
        User user = new User(1, "u", "p", false);
        user.addWrongQuestion(List.of("tag1"));

        List<List<String>> copy = user.getWrongQuestionData();
        copy.clear();

        assertEquals(1, user.getWrongQuestionData().size());
    }

    @Test
    void testAddStudySetScore() {
        User user = new User(1, "u", "p", false);

        user.addStudySetScore(10, 88.25);

        assertEquals(1, user.getStudySetAvg().size());
        assertEquals(88.25, user.getStudySetAvg().get(10));
    }

    @Test
    void testEqualsSameReference() {
        User user = new User(1, "u", "p", false);

        assertEquals(user, user);
    }

    @Test
    void testEqualsSameIdDifferentFields() {
        User a = new User(1, "alice", "pw1", false);
        User b = new User(1, "bob", "pw2", true);

        assertEquals(a, b);
    }

    @Test
    void testEqualsDifferentId() {
        User a = new User(1, "alice", "pw1", false);
        User b = new User(2, "alice", "pw1", false);

        assertNotEquals(a, b);
    }

    @Test
    void testEqualsNull() {
        User user = new User(1, "u", "p", false);

        assertNotEquals(null, user);
    }

    @Test
    void testEqualsDifferentType() {
        User user = new User(1, "u", "p", false);

        assertNotEquals("not a user", user);
    }

    @Test
    void testHashCodeSameIdSameHash() {
        User a = new User(42, "alice", "pw1", false);
        User b = new User(42, "bob", "pw2", true);

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHashCodeDifferentIdDifferentHashLikely() {
        User a = new User(1, "a", "p", false);
        User b = new User(2, "a", "p", false);

        assertNotEquals(a.hashCode(), b.hashCode());
    }
}