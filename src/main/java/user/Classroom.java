package user;

import java.util.*;
import teacher.StudySet;

/**
 * Classroom represents a class created by a teacher.
 * Assumption: Named Classroom to avoid conflict with java.lang.Class.
 */
public class Classroom {
    private String name;
    private String code;
    private User teacher; // single teacher
    private List<User> students;
    private List<StudySet> assignedStudySets;
    private List<Integer> assignedStudySetIds;

    public Classroom(String name, String code, User teacher) {
        this.name = name;
        this.code = code;
        this.teacher = teacher;
        this.students = new ArrayList<>();
        this.assignedStudySets = new ArrayList<>();
        this.assignedStudySetIds = new ArrayList<>();
        teacher.getClassrooms().add(this.name);
    }

    public Classroom() {
        this.students = new ArrayList<>();
        this.assignedStudySets = new ArrayList<>();
        this.assignedStudySetIds = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public User getTeacher() { return teacher; }
    public List<User> getStudents() { return students; }
    public List<StudySet> getAssignedStudySets() { return assignedStudySets; }
    public List<Integer> getAssignedStudySetIds() { return assignedStudySetIds; }

    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public void setStudents(List<User> students) { this.students = students; }
    public void setAssignedStudySets(List<StudySet> assignedStudySets) { this.assignedStudySets = assignedStudySets; }
    public void setAssignedStudySetIds(List<Integer> ids) { this.assignedStudySetIds = ids; }

    // helper methods
    public boolean addStudent(User student) {
        if (student == null) return false;
        if (students.contains(student)) return false;
        student.getClassrooms().add(this.name);
        return students.add(student);
    }

    public boolean removeStudent(User student) {
        if (student == null) return false;
        student.getClassrooms().remove(this.name);
        return students.remove(student);
    }

    public boolean addStudySet(StudySet s) {
        if (s == null) return false;
        return assignedStudySets.add(s);
    }

    public boolean addAssignedStudySetId(int id) {
        if (assignedStudySetIds == null) assignedStudySetIds = new ArrayList<>();
        if (assignedStudySetIds.contains(id)) return false;
        return assignedStudySetIds.add(id);
    }

    public List<Map<Integer, Double>> studentsSetScores() {
        List<Map<Integer, Double>> scores = new ArrayList<>();
        for (User student : students) {
            scores.add(student.getStudySetAvg());
        }
        return scores;
    }

    public List<Map<String, Double>> classStruggleVector() {
        List<Map<String, Double>> struggleVectors = new ArrayList<>();
        struggleVectors.add(UserPrediction.generateUserStruggleVector(this.students));
        for (User student : students) {
            UserPrediction up = new UserPrediction(student);
            struggleVectors.add(up.generateUserStruggleVector());
        }
        return struggleVectors;
    }

    public Map<String, Double> studentStruggleVector(int id) {
        User student = students.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
        if (student == null) return Collections.emptyMap();
        UserPrediction up = new UserPrediction(student);
        return up.generateUserStruggleVector();
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
