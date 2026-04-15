package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.questionTracker;
import user.User;

import java.util.Arrays;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private User[] users;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        users = questionTracker.getUsers();
        User found = Arrays.stream(users)
                .filter(u -> u.getUsername().equals(username) &&
                            u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (found == null) {
            errorLabel.setText("Invalid username or password");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setUser(found);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLoginScreen(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            ClassListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
