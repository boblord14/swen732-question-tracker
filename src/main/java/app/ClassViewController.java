package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import model.BaseSet;
import model.SetSession;
import model.QuestionTracker;
import model.StudySetMaker;
import teacher.StudySet;
import user.Classroom;
import user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClassViewController {

    @FXML private Label classNameLabel;
    @FXML private Label classCodeLabel;
    @FXML private VBox studentListVBox;
    @FXML private VBox teacherSection;
    @FXML private VBox studySetListVBox;

    private Classroom classroom;
    private User user;
    private static final  String CSS_SHEET = "/styles/styles.css";

    public void setData(String classroomName, User user) {
        this.classroom = QuestionTracker.getClassByName(classroomName);
        this.user = user;

        loadClassData();
    }

    private void loadClassData() {
        if (classroom == null) return;

        classNameLabel.setText(classroom.getName());
        classCodeLabel.setText("Code: " + classroom.getCode());

        if (user != null && user.getIsTeacher()) {
            teacherSection.setVisible(true);
            loadStudents();
            loadStudySets();
        } else {
            teacherSection.setVisible(false);
            loadStudySets();
        }
    }

    private void loadStudents() {
        studentListVBox.getChildren().clear();

        for (User student : classroom.getStudents()) {
            Label studentLabel = new Label("• " + student.getUsername());
            studentLabel.setStyle("-fx-font-size: 14px;");
            studentListVBox.getChildren().add(studentLabel);
        }
    }

    private void loadStudySets() {
        studySetListVBox.getChildren().clear();

        // Prefer persisted IDs
        List<Integer> ids = classroom.getAssignedStudySetIds();

        if (user.getIsTeacher()) {
            loadTeacherStudySetView(ids);
        } else {
            loadStudentStudySetView(ids);
        }
    }

    private void buildGradesBox(VBox gradesBox, int id) {
        List<User> students = classroom.getStudents();
        if(students == null || students.isEmpty()){
            gradesBox.getChildren().add(new Label("No students enrolled"));
            return;
        }

        for(User student : students){
            //done since the student in the class and the student object itself can diverge at times
            User realStudentUser = QuestionTracker.getUserById(student.getId());
            if (realStudentUser == null) realStudentUser = student; //pls dont break k thx

            String name = realStudentUser.getUsername();
            Map<Integer, Double> scores = realStudentUser.getStudySetAvg();
            String scoreText;

            if (scores != null && scores.containsKey(id)) {
                double pct = scores.get(id) * 100;
                scoreText = String.format("%.0f%%", pct);
            } else {
                scoreText = "Not Attempted";
            }

            Label entry = new Label("- " + name + ": " + scoreText);
            entry.setStyle("-fx-font-size: 13px;");
            gradesBox.getChildren().add(entry);
        }
    }

    public void loadStudentStudySetView(List<Integer> ids){
        if (ids == null || ids.isEmpty()) return;
        for (Integer id : ids) {

            BaseSet tset = StudySetMaker.getSetById(id);
            String displayName;
            if (tset != null) {
                String subj = ((StudySet) tset).getSubject();
                displayName = tset.getName() + (subj != null && !subj.isBlank() ? " (" + subj + ")" : "");
            } else {
                tset = QuestionTracker.getQuestionSetById(id);
                if (tset == null) continue;
                displayName = tset.getName() + " (question set)";
            }

            HBox row = new HBox(10);
            Label lbl = new Label(displayName);
            Button takeBtn = new Button("Take");
            BaseSet finalTset = tset;
            takeBtn.setOnAction(e -> startSession(finalTset));
            row.getChildren().addAll(lbl, takeBtn);
            studySetListVBox.getChildren().add(row);
        }
    }

    public void loadTeacherStudySetView(List<Integer> ids){
        //teachers can see all their owned study sets, even unassigned ones
        StudySet[] allSets = StudySetMaker.getAllSets();
        for(StudySet set : allSets) {
            if (!user.getUsername().equals(set.getCreator())) continue;

            VBox rowGroup = new VBox(0);

            HBox row = new HBox(10);
            Label lbl = new Label(set.getName());
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);

            Button editBtn = new Button("Edit");
            editBtn.setOnAction(e -> openStudySetEditor(set));

            //if already assigned, disable the assign button
            boolean alreadyAssigned = ids.contains(set.getId());
            Button assignBtn = new Button(alreadyAssigned ? "Assigned" : "Assign");
            assignBtn.setDisable(alreadyAssigned);

            StudySet finalS = set;
            assignBtn.setOnAction(e -> {
                assignSetToClass(finalS);
                assignBtn.setText("Assigned");
                assignBtn.setDisable(true);
            });

            VBox gradesBox = new VBox(4);
            gradesBox.setVisible(false);
            gradesBox.setManaged(false);
            gradesBox.setStyle("-fx-padding: 4 0 4 16;");
            buildGradesBox(gradesBox, set.getId());

            Button gradesBtn = new Button("Grades v");
            boolean isSetAssigned = classroom.getAssignedStudySetIds().contains(set.getId());
            gradesBtn.setVisible(isSetAssigned);
            gradesBtn.setManaged(isSetAssigned);
            gradesBtn.setOnAction(e -> {
                boolean nowVisible = !gradesBox.isVisible();
                gradesBox.setVisible(nowVisible);
                gradesBox.setManaged(nowVisible);
                gradesBtn.setText(nowVisible ? "Grades ^" : "Grades v");
            });

            row.getChildren().addAll(lbl, editBtn, assignBtn, gradesBtn);
            rowGroup.getChildren().addAll(row, gradesBox);
            studySetListVBox.getChildren().add(rowGroup);
        }
    }

    private void startSession(BaseSet set) {
        if (set == null || user == null) return;
        boolean isStudyMode = set instanceof StudySet;
        SetSession session = new SetSession(set, user, isStudyMode);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetSessionView.fxml"));
            Parent root = loader.load();
            SetSessionController ctrl = loader.getController();
            Stage stage = (Stage) classNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
            stage.setScene(scene);

            ctrl.setSession(session, user, () -> {
                try {
                    FXMLLoader back = new FXMLLoader(getClass().getResource("/fxml/ClassView.fxml"));
                    Parent backRoot = back.load();
                    ClassViewController backCtrl = back.getController();
                    backCtrl.setData(classroom.getName(), user);
                    Scene backScene = new Scene(backRoot);
                    backScene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
                    stage.setScene(backScene);
                } catch (IOException e) { e.printStackTrace(); }
            });

            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateStudySet(ActionEvent event) {
        if (user == null || !user.getIsTeacher()) return;

        TextInputDialog titleDlg = new TextInputDialog();
        titleDlg.setGraphic(null);
        titleDlg.setHeaderText("Enter study set title");
        Optional<String> titleOpt = titleDlg.showAndWait();
        if (!titleOpt.isPresent()) return;
        String title = titleOpt.get().trim();
        if (title.isEmpty()) return;

        TextInputDialog subjDlg = new TextInputDialog();
        subjDlg.setGraphic(null);
        subjDlg.setHeaderText("Enter subject (optional)");
        Optional<String> subjOpt = subjDlg.showAndWait();
        String subject = subjOpt.map(String::trim).orElse("");

        teacher.StudySet set = StudySetMaker.createSet(new ArrayList<>(), user, title, subject);

        classroom.addStudySet(set);
        loadStudySets();

        // also add to user's owned sets and persist user
        try {
            user.addQuestionSetId(set.getId());
            QuestionTracker.saveUser(user);
        } catch (Exception ex) { ex.printStackTrace(); }

    }

    // Study sets (Question Sets) are now managed from the main app screen; class view keeps class-only controls.

    private void assignSetToClass(StudySet set){
        // assign to class and persist
        boolean assigned = QuestionTracker.assignStudySetToClass(classroom.getName(), set.getId());
        if (assigned) {
            // update local classroom object
            classroom.addAssignedStudySetId(set.getId());
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Study set assigned to class.", ButtonType.OK);
            a.setGraphic(null);
            a.showAndWait();
            loadStudySets();
        } else {
            Alert a = new Alert(Alert.AlertType.WARNING, "Failed to assign study set to class.", ButtonType.OK);
            a.setGraphic(null);
            a.showAndWait();
        }
    }

    @FXML
    private void handleViewStruggles(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StruggleView.fxml"));
            Parent root = loader.load();
            StruggleViewController ctrl = loader.getController();

            ctrl.setDataTeacher(user, classroom.getName(), classroom.getStudents());

            Stage stage = (Stage) classNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
            stage.setScene(scene);
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();

            ClassListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openStudySetEditor(StudySet set) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionSetView.fxml"));
            Parent root = loader.load();
            QuestionSetViewController controller = loader.getController();
            controller.setDataStudySet(set, user, classroom.getName());
            Stage stage = (Stage) classNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}