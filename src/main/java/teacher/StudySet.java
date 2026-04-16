package teacher;

import java.util.ArrayList;
import java.util.List;

import model.BaseSet;
import user.Question;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StudySet implements BaseSet {
    private int id;
    private String name;
    private String creator;
    private String subject;
    private List<String> tags;
    @JsonProperty("questionSet")
    private List<Question> questionSet;

    public StudySet() {}

    public StudySet(int id, String name, String creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.questionSet = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // BaseSet impl
    @Override public int getId() { return id; }
    @Override public String getName() { return name; }
    @JsonProperty("questionSet")
    public List<Question> getQuestions() {
        return questionSet;
    }
    @Override public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
    @Override public String getCreator() { return creator; }

    public String getSubject() { return subject; }

    public void setCreator(String creator){
        this.creator = creator;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setTags(List<String> tags){
        this.tags = tags;
    }

    public void setQuestionSet(List<Question> questionSet){
        this.questionSet = questionSet;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSubject(String subject) { this.subject = subject; }

    // Helpers
    public boolean addQuestion(Question q) {
        if (q == null) return false;
        return this.questionSet.add(q);
    }

    public boolean removeQuestionById(int questionId) {
        return this.questionSet.removeIf(q -> q.getId() == questionId);
    }
}
