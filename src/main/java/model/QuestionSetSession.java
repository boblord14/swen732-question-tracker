package model;
import user.Question;
import user.QuestionSet;
import user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session for going through a QuestionSet question-by-question.
 * Keeps per-session results (question id -> correct boolean) and logs wrong-question tags into the user's record.
 */
public class QuestionSetSession {
    private final QuestionSet set;
    private final User user;
    private int index = 0;
    private Question lastQuestion = null;
    private final Map<Integer, Boolean> results = new HashMap<>();

    public QuestionSetSession(QuestionSet set, User user) {
        this.set = set;
        this.user = user;
    }

    public boolean hasNext() {
        List<Question> qs = set.getQuestions();
        return index < qs.size();
    }

    /**
     * Move to the next question and return it (or null if none left).
     */
    public Question nextQuestion() {
        List<Question> qs = set.getQuestions();
        if (index >= qs.size()) return null;
        lastQuestion = qs.get(index++);
        return lastQuestion;
    }

    /**
     * Reveal the answer for the last returned question.
     */
    public String revealAnswer() {
        if (lastQuestion == null) return null;
        return lastQuestion.getAnswer();
    }

    /**
     * Mark the last returned question as correct/incorrect. If incorrect, log its tags into the user's wrong-question list and persist the user.
     */
    public void markLastAnswer(boolean correct) {
        if (lastQuestion == null) return;
        results.put(lastQuestion.getId(), correct);
        if (!correct && user != null) {
            List<String> tags = lastQuestion.getTags();
            if (tags != null) {
                user.addWrongQuestion(tags);
                // persist user
                questionTracker.saveUser(user);
            }
        }
        // clear lastQuestion so it's not re-marked
        lastQuestion = null;
    }

    public int getTotalQuestions() { return set.getQuestions().size(); }

    public int getCurrentIndex() { return index; }

    public Map<Integer, Boolean> getResults() { return new HashMap<>(results); }
}
