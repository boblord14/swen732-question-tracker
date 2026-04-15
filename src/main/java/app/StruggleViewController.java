package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.SearchService;
import model.SetSession;
import model.questionTracker;
import model.studySetMaker;
import teacher.StudySet;
import user.Question;
import user.QuestionSet;
import user.User;
import user.UserPrediction;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StruggleViewController {

    @FXML
    private Label titleLabel;
    @FXML
    private VBox struggleListVBox;
    @FXML
    private Button practiceButton;

    private User user;
    private String classroomName;
    private Map<String, Double> struggleVector;
    private boolean isTeacherMode;

    //practice quiz question number, 10 felt right
    private static final int PRACTICE_QUESTION_COUNT = 10;

    public void setDataTeacher(User teacher, String classroomName, List<User> students){
        this.user = teacher;
        this.classroomName = classroomName;
        this.isTeacherMode = true;

        titleLabel.setText(classroomName + ": Class Struggles");
        practiceButton.setText("Generate Struggle Set");

        List<User> updatedStudentData = new ArrayList<>();
        for(User student: students){
            User user = questionTracker.getUserById(student.getId());
            if (user != null) {
                user = student;
            }
            updatedStudentData.add(user);
        }

        if (updatedStudentData.isEmpty()) {
            struggleVector = new HashMap<>();
            titleLabel.setText(classroomName + ": No Students Enrolled");
            practiceButton.setDisable(true);
        } else {
            struggleVector = UserPrediction.generateUserStruggleVector(updatedStudentData);
            buildStruggleList();
        }
    }

    public void setDataStudent(User student){
        this.user = questionTracker.getUserById(student.getId());
        if (user != null) { this.user = student; }

        this.isTeacherMode = false;

        titleLabel.setText(student.getUsername() + ": Your Struggles");
        practiceButton.setText("Practice Struggles");

        UserPrediction up = new UserPrediction(student);
        struggleVector = up.generateUserStruggleVector();

        if (struggleVector.isEmpty()) {
            Label none = new Label("No struggle data, go get some questions wrong");
            none.setStyle("-fx-font-size: 13px;");
            struggleListVBox.getChildren().add(none);
            practiceButton.setDisable(true);
        } else {
            buildStruggleList();
        }
    }

    private void buildStruggleList(){
        struggleListVBox.getChildren().clear();

        //funny function that ditches super low scores and sorts by score as well
        List<Map.Entry<String, Double>> sorted = struggleVector.entrySet().stream()
                .filter(e -> e.getValue() > 0.01)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            struggleListVBox.getChildren().add(new Label("No major struggles found, go get some more questions wrong"));
            practiceButton.setDisable(true);
            return;
        }

        double max = sorted.get(0).getValue(); // to make scores make sense

        //iterate over tags and scores
        for (Map.Entry<String, Double> entry : sorted){
            String tag = entry.getKey();
            double score = entry.getValue();
            double normalized = max > 0 ? score / max : 0; //makes sense now

            HBox row = new HBox(12);
            row.setStyle("-fx-padding: 4 0;");

            Label tagLabel = new Label(tag);
            tagLabel.setMinWidth(140);
            tagLabel.setStyle("-fx-font-size: 13px;");

            Pane bar = new Pane();
            bar.setPrefHeight(14);
            bar.setPrefWidth(normalized * 220);
            bar.setMaxWidth(normalized * 220);
            bar.setStyle("-fx-background-color: #4a90d9; -fx-background-radius: 3;");

            Label pctLabel = new Label(String.format("%.0f%%", score * 100));
            pctLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            row.getChildren().addAll(tagLabel, bar, pctLabel);
            struggleListVBox.getChildren().add(row);
        }
    }

    @FXML
    private void handlePractice(ActionEvent event) {
        if (isTeacherMode) {
            generateTeacherStudySet();
        } else {
            launchStudentPracticeSession();
        }
    }

    private void launchStudentPracticeSession() {
        SearchService search = new SearchService();
        List<Question> recommended = search.recommendTopQuestionsForUser(user, PRACTICE_QUESTION_COUNT);

        if (recommended.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No tagged questions found", ButtonType.OK).showAndWait();
            return;
        }

        //not persisting this
        QuestionSet practiceSet = new QuestionSet(-1, "Struggle Practice", user.getUsername());
        practiceSet.setQuestions(recommended);

        SetSession session = new SetSession(practiceSet, user, false);

        goToSession(session, () -> goToOrigin());
    }

    private void generateTeacherStudySet() {
        if(struggleVector.isEmpty()){ return; }

        //funny name generation function to get the number of struggle sets generated already for a given class
        StudySet[] existing = studySetMaker.getAllSets();
        long count = Arrays.stream(existing)
                .filter(s -> s != null && user.getUsername().equals(s.getCreator())
                        && s.getName() != null && s.getName().startsWith(classroomName + " Struggle Set"))
                .count();

        String setName = classroomName + " Struggle Set #" + (count + 1);

        SearchService search = new SearchService();
        List<Question> recommended = search.recommendTopQuestionsGivenVector(struggleVector, PRACTICE_QUESTION_COUNT);

        if (recommended.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No tagged questions found", ButtonType.OK).showAndWait();
            return;
        }

        ArrayList<user.Question> qList = new ArrayList<>(recommended);
        StudySet saved = studySetMaker.createSet(qList, user, setName, "");

        new Alert(Alert.AlertType.INFORMATION,
                "\"" + setName + "\" created and saved to your account. You can assign it to your class in the class window",
                ButtonType.OK).showAndWait();

        goToOrigin();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goToOrigin();
    }

    private void goToOrigin() {
        try {
            if(isTeacherMode) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassView.fxml"));
                Parent root = loader.load();
                ClassViewController ctrl = loader.getController();
                ctrl.setData(classroomName, user);
                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
            }else{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) titleLabel.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
                stage.setScene(scene);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToSession(SetSession session, Runnable onFinish) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetSessionView.fxml"));
            Parent root = loader.load();
            SetSessionController ctrl = loader.getController();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            ctrl.setSession(session, user, onFinish);
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
