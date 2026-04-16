package user;

import java.util.ArrayList;

import java.util.List;

public class Question {
    private int id;
    private String text;
    private String answer;
    private List<String> tags;

    public Question() {}

    public Question(int id, String text) {
        this.id = id;
        this.text = text;
        this.answer = "";
        this.tags = new ArrayList<>();
    }

    public Question(int id, String text, String answer) {
        this.id = id;
        this.text = text;
        this.answer = answer;
        this.tags = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String getAnswer() { return answer; }

    public void setId(int id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setAnswer(String answer) { this.answer = answer; }

    public void addTag(String tag) { this.tags.add(tag); }
    public List<String> getTags() { return tags; }
    public void removeTag(String tag) { this.tags.remove(tag); }

    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", answer='" + answer + '\'' +
                ", tags=" + tags +
                '}';
    }

    }
