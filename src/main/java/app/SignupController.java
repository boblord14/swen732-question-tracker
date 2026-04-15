package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import user.User;
import model.questionTracker;

import java.util.Arrays;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private User[] users;

    @FXML
    public void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        users = questionTracker.getUsers();
        User found = Arrays.stream(users)
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (found != null) {
            errorLabel.setText("This username already exists");
            return;
        } else{
            questionTracker.signUp(username, password, false);
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

    public void loadSignupScreen(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignupView.fxml"));
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
