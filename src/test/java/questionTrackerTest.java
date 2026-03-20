import static org.junit.jupiter.api.Assertions.*;

class questionTrackerTest {

    @org.junit.jupiter.api.Test
    void genericAddTest() {
        assertEquals(4, questionTracker.genericAddTest(2, 2));
        assertNotEquals(4, questionTracker.genericAddTest(2, 3));
    }
}