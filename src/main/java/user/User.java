package user;

import java.util.*;
import java.util.stream.Collectors;

public class User {
    private static final int QUESTION_STORAGE_MAX = 100;

    private int id;
    private String username;
    private String password;
    private boolean isTeacher;
    private Deque<List<String>> wrongQuestionList;
    private Map<Integer, Double> studySetAvg;
    private List<String> classrooms;
    private List<Integer> questionSetIds;

    public User(int id, String username, String password, boolean isTeacher) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isTeacher = isTeacher;   
        this.wrongQuestionList = new LinkedList<>();
        this.studySetAvg = new HashMap<>();
        this.classrooms = new ArrayList<>();
        this.questionSetIds = new ArrayList<>();
    }

    public User() {
        this.wrongQuestionList = new LinkedList<>();
        this.studySetAvg = new HashMap<>();
        this.classrooms = new ArrayList<>();
        this.questionSetIds = new ArrayList<>();
    }

    public int getId() { return id;}
    public String getUsername() { return username;}
    public String getPassword() { return password;}
    public boolean getIsTeacher() { return isTeacher;}
    public Map<Integer, Double> getStudySetAvg() { return studySetAvg;}
    public List<String> getClassrooms() { return classrooms;}
    public List<Integer> getQuestionSetIds() { return questionSetIds;}

    public void setId(int id) { this.id = id;}
    public void setUsername(String username) { this.username = username;}
    public void setPassword(String password) { this.password = password;}
    public void setIsTeacher(boolean isTeacher) { this.isTeacher = isTeacher;}
    public void setStudySetAvg(Map<Integer, Double> studySetAvg) { this.studySetAvg = studySetAvg;}
    public void setClassrooms(List<String> classrooms) { this.classrooms = classrooms;}
    public void setQuestionSetIds(List<Integer> questionSetIds) { this.questionSetIds = questionSetIds;}

    public void addQuestionSetId(int id) {
        if (this.questionSetIds == null) this.questionSetIds = new ArrayList<>();
        if (!this.questionSetIds.contains(id)) this.questionSetIds.add(id);
    }

    public void removeQuestionSetId(int id) {
        if (this.questionSetIds == null) return;
        this.questionSetIds.removeIf(i -> i == id);
    }

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

    public void addStudySetScore(int id, double score){
        this.studySetAvg.put(id, score);
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
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}