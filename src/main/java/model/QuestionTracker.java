package model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import user.User;
import user.Classroom;
import user.Question;
import user.QuestionSet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


public class QuestionTracker {

    static Logger logger = Logger.getLogger(QuestionTracker.class.getName());

    public QuestionTracker() {}

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
                logger.info("Username already exists.");
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

        logger.info(() -> "User registered successfully: " + username);
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
            logger.info("Only teachers can create classes.");
            return null;
        }

        Classroom[] classes = getClasses();
        // ensure code uniqueness
        for (Classroom c : classes){
            if (c != null && c.getCode() != null && c.getCode().equals(code)){
                logger.info("A class with this code already exists.");
                return null;
            }
        }

        Classroom newClass = new Classroom(name, code, teacher);

        Classroom[] updated = new Classroom[classes.length + 1];
        System.arraycopy(classes, 0, updated, 0, classes.length);
        updated[classes.length] = newClass;

        saveClasses(updated);
        // Persist teacher update (their classrooms list was modified in Classroom constructor)
        try {
            if (teacher != null) saveUser(teacher);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info(() -> "Class created: " + name + " (" + code + ") by " + teacher.getUsername());
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
                    logger.info(() -> student.getUsername() + " joined class " + c.getName());
                } else {
                    logger.info(() -> student.getUsername() + " is already in class " + c.getName());
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
            File central = new File("src/main/questionSets.json");
            if (central.exists()){
                // read centralized file
                List<QuestionSet> list = mapper.readValue(central, new TypeReference<List<QuestionSet>>(){});
                return list.toArray(new QuestionSet[0]);
            }

            // Backwards-compatibility: if centralized file missing but old per-file dir exists, migrate
            File dir = new File("src/main/questionSets");
            if (dir.exists() && dir.isDirectory()){
                return backwardsCompatabilityGetQuestionSets(dir);
            }

            return new QuestionSet[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new QuestionSet[0];
        }
    }

    private static QuestionSet[] backwardsCompatabilityGetQuestionSets(File dir){
        ObjectMapper mapper = new ObjectMapper();

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
        // persist centralized file for future runs
        QuestionSet[] arr = out.toArray(new QuestionSet[0]);
        saveQuestionSets(arr);
        // attempt to remove old per-set files and directory to avoid duplicate storage
        try {
            for (File old : files) {
                if(!old.delete()){
                    throw new IOException("Could not delete file: " + old.getAbsolutePath());
                }
            }
            File[] remaining = dir.listFiles();
            if ((remaining == null || remaining.length == 0) && !dir.delete()){
                throw new IOException("Could not delete directory: " + dir.getAbsolutePath());
            }
        } catch (Exception cleanupEx) {
            // don't fail migration on cleanup errors
            cleanupEx.printStackTrace();
        }
        return arr;
    }

    private static void saveQuestionSets(QuestionSet[] sets){
        ObjectMapper mapper = new ObjectMapper();
        try {
            File out = new File("src/main/questionSets.json");
            // convert to list for cleaner JSON array
            List<QuestionSet> list = new java.util.ArrayList<>();
            if (sets != null) {
                for (QuestionSet s : sets) if (s != null) list.add(s);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, list);
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

        QuestionSet newSet = new QuestionSet(maxId + 1, name, creator.getUsername());
        QuestionSet[] updated = new QuestionSet[sets.length + 1];
        System.arraycopy(sets, 0, updated, 0, sets.length);
        updated[sets.length] = newSet;
        saveQuestionSets(updated);
        // Associate created set with creator and persist
        try {
            if (creator != null) {
                creator.addQuestionSetId(newSet.getId());
                saveUser(creator);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info(() -> "Question set created: " + name + " (id=" + newSet.getId() + ") by " + creator.getUsername());
        return newSet;
    }

    /**
     * Return question sets available to the given user (by membership in user's questionSetIds).
     */
    public static QuestionSet[] getQuestionSetsForUser(User user) {
        if (user == null) return new QuestionSet[0];
        QuestionSet[] all = getQuestionSets();
        java.util.List<QuestionSet> out = new java.util.ArrayList<>();
        java.util.List<Integer> ids = user.getQuestionSetIds();
        if (ids == null || ids.isEmpty()) return new QuestionSet[0];
        for (QuestionSet s : all) {
            if (s == null) continue;
            if (ids.contains(s.getId())) out.add(s);
        }
        return out.toArray(new QuestionSet[0]);
    }

    /**
     * Return a user by id from persistent storage, or null if not found.
     */
    public static User getUserById(int id) {
        User[] users = getUsers();
        for (User u : users) {
            if (u != null && u.getId() == id) return u;
        }
        return null;
    }

    /**
     * Return a QuestionSet by id from the centralized questionSets.json storage.
     */
    public static user.QuestionSet getQuestionSetById(int id) {
        QuestionSet[] sets = getQuestionSets();
        if (sets == null) return null;
        for (QuestionSet s : sets) {
            if (s != null && s.getId() == id) return s;
        }
        return null;
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
     * Add a question to a question set. Returns true if added.
     */
    public static boolean removeQuestionFromSet(int setId, int questionId){
        QuestionSet[] sets = getQuestionSets();
        boolean changed = false;
        for (QuestionSet s : sets){
            if (s != null && s.getId() == setId){
                s.removeQuestionById(questionId);
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
            if (!(s != null && s.getId() == setId)) {
                continue;
            }
            Question q = s.findQuestionById(questionId);
            if (q == null) {
                continue;
            }
            if (newText != null) q.setText(newText);
            if (newAnswer != null) q.setAnswer(newAnswer);
            if (newTags != null) q.setTags(newTags);
            changed = true;
            break;
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
            if (!(s != null && s.getId() == setId)){
                continue;
            }
            if (s.getCreator() == null || !Objects.equals(s.getCreator(), requester.getUsername())){
                logger.info("Only the creator may edit this question set.");
                return false;
            }
            if (newName != null) s.setName(newName);
            if (newTags != null) s.setTags(newTags);
            changed = true;
            break;

        }
        if (changed) saveQuestionSets(sets);
        return changed;
    }

    /**
     * Create an interactive session for a question set so a user can go through it question-by-question.
     */
    public static SetSession createQuestionSetSession(int id, User user) {
        QuestionSet set = getQuestionSetById(id);
        if (set == null) return null;
        return new SetSession(set, user, false);  // false = flashcard mode
    }

    /**
     * Assign an existing study set (question set) to a class by name.
     * This updates the classroom's assignedStudySetIds and persists classes.json.
     */
    public static boolean assignStudySetToClass(String className, int setId) {
        Classroom[] classes = getClasses();
        boolean changed = false;
        for (int i = 0; i < classes.length; i++){
            Classroom c = classes[i];
            if (c != null && c.getName() != null && c.getName().equals(className)){
                // add id to classroom and try to load StudySet for runtime list
                c.addAssignedStudySetId(setId);
                teacher.StudySet set = model.studySetMaker.getSetById(setId);
                if (set != null) c.addStudySet(set);
                changed = true;
                break;
            }
        }

        if (changed) {
            // reuse private saveClasses method
            saveClasses(classes);
        }

        return changed;
    }


}
