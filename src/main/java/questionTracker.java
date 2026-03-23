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


}
