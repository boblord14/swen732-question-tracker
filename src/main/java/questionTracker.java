import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import user.User;
import user.Classroom;

import java.io.File;
import java.util.ArrayList;
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


}
