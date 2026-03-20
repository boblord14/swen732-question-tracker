import static org.junit.jupiter.api.Assertions.*;

class questionTrackerTest {

    questionTracker qt = new questionTracker();

    @org.junit.jupiter.api.Test
    void genericAddTest() {
        assertEquals(4, qt.genericAddTest(2, 2));
        assertNotEquals(4, qt.genericAddTest(2, 3));
    }
}