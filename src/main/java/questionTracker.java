import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import user.User;
import user.Classroom;
import user.Question;
import user.QuestionSet;

import java.io.File;
import java.util.List;

public class questionTracker {
    public static void main(String[] args) {
        User user = logIn("admin", "admin123");
        if (user != null) {
            System.out.println("Login successful for user: " + user.getUsername());
        } else {
            System.out.println("Login failed.");
        }
    }

    public int genericAddTest(int a, int b){
        return a + b;
    }

    public static User logIn(String username, String password){
        User[] users = getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static User[] getUsers(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("src/main/users.json"), User[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return new User[0];
        }
    }

    private static void saveUsers(User[] users){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/users.json"), users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User signUp(String username, String password, boolean isTeacher){
        User[] users = getUsers();
        
        // Check if username already exists
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("Username already exists.");
                return null;
            }
        }
        
        // Find next available ID
        int maxId = 0;
        for (User user : users) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        
        // Create new user
        User newUser = new User();
        newUser.setId(maxId + 1);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setIsTeacher(isTeacher);
        
        // Add to users array
        User[] updatedUsers = new User[users.length + 1];
        System.arraycopy(users, 0, updatedUsers, 0, users.length);
        updatedUsers[users.length] = newUser;
        
        // Save to file
        saveUsers(updatedUsers);
        
        System.out.println("User registered successfully: " + username);
        return newUser;
    }

    // --- Classroom persistence and operations ---

    public static Classroom[] getClasses(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            File f = new File("src/main/classes.json");
            if (!f.exists()) {
                return new Classroom[0];
            }
            // read as list to avoid issues with empty files
            List<Classroom> list = mapper.readValue(f, new TypeReference<List<Classroom>>(){});
            return list.toArray(new Classroom[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new Classroom[0];
        }
    }

    private static void saveClasses(Classroom[] classes){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/classes.json"), classes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new classroom. Only users marked as teacher may create a class.
     * Ensures the class code is unique.
     */
    public static Classroom createClass(String name, String code, User teacher){
        if (teacher == null || !teacher.getIsTeacher()){
            System.out.println("Only teachers can create classes.");
            return null;
        }

        Classroom[] classes = getClasses();
        // ensure code uniqueness
        for (Classroom c : classes){
            if (c != null && c.getCode() != null && c.getCode().equals(code)){
                System.out.println("A class with this code already exists.");
                return null;
            }
        }

        Classroom newClass = new Classroom(name, code, teacher);

        Classroom[] updated = new Classroom[classes.length + 1];
        System.arraycopy(classes, 0, updated, 0, classes.length);
        updated[classes.length] = newClass;

        saveClasses(updated);
        System.out.println("Class created: " + name + " (" + code + ") by " + teacher.getUsername());
        return newClass;
    }

    /**
     * Student joins a class with the provided code. Returns true on success.
     */
    public static boolean joinClass(User student, String code){
        if (student == null) return false;
        Classroom[] classes = getClasses();
        boolean changed = false;
        for (Classroom c : classes){
            if (c != null && c.checkCode(code)){
                // add student
                if (c.addStudent(student)){
                    changed = true;
                    System.out.println(student.getUsername() + " joined class " + c.getName());
                } else {
                    System.out.println(student.getUsername() + " is already in class " + c.getName());
                }
                break; // assume codes are unique
            }
        }

        if (changed){
            saveClasses(classes);
        }

        return changed;
    }

    // --- QuestionSet (study materials) persistence and operations ---

    public static QuestionSet[] getQuestionSets(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            File f = new File("src/main/questionSets.json");
            if (!f.exists()) {
                return new QuestionSet[0];
            }
            List<QuestionSet> list = mapper.readValue(f, new TypeReference<List<QuestionSet>>(){});
            return list.toArray(new QuestionSet[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new QuestionSet[0];
        }
    }

    private static void saveQuestionSets(QuestionSet[] sets){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/questionSets.json"), sets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean userCanCreateQuestionSets(User user) {
        return user != null && user.getIsTeacher();
    }

    /**
     * Create a new QuestionSet (study material). Only 'question creators' (teachers) may create.
     */
    public static QuestionSet createQuestionSet(String name, User creator){
        if (!userCanCreateQuestionSets(creator)){
            System.out.println("Only question creators can create question sets.");
            return null;
        }

        QuestionSet[] sets = getQuestionSets();
        int maxId = 0;
        for (QuestionSet s : sets){
            if (s != null && s.getId() > maxId) maxId = s.getId();
        }

        QuestionSet newSet = new QuestionSet(maxId + 1, name, creator);
        QuestionSet[] updated = new QuestionSet[sets.length + 1];
        System.arraycopy(sets, 0, updated, 0, sets.length);
        updated[sets.length] = newSet;
        saveQuestionSets(updated);
        System.out.println("Question set created: " + name + " (id=" + newSet.getId() + ") by " + creator.getUsername());
        return newSet;
    }

    /**
     * Add a question to a question set. Returns true if added.
     */
    public static boolean addQuestionToSet(int setId, String questionText, String answer, List<String> tags){
        QuestionSet[] sets = getQuestionSets();
        boolean changed = false;
        for (QuestionSet s : sets){
            if (s != null && s.getId() == setId){
                // determine next question id within the set
                int maxQ = 0;
                for (Question q : s.getQuestions()){
                    if (q.getId() > maxQ) maxQ = q.getId();
                }
                Question q = new Question(maxQ + 1, questionText, answer);
                if (tags != null) q.setTags(tags);
                s.addQuestion(q);
                changed = true;
                break;
            }
        }

        if (changed) saveQuestionSets(sets);
        return changed;
    }

    /**
     * Edit an existing question inside a set. Returns true if modified.
     */
    public static boolean editQuestionInSet(int setId, int questionId, String newText, String newAnswer, List<String> newTags){
        QuestionSet[] sets = getQuestionSets();
        boolean changed = false;
        for (QuestionSet s : sets){
            if (s != null && s.getId() == setId){
                Question q = s.findQuestionById(questionId);
                if (q != null){
                    if (newText != null) q.setText(newText);
                    if (newAnswer != null) q.setAnswer(newAnswer);
                    if (newTags != null) q.setTags(newTags);
                    changed = true;
                }
                break;
            }
        }
        if (changed) saveQuestionSets(sets);
        return changed;
    }

    /**
     * Update metadata for a question set (name, tags). Only the creator may edit.
     */
    public static boolean updateQuestionSetMetadata(int setId, User requester, String newName, List<String> newTags){
        if (requester == null) return false;
        QuestionSet[] sets = getQuestionSets();
        boolean changed = false;
        for (QuestionSet s : sets){
            if (s != null && s.getId() == setId){
                if (s.getCreator() == null || s.getCreator().getId() != requester.getId()){
                    System.out.println("Only the creator may edit this question set.");
                    return false;
                }
                if (newName != null) s.setName(newName);
                if (newTags != null) s.setTags(newTags);
                changed = true;
                break;
            }
        }
        if (changed) saveQuestionSets(sets);
        return changed;
    }


}
