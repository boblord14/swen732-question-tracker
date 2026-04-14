package model;
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
            return mapper.readValue(new File("src/main/resources/users.json"), User[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return new User[0];
        }
    }

    private static void saveUsers(User[] users){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/users.json"), users);
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
        int newId = maxId + 1;
        User newUser = new User(newId, username, password, isTeacher);

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

    public static Classroom getClassByName(String name){
        Classroom[] classes = getClasses();
        for (Classroom c : classes){
            if (c != null && c.getName() != null && c.getName().equals(name)){
                return c;
            }
        }
        return null;
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
            File dir = new File("src/main/questionSets");
            if (!dir.exists() || !dir.isDirectory()) return new QuestionSet[0];
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files == null || files.length == 0) return new QuestionSet[0];
            List<QuestionSet> out = new java.util.ArrayList<>();
            for (File f : files) {
                try {
                    QuestionSet s = mapper.readValue(f, QuestionSet.class);
                    out.add(s);
                } catch (Exception ex) {
                    // skip malformed file but continue
                    ex.printStackTrace();
                }
            }
            return out.toArray(new QuestionSet[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new QuestionSet[0];
        }
    }

    private static void saveQuestionSets(QuestionSet[] sets){
        ObjectMapper mapper = new ObjectMapper();
        try {
            File dir = new File("src/main/questionSets");
            if (!dir.exists()) dir.mkdirs();
            for (QuestionSet s : sets) {
                if (s == null) continue;
                File out = new File(dir, "set_" + s.getId() + ".json");
                try {
                    mapper.writerWithDefaultPrettyPrinter().writeValue(out, s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update a single user entry in users.json (by id).
     */
    public static void saveUser(User user) {
        if (user == null) return;
        User[] users = getUsers();
        boolean found = false;
        for (int i = 0; i < users.length; i++){
            if (users[i] != null && users[i].getId() == user.getId()){
                users[i] = user;
                found = true;
                break;
            }
        }
        if (!found){
            // append
            User[] updated = new User[users.length + 1];
            System.arraycopy(users, 0, updated, 0, users.length);
            updated[users.length] = user;
            users = updated;
        }
        saveUsers(users);
    }

    /**
     * Create a new QuestionSet (study material). Only 'question creators' (teachers) may create.
     */
    public static QuestionSet createQuestionSet(String name, User creator){

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

    /**
     * Create an interactive session for a question set so a user can go through it question-by-question.
     */
    public static QuestionSetSession createQuestionSetSession(int setId, User user) {
        QuestionSet[] sets = getQuestionSets();
        for (QuestionSet s : sets) {
            if (s != null && s.getId() == setId) {
                return new QuestionSetSession(s, user);
            }
        }
        return null;
    }


}
