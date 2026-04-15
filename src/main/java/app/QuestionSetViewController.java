package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.SetSession;
import model.questionTracker;
import model.studySetMaker;
import teacher.StudySet;
import user.Question;
import user.QuestionSet;
import user.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionSetViewController {
    @FXML
    private Button takeButton;
    @FXML
    private Button addQuestionButton;
    @FXML
    private Label setTitleLabel;
    @FXML
    private VBox questionListVBox;


    private QuestionSet set;
    private User user;
    private boolean isStudySetMode = false;
    private StudySet studySet;
    private String returnClassName;

    //question set variant
    public void setData(QuestionSet set, User user) {
        this.set = set;
        this.user = user;
        boolean isOwner = user.getUsername().equals(set.getCreator());

        takeButton.setManaged(true);
        takeButton.setVisible(true);

        //these two get rid of the button if the user doesnt own it, no reason to modify someone else's set
        addQuestionButton.setVisible(isOwner);
        addQuestionButton.setManaged(isOwner);

        refreshView();
    }

    public void setDataStudySet(StudySet set, User user, String className) {
        this.studySet = set;
        this.user = user;
        this.isStudySetMode = true;
        this.returnClassName = className;

        takeButton.setManaged(false);
        takeButton.setVisible(false);

        //redundant(if we get this far it is always the owner) but for consistency's sake
        boolean isOwner = user.getUsername().equals(set.getCreator());
        addQuestionButton.setVisible(isOwner);
        addQuestionButton.setManaged(isOwner);

        setTitleLabel.setText(set.getName() + (set.getSubject() != null && !set.getSubject().isBlank() ? " (" + set.getSubject() + ")" : ""));
        questionListVBox.getChildren().clear();

        if (set.getQuestions() == null) return;
        for (Question q : set.getQuestions()) {
            HBox row = new HBox(8);

            //show tags next to question too
            List<String> tags = q.getTags();
            String tagText = "";
            if (tags != null && !tags.isEmpty()) {
                tagText = " [" + String.join(", ", tags) + "]";
            }

            Label lbl = new Label(q.getText() + " — " + q.getAnswer() + tagText);
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("Remove");

            deleteBtn.setVisible(isOwner);
            deleteBtn.setManaged(isOwner);

            deleteBtn.setOnAction(e -> {
                studySetMaker.removeQuestionFromStudySet(studySet.getId(), q.getId());
                studySet = studySetMaker.getSetById(studySet.getId());
                setDataStudySet(studySet, user, returnClassName);
            });

            row.getChildren().addAll(lbl, deleteBtn);
            questionListVBox.getChildren().add(row);
        }
    }

    private void refreshView() {
        setTitleLabel.setText(set.getName());
        questionListVBox.getChildren().clear();
        for (Question q : set.getQuestions()) {
            HBox row = new HBox(8);

            //show tags next to question too
            List<String> tags = q.getTags();
            String tagText = "";
            if (tags != null && !tags.isEmpty()) {
                tagText = " [" + String.join(", ", tags) + "]";
            }

            Label lbl = new Label(q.getText() + " — " + q.getAnswer() + tagText);
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("Remove");

            boolean isOwner = user.getUsername().equals(set.getCreator());
            deleteBtn.setVisible(isOwner);
            deleteBtn.setManaged(isOwner);

            deleteBtn.setOnAction(e -> handleDeleteQuestion(q));

            row.getChildren().addAll(lbl, deleteBtn);
            questionListVBox.getChildren().add(row);
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (isStudySetMode) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassView.fxml"));
                Parent root = loader.load();
                ClassViewController ctrl = loader.getController();
                ctrl.setData(returnClassName, user);
                Stage stage = (Stage) setTitleLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionSetListView.fxml"));
            Parent root = loader.load();
            QuestionSetListController ctrl = loader.getController();
            ctrl.setUser(user);
            Stage stage = (Stage) setTitleLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            // fade in new scene content
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddQuestion() {
        if (isStudySetMode) { handleAddQuestionStudySet(); return; }

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setGraphic(null);
        dialog.setTitle("Add Question");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        VBox box = new VBox(8);
        TextField qField = new TextField();
        qField.setPromptText("Question text");
        TextField aField = new TextField();
        aField.setPromptText("Answer text");
        TextField tagsField = new TextField();
        tagsField.setPromptText("Tags (comma separated)");
        box.getChildren().addAll(new Label("Question:"), qField, new Label("Answer:"), aField, new Label("Tags:"), tagsField);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                String q = qField.getText();
                String a = aField.getText();
                String tags = tagsField.getText();
                return Arrays.asList(q, a, tags == null ? "" : tags);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(list -> {
            String q = list.get(0);
            String a = list.get(1);
            List<String> tags = Arrays.stream(list.get(2).split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            questionTracker.addQuestionToSet(set.getId(), q, a, tags);
            // reload the set from storage
            QuestionSet[] sets = questionTracker.getQuestionSets();
            for (QuestionSet s : sets)
                if (s.getId() == set.getId()) {
                    set = s;
                    break;
                }
            refreshView();
        });
    }

    private void handleAddQuestionStudySet() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setGraphic(null);
        dialog.setTitle("Add Question");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        VBox box = new VBox(8);
        TextField qField = new TextField(); qField.setPromptText("Question text");
        TextField aField = new TextField(); aField.setPromptText("Answer text");
        TextField tagsField = new TextField(); tagsField.setPromptText("Tags (comma separated)");
        box.getChildren().addAll(new Label("Question:"), qField, new Label("Answer:"), aField, new Label("Tags:"), tagsField);
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == addBtn ? Arrays.asList(qField.getText(), aField.getText(), tagsField.getText()) : null);
        dialog.showAndWait().ifPresent(list -> {
            List<String> tags = Arrays.stream(list.get(2).split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            studySetMaker.addQuestionToStudySet(studySet.getId(), list.get(0), list.get(1), tags);
            studySet = studySetMaker.getSetById(studySet.getId());
            setDataStudySet(studySet, user, returnClassName);
        });
    }

    @FXML
    private void handleTakeSet() {
        try {
            SetSession session = isStudySetMode
                    ? model.studySetMaker.createStudySetSession(studySet.getId(), user)
                    : questionTracker.createQuestionSetSession(set.getId(), user);
            if (session == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetSessionView.fxml"));
            Parent root = loader.load();
            SetSessionController ctrl = loader.getController();

            Stage stage = (Stage) setTitleLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);

            ctrl.setSession(session, user, () -> {
                try {
                    FXMLLoader back = new FXMLLoader(getClass().getResource("/fxml/QuestionSetView.fxml"));
                    Parent backRoot = back.load();
                    QuestionSetViewController backCtrl = back.getController();
                    if (isStudySetMode) {
                        backCtrl.setDataStudySet(studySet, user, returnClassName);
                    } else {
                        backCtrl.setData(set, user);
                    }
                    stage.setScene(new Scene(backRoot));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteQuestion(Question q) {
        questionTracker.removeQuestionFromSet(set.getId(), q.getId());

        QuestionSet[] sets = questionTracker.getQuestionSets();
        for (QuestionSet s : sets) {
            if (s.getId() == set.getId()) {
                set = s;
                break;
            }
        }
        refreshView();
    }
}
