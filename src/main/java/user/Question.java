package user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Question {
    private int id;
    private String text;
    private List<String> tags;

    public Question() {}

    public Question(int id, String text) {
        this.id = id;
        this.text = text;
        this.tags = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getText() { return text; }

    public void setId(int id) { this.id = id; }
    public void setText(String text) { this.text = text; }

    public void addTag(String tag) { this.tags.add(tag); }
    public List<String> getTags() { return tags; }
    public void removeTag(String tag) { this.tags.remove(tag); }

    }
