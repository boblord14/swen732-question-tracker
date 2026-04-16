package model;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
//import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import user.Question;
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
                if (username.equals(data[i].getCreator()) && Objects.equals(title, data[i].getName())) {
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
                    boolean titleMatches = Objects.equals(title, item.getName());
                    return creatorMatches && titleMatches;
                })
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            e.printStackTrace();
            return new StudySet();
        }
    }

    public static StudySet getSetById(int id) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            StudySet[] data = mapper.readValue(new File("src/main/sets.json"), StudySet[].class);
            return Arrays.stream(data)
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static StudySet[] getAllSets() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("src/main/sets.json");
            if (!file.exists() || file.length() == 0) return new StudySet[0];
            StudySet[] data = mapper.readValue(file, StudySet[].class);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new StudySet[0];
        }
    }

    public static StudySet addTags(String username, String title, ArrayList<String> newTags){
        StudySet set = getSet(username, title);
        ArrayList<String> tags = (ArrayList<String>) set.getTags();
        
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
    public static double studySetQuiz(User user, String title){
        String username = user.getUsername();
        StudySet set = getSet(username, title);
        System.out.println("Practice Test begins now:");
        Scanner s = new Scanner(System.in);

        int correct = 0;
        int i = 0;

        ArrayList<Question> questionSet = (ArrayList<Question>) set.getQuestions();
        for(Question question : questionSet){
            System.out.println(question.getText());
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

        user.addStudySetScore(set.getId(), average);

        s.close();
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
        set.setId(nextId());
        set.setCreator(user.getUsername());
        set.setQuestionSet(questions);
        set.setName(title);
        set.setSubject(subject);
        saveSet(set);
        return set;
    }

    public static StudySet createSet(ArrayList<Question> questions, User user, String title, String subject, ArrayList<String> tags){
        StudySet set = createSet(questions, user, title, subject);
        set.setTags(new ArrayList<>(tags));

        editTags(set, user.getUsername(), title);
        return set;
    }

    private static int nextId() {
        StudySet[] all = getAllSets();
        int max = 0;
        for (StudySet s : all) if (s.getId() > max) max = s.getId();
        return max + 1;
    }

    public static SetSession createStudySetSession(int id, User user) {
        StudySet set = getSetById(id);
        if (set == null) return null;
        return new SetSession(set, user, true);
    }

    public static boolean addQuestionToStudySet(int setId, String text, String answer, List<String> tags) {
        StudySet[] all = getAllSets();
        boolean changed = false;
        for (StudySet s : all) {
            if (s != null && s.getId() == setId) {
                ArrayList<Question> qs = (ArrayList<Question>) s.getQuestions();
                if (qs == null) qs = new ArrayList<>();
                int maxId = qs.stream().mapToInt(Question::getId).max().orElse(0);
                Question q = new Question(maxId + 1, text, answer);
                if (tags != null) q.setTags(tags);
                qs.add(q);
                s.setQuestionSet(qs);
                changed = true;
                break;
            }
        }
        if (changed) saveAllSets(all);
        return changed;
    }

    public static boolean removeQuestionFromStudySet(int setId, int questionId) {
        StudySet[] all = getAllSets();
        boolean changed = false;
        for (StudySet s : all) {
            if (s != null && s.getId() == setId) {
                ArrayList<Question> qs = (ArrayList<Question>) s.getQuestions();
                if (qs != null) changed = qs.removeIf(q -> q.getId() == questionId);
                break;
            }
        }
        if (changed) saveAllSets(all);
        return changed;
    }

    private static void saveAllSets(StudySet[] sets) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/sets.json"), sets);
        } catch (Exception e) { e.printStackTrace(); }
    }

}
