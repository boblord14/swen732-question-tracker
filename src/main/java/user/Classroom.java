package user;

import java.util.ArrayList;
import java.util.List;

/**
 * Classroom represents a class created by a teacher.
 * Assumption: Named Classroom to avoid conflict with java.lang.Class.
 */
public class Classroom {
    private String name;
    private String code;
    private User teacher; // single teacher
    private List<User> students;
    private List<Question> questions; // placeholder Question objects

    public Classroom() {
        this.students = new ArrayList<>();
        this.questions = new ArrayList<>();
    }

    public Classroom(String name, String code, User teacher) {
        this.name = name;
        this.code = code;
        this.teacher = teacher;
        this.students = new ArrayList<>();
        this.questions = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public User getTeacher() { return teacher; }
    public List<User> getStudents() { return students; }
    public List<Question> getQuestions() { return questions; }

    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public void setStudents(List<User> students) { this.students = students; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    // helper methods
    public boolean addStudent(User student) {
        if (student == null) return false;
        if (students.contains(student)) return false;
        return students.add(student);
    }

    public boolean removeStudent(User student) {
        if (student == null) return false;
        return students.remove(student);
    }

    public boolean addQuestion(Question q) {
        if (q == null) return false;
        return questions.add(q);
    }

    /**
     * Check whether the provided code matches this classroom's code.
     * This method is null-safe and trims the input before comparison.
     * @param code the code to verify
     * @return true if the given code matches this classroom's code
     */
    public boolean checkCode(String code) {
        if (this.code == null) return false;
        if (code == null) return false;
        return this.code.equals(code.trim());
    }
}
