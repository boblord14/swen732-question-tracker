import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import question.Question;
import teacher.StudySet;
import user.User;

public class studySetMaker {
    public static void main(String[] args) {
        
    }

    //Adds the set to the json file
    private static void saveSet(StudySet set){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/sets.json");
        try {
            StudySet[] existing = new StudySet[0];

            if (file.exists() && file.length() > 0) {
                existing = mapper.readValue(file, StudySet[].class);
            }

            List<StudySet> list = new ArrayList<>(Arrays.asList(existing));
            list.add(set);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Goes into the json file and adds the tags to the study set
    private static void editTags(StudySet set, String username, String title){
        ObjectMapper mapper = new ObjectMapper();
        try {
            StudySet[] data = mapper.readValue(new File("src/main/sets.json"), StudySet[].class);
            for (int i = 0; i < data.length; i++) {
                if (username.equals(data[i].getCreator()) && Objects.equals(title, data[i].getTitle())) {
                    data[i] = set;   // <-- THIS is the important part
                    break;
                }
            }
            
            
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/sets.json"), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Retrieves the users set based on the name entered
    public static StudySet getSet(String username, String title){
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            StudySet[] data = mapper.readValue(new File("src/main/sets.json"), StudySet[].class);
            return Arrays.stream(data)
                .filter(item -> {
                    boolean creatorMatches = username.equals(item.getCreator());
                    boolean titleMatches = Objects.equals(title, item.getTitle());
                    return creatorMatches && titleMatches;
                })
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            e.printStackTrace();
            return new StudySet();
        }
    }

    public static StudySet addTags(String username, String title, ArrayList<String> newTags){
        StudySet set = getSet(username, title);
        ArrayList<String> tags = set.getTags();
        
        if (tags == null){
            set.setTags(newTags);
        } else{
            System.out.println(tags);
            for (String t : newTags) {
                tags.add(t);
            }

            set.setTags(tags);
        }
        editTags(set, username, title);
        return set;
    }

    //This function quizzes a user on a specific study set
    public static double studySetQuiz(String username, String title){
        StudySet set = getSet(username, title);
        System.out.println("Practice Test begins now:");
        Scanner s = new Scanner(System.in);

        int correct = 0;
        int i = 0;

        ArrayList<Question> questionSet = set.getQuestionSet();
        for(Question question : questionSet){
            System.out.println(question.getQuestion());
            System.out.print("Your answer: ");
            String line = s.nextLine();
            
            if(line.toLowerCase().equals(question.getAnswer().toLowerCase())){
                System.out.println("Correct");
                correct++;
            } else{
                System.out.println("Incorrect");
            }

            i++;
        }
        
        double average = Double.valueOf(correct) / Double.valueOf(i);
        System.out.println("You got " + correct + " out of " + i + " questions right.\n"
            + "Your average is " + average);

        return average;
    }

    public static long getSetCount(String username){
        ObjectMapper mapper = new ObjectMapper();
        try {
            StudySet[] data = mapper.readValue(new File("src/main/sets.json"), StudySet[].class);
            return Arrays.stream(data)
                .filter(item -> username.equals(item.getCreator()))
                .count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static StudySet createSet(ArrayList<Question> questions, User user, String title, String subject){
        StudySet set = new StudySet();
        set.setCreator(user.getUsername());
        set.setQuestionSet(questions);
        set.setSubject(subject);
        set.setTitle(title);

        saveSet(set);
        return set;
    }

    public static StudySet createSet(ArrayList<Question> questions, User user, String title, String subject, ArrayList<String> tags){
        StudySet set = new StudySet();
        set.setCreator(user.getUsername());
        set.setQuestionSet(questions);
        set.setSubject(subject);
        set.setTags(tags);
        set.setTitle(title);

        saveSet(set);
        return set;
    }
}
