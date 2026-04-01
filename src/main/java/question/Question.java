package question;

import java.util.ArrayList;

public class Question {
    private int id;
    private String subject;
    private ArrayList<String> tags;
    private String question;
    private String answer;

    public int getId() { return id;}
    public String getSubject() { return subject;}
    public ArrayList<String> getTags() { return tags;}
    public String getQuestion() { return question;}
    public String getAnswer() { return answer;}

    public void setId(int id) { this.id = id;}
    public void setSubject(String subject) { this.subject = subject;}
    public void setTag(ArrayList<String> tags) { 
        this.tags = tags;
    }
    public void setQuestion(String question) { this.question = question;}
    public void setAnswer(String answer) {this.answer = answer;}
}
