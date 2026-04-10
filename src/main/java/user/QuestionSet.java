package user;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named collection of questions (study material) created by a user.
 */
public class QuestionSet {
    private int id;
    private String name;
    private User creator;
    private List<Question> questions;
    private List<String> tags;

    public QuestionSet() {
        this.questions = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public QuestionSet(int id, String name, User creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.questions = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public User getCreator() { return creator; }
    public List<Question> getQuestions() { return questions; }
    public List<String> getTags() { return tags; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCreator(User creator) { this.creator = creator; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // Helpers
    public boolean addQuestion(Question q) {
        if (q == null) return false;
        return this.questions.add(q);
    }

    public boolean removeQuestionById(int questionId) {
        return this.questions.removeIf(q -> q.getId() == questionId);
    }

    public Question findQuestionById(int questionId) {
        for (Question q : questions) {
            if (q.getId() == questionId) return q;
        }
        return null;
    }

    public void addTag(String tag) {
        if (tag == null) return;
        if (!this.tags.contains(tag)) this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
}
