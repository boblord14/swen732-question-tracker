package model;
import user.Question;
import user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session for going through a QuestionSet/StudySet question-by-question.
 * Keeps per-session results (question id -> correct boolean) and logs wrong-question tags into the user's record.
 */
public class SetSession {
    private final BaseSet set;
    private final User user;
    private int index = 0;
    private Question currentQuestion = null;
    private final Map<Integer, Boolean> results = new HashMap<>();
    boolean isStudySet;

    public SetSession(BaseSet set, User user, Boolean isStudySet) {
        this.set = set;
        this.user = user;
        this.isStudySet = isStudySet;
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

        currentQuestion = qs.get(index++);
        return currentQuestion;
    }

    public int getTotalQuestions() { return set.getQuestions().size(); }

    public int getCurrentIndex() { return index; }

    public Map<Integer, Boolean> getResults() { return new HashMap<>(results); }

    public boolean getIsStudySet() {
        return isStudySet;
    }

    public int getSetId() { return set.getId(); }

    public boolean submitAnswer(String userAnswer) {
        if (currentQuestion == null || userAnswer == null) return false;

        boolean correct;

        if ("FLASHCARD_CORRECT".equals(userAnswer)) {
            correct = true;
        } else if ("FLASHCARD_WRONG".equals(userAnswer)) {
            correct = false;
        } else {
            correct = currentQuestion.getAnswer()
                    .equalsIgnoreCase(userAnswer);
        }

        results.put(currentQuestion.getId(), correct);

        if (!correct && user != null) {
            List<String> tags = currentQuestion.getTags();
            if (tags != null) {
                user.addWrongQuestion(tags);
                questionTracker.saveUser(user);
            }
        }

        return correct;
    }
}
