import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import user.Question;
import user.User;

public class questionMaker {

    public static void main(String[] args) {
        Question question = createQuestion("What is two + two?", "four");
        if (question != null) {
            System.out.println("Question successfully created: \n" + question.getText());
        } else {
            System.out.println("Failed to make question.");
        }
    }

    public static Question[] getQuestions(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("src/main/questions.json"), Question[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Question[0];
        }
    }

    private static void saveQuestion(Question[] questions){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/questions.json"), questions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Question createQuestion(String question, String answer){
        Question[] questions = getQuestions();
        int maxId = questions.length;

        Question newQuestion = new Question();
        newQuestion.setId(maxId);
        newQuestion.setText(question);
        newQuestion.setAnswer(answer);

        Question[] updatedQuestions = new Question[questions.length + 1];
        System.arraycopy(questions, 0, updatedQuestions, 0, questions.length);
        updatedQuestions[questions.length] = newQuestion;

        saveQuestion(updatedQuestions);
        return newQuestion;
    }

    public static Question createQuestion(String question, String answer, ArrayList<String> tags){
        Question[] questions = getQuestions();
        int maxId = questions.length;

        Question newQuestion = new Question(maxId, question, answer);
        newQuestion.setTags(tags);

        Question[] updatedQuestions = new Question[questions.length + 1];
        System.arraycopy(questions, 0, updatedQuestions, 0, questions.length);
        updatedQuestions[questions.length] = newQuestion;

        saveQuestion(updatedQuestions);
        return newQuestion;
    }

    public void addTag(Question question, ArrayList<String> newTags){
        List<String> tags = question.getTags();
        for(String tag : newTags){
            if(!tags.contains(tag)){
                tags.add(tag);
            }
        }
        question.setTags(tags);
    }
}
