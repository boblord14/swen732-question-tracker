package user;

import java.util.*;
import java.util.stream.Collectors;

public class User {
    private static final int QUESTION_STORAGE_MAX = 100;

    private int id;
    private String username;
    private String password;
    private boolean isTeacher;
    private final Deque<List<String>> wrongQuestionList = new LinkedList<>();

    public int getId() { return id;}
    public String getUsername() { return username;}
    public String getPassword() { return password;}
    public boolean getIsTeacher() { return isTeacher;}

    public void setId(int id) { this.id = id;}
    public void setUsername(String username) { this.username = username;}
    public void setPassword(String password) { this.password = password;}
    public void setIsTeacher(boolean isTeacher) { this.isTeacher = isTeacher;}

    /**
     * Add a question's subject tags to the last x wrong question lists, removes oldest if list reaches capacity
     * @param wrongQuestion list of question tags to add
     */
    public void addWrongQuestion(List<String> wrongQuestion) {
        //only tracking the last ~100 or so questions
        if(wrongQuestionList.size() >= QUESTION_STORAGE_MAX) {
            wrongQuestionList.pollFirst();
        }
        wrongQuestionList.addLast(wrongQuestion);
    }

    /**
     * Return wrong question tag data as an arraylist
     * @return question data as arraylist rather than dequeue
     */
    public List<List<String>> getWrongQuestionData() {
        return new ArrayList<>(wrongQuestionList);
    }

    @Override
    public boolean equals(Object o) {
        return this.id == ((User) o).getId() && o.getClass().getName().equals(this.getClass().getName());
    }
}