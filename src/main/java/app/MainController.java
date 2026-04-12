package app;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import user.Classroom;
import user.User;

public class MainController {

    @FXML
    public void handleLoad() {
        System.out.println("Button clicked!");

        // 🔥 Call your existing backend logic here
        // Example:
        // ClassroomService.getAllClasses();
    }

}