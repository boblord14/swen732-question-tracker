package teacher;

import java.util.ArrayList;
import user.Question;

public class StudySet {
    private int id;
    private static int counter = 0;
    private String subject;
    private String creator;
    private String title;
    private ArrayList<String> tags;
    private ArrayList<Question> questionSet;

    public StudySet(){
        this.id = counter++;
    }

    public ArrayList<Question> getQuestionSet() { 
        return questionSet;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getCreator(){
        return creator;
    }

    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getSubject(){
        return subject;
    }

    public void setCreator(String creator){
        this.creator = creator;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setTags(ArrayList<String> tags){
        this.tags = tags;
    }

    public void setQuestionSet(ArrayList<Question> questionSet){
        this.questionSet = questionSet;
    }

    public void setSubject(String subject){
        this.subject = subject;
    }

    public void setTitle(String title){
        this.title = title;
    }
}
