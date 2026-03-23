import com.fasterxml.jackson.databind.ObjectMapper;

import user.User;

import java.io.File;

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


}
