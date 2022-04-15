import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Viterbi {
    HashMap<String, HashMap<String,Double>> states = new HashMap<>(); //counts for now
    HashMap<String, HashMap<String,Double>> transitions = new HashMap<>(); //counts for now
    double unseenWordScore = -1000.0;
    boolean creatingStartNode = true;

    ArrayList<ArrayList<String>> trainingSentences = new ArrayList<>();
    ArrayList<ArrayList<String>> trainingTags = new ArrayList<>();


    public Viterbi(HashMap<String, HashMap<String,Double>> states, HashMap<String,HashMap<String,Double>> transitions){
        this.states = states;
        this.transitions = transitions;
    }

    public Viterbi(String trainingSentencesFileName, String trainingTagsFileName){
        linesAsArrays(trainingSentencesFileName, trainingTagsFileName);
        trainStates();
        trainTransitions();
    }

    public void linesAsArrays(String sentenceFileName, String tagFileName){
        try{
            BufferedReader sentenceFile = new BufferedReader(new FileReader(sentenceFileName));
            try {
                boolean doneReading = false;
                while (!doneReading) {
                    String currLine = sentenceFile.readLine();
                    if (currLine == null) {
                        doneReading = true;
                    }
                    String currLineLower = currLine.toLowerCase();
                    String[] splitLine = currLineLower.split(" ");
                    ArrayList<String> line = new ArrayList<>();
                    for (String word: splitLine){
                        line.add(word);
                    }
                    trainingSentences.add(line);
                }
            } catch (IOException e) {
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                sentenceFile.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        } catch (Exception e){
            System.out.println();
        }


        try{
            BufferedReader tagFile = new BufferedReader(new FileReader(tagFileName));
            try {
                boolean doneReading = false;
                while (!doneReading) {
                    String currLine = tagFile.readLine();
                    if (currLine == null) {
                        doneReading = true;
                    }
                    String[] splitLine = currLine.split(" ");
                    ArrayList<String> line = new ArrayList<>();
                    for (String word: splitLine){
                        line.add(word);
                    }
                    trainingTags.add(line);
                }
            } catch (IOException e) {
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                tagFile.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        } catch (Exception e){
            System.out.println();
        }
    }

    public void trainStates(){
        // Getting the counts first
        for (int i = 0; i < trainingTags.size(); i++){
            // Grabs an ArrayList of a line split from trainingTags
            ArrayList<String> currLineTags = trainingTags.get(i);
            // Grabs an ArrayList of a line split from trainingSenteces
            ArrayList<String> currLineSentences = trainingSentences.get(i);

            for (int j = 0; j < currLineTags.size(); j++){
                // If the state (POS) already exists
                if (states.containsKey(currLineTags.get(j))){
                    // Check to see if the current word (at position i in currLineSentences)
                    // is in the map at that state
                    if (states.get(currLineTags.get(j)).containsKey(currLineSentences.get(j))){
                        // Update the count of that word in that state
                        states.get(currLineTags.get(j)).put(currLineSentences.get(j),states.get(currLineTags.get(j)).get(currLineSentences.get(j))+1);
                    }
                    // Else create add that word (at position i in currLineSentences) with a count of one to the map
                    else{
                        states.get(currLineTags.get(j)).put(currLineSentences.get(j),1.0);
                    }
                } else {
                    HashMap<String,Double> wordMap = new HashMap<>();
                    wordMap.put(currLineSentences.get(j), 1.0);
                    states.put(currLineTags.get(j), wordMap);
                }
            }

        }
        // Now converting scores to ln(probabilites)
        for (String state : states.keySet()){
            double totalWords = 0.0;
            for (String word: states.get(state).keySet()){
                totalWords += states.get(state).get(word);
            }
            for (String word: states.get(state).keySet()){
                states.get(state).put(word, Math.log(states.get(state).get(word)/totalWords));
            }
        }
    }

    public void trainTransitions(){
        for (int i = 0; i < trainingTags.size(); i++){
            ArrayList<String> currLineTags = trainingTags.get(i);
            for (int j = -1; j < currLineTags.size()-1; j++){
                int next = j + 1;
                // Creating the start node
                if (j == -1){
                    // If there is no starting "node"
                    if (!transitions.containsKey("start")){
                        // Add a starting node
                        HashMap<String,Double> startTransition = new HashMap<>();
                        startTransition.put(currLineTags.get(next), 1.0);
                        transitions.put("start", startTransition);
                    } else {
                        // If the start state already has the transition
                        if (transitions.get("start").containsKey(currLineTags.get(next))){
                            // Update the count of the transition
                            transitions.get("start").put(currLineTags.get(next), transitions.get("start").get(currLineTags.get(next))+1);
                        } else {
                            // Add the new "ending" state with a count of one
                            transitions.get("start").put(currLineTags.get(next), 1.0);
                        }
                    }
                    creatingStartNode = false;
                } else {
                    // If the "starting" state already exists as a key in transitions
                    if (transitions.containsKey(currLineTags.get(j))){
                        // If the "ending" state already exists as a key in the map
                        if (transitions.get(currLineTags.get(j)).containsKey(currLineTags.get(next))){
                            // Update the count of the transition
                            transitions.get(currLineTags.get(j)).put(currLineTags.get(next),transitions.get(currLineTags.get(j)).get(currLineTags.get(next))+1);
                        }
                        // Else create add ending state with a count of one to the map
                        else{
                            transitions.get(currLineTags.get(j)).put(currLineTags.get(next),1.0);
                        }
                    } else {
                        // Add the new "ending" state with a count of one
                        HashMap<String,Double> newTransition = new HashMap<>();
                        newTransition.put(currLineTags.get(next), 1.0);
                        transitions.put(currLineTags.get(j), newTransition);
                    }
                }
            }
        }
        // Now converting scores to ln(probabilites)
        for (String startingState : transitions.keySet()){
            double totalTransitions = 0.0;
            for (String transition: transitions.get(startingState).keySet()){
                totalTransitions += transitions.get(startingState).get(transition);
            }
            for (String transition: transitions.get(startingState).keySet()){
                transitions.get(startingState).put(transition, Math.log(transitions.get(startingState).get(transition)/totalTransitions));
            }
        }
    }

    public void assignPOSTags(String testFileName, String testTags){
        try{
            ArrayList<String> taggedSentences = new ArrayList<>();
            ArrayList<ArrayList<String>> calculatedTags = new ArrayList<>();
            BufferedReader sentenceFile = new BufferedReader(new FileReader(testFileName));
            try {
                boolean doneReading = false;
                while (!doneReading) {
                    String currLine = sentenceFile.readLine();
                    if (currLine == null) {
                        doneReading = true;
                    }
                    if (!doneReading){
                        ArrayList<String> bestPathForCurrLine = calculateViterbi(currLine.toLowerCase());
                        calculatedTags.add(bestPathForCurrLine);
                        String[] splitLine = currLine.split(" ");
                        String taggedLine = "";
                        for (int i = 0; i < splitLine.length; i++){
                            taggedLine += splitLine[i] + "/" + bestPathForCurrLine.get(i) + " ";
                        }
                        taggedSentences.add(taggedLine);
                    }
                }
                // Prints out the results
                System.out.println("Tagged Sentences:" + "\n");
                for (int i = 0; i < taggedSentences.size()-1; i++){
                    System.out.println(taggedSentences.get(i));
                }

                // Calculates how well the machine learning model performed

                // Tag accuracy
                double correctlyAssignedTags = 0.0;
                double totalTags = 0.0;
                // Line accuracy
                double correctlyAssignedLines = 0.0;
                int lineNum = 0;
                BufferedReader testTagFile = new BufferedReader(new FileReader(testTags));
                boolean isDoneReading = false;
                while (!isDoneReading) {
                    String currLine = testTagFile.readLine();
                    if (currLine == null) {
                        isDoneReading = true;
                    }
                    if (!isDoneReading){
                        String[] splitLine = currLine.split(" ");
                        for (int i = 0; i < splitLine.length; i++){
                            totalTags += 1;
                            if (splitLine[i].equals(calculatedTags.get(lineNum).get(i))){
                                correctlyAssignedTags += 1;
                            }
                        }
                        String result = "";
                        for (int i = 0; i < calculatedTags.get(lineNum).size()-1; i++){
                            result += calculatedTags.get(lineNum).get(i) + " ";
                        }
                        result += calculatedTags.get(lineNum).get(calculatedTags.get(lineNum).size()-1);
                        if (currLine.equals(result)){
                            correctlyAssignedLines += 1;
                        }
                    }
                    lineNum += 1;
                }

                // Prints out the results
                System.out.println("\n" + "Model Accuracy:");
                System.out.println("\t" + "Tag Accuracy:");
                System.out.println("\t" + "\t" + "Total tags: " + totalTags);
                System.out.println("\t" + "\t" + "Number of correctly predicted tags: " + correctlyAssignedTags);
                System.out.println("\t" + "\t" + "Number of incorrectly predicted tags: " + (totalTags-correctlyAssignedTags));
                String correctTagsPercentage = String.format("%.2f",(correctlyAssignedTags/totalTags)*100);
                System.out.println("\t" + "\t" + "Percent Correct Tags: " + correctTagsPercentage + "%");
                System.out.println("\t" + "Line Accuracy:");
                System.out.println("\t" + "\t" + "Total lines: " + (lineNum-1));
                System.out.println("\t" + "\t" + "Number of correctly predicted lines: " + correctlyAssignedLines);
                System.out.println("\t" + "\t" + "Number of incorrectly predicted lines: " + (lineNum-1-correctlyAssignedLines));
                String correctLinesPercentage = String.format("%.2f",(correctlyAssignedLines/(lineNum-1))*100);
                System.out.println("\t" + "\t" + "Percent Correct Lines: " + correctLinesPercentage + "%");

            } catch (IOException e) {
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                sentenceFile.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        } catch (Exception e){
            System.out.println("File not found.");
        }
    }

    public ArrayList<String> calculateViterbi(String sentence){ // maybe need to return best score or a string with the words and assigned POS
       String[] words = sentence.split(" ");
       int sentenceLength = words.length;

       ArrayList<HashMap<String,String>> backPointers = new ArrayList<>();
       HashSet<String> currentStates = new HashSet<>();
       //ArrayList<String> currentStates = new ArrayList<>();
       HashMap<String, Double> currentScores= new HashMap<>();

       currentStates.add("start");
       currentScores.put("start", 0.0);
       // Building currentScores and backPointers
       for (int i = 0; i < sentenceLength; i++){
           HashSet<String> nextStates = new HashSet<>();
           //ArrayList<String> nextStates = new ArrayList<>();
           HashMap<String,Double> nextScores = new HashMap<>();
           HashMap<String,String> nextBackPointers = new HashMap<>();
           // Iterates over each currState in the currentStates ArrayList
           for (String currState: currentStates){
               // Iterates over each transition state (held in the keySet of currState) of the
               // currState in the current state ArrayList
               if (transitions.containsKey(currState)){
                   for (String nextState : transitions.get(currState).keySet()) {
                       nextStates.add(nextState);
                       double observationScore;
                       if (states.get(nextState).containsKey(words[i])) {
                           observationScore = states.get(nextState).get(words[i]);
                       } else {
                           observationScore = unseenWordScore;
                       }
                       double nextScore = currentScores.get(currState) + transitions.get(currState).get(nextState) + observationScore;
                       if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) { // Error is here
                           nextScores.put(nextState, nextScore);
                           nextBackPointers.put(nextState, currState);
                       }
                   }
               }
           }
           backPointers.add(i,nextBackPointers);
           currentStates = nextStates;
           currentScores = nextScores;
       }
       ArrayList<String> bestPath = new ArrayList<>();

       double maxScore = Double.NEGATIVE_INFINITY;
       String stateWithMaxScore = "";
       for (String state: currentScores.keySet()){
           if (currentScores.get(state) > maxScore){
               maxScore = currentScores.get(state);
               stateWithMaxScore = state;
           }
       }
       String currState = stateWithMaxScore;
       for (int i = 0; i < sentenceLength; i++){
            bestPath.add(0,currState);
            currState = backPointers.get(sentenceLength-(i+1)).get(currState);
       }
       return bestPath;
    }

    public static void main(String[] args) {
        /*

        // Creating the states map
        HashMap<String, HashMap<String,Double>> exampleStates = new HashMap<>();
        // Creating the word maps for each POS tag
        //NP
        HashMap<String, Double> NPwordList = new HashMap<>();
        NPwordList.put("jobs", -0.7);
        NPwordList.put("will", -0.7);
        //MOD
        HashMap<String, Double> MODwordList = new HashMap<>();
        MODwordList.put("can", -0.7);
        MODwordList.put("will", -0.7);
        //PRO
        HashMap<String, Double> PROwordList = new HashMap<>();
        PROwordList.put("i",-1.9);
        PROwordList.put("many", -1.9);
        PROwordList.put("me", -1.9);
        PROwordList.put("mine", -1.9);
        PROwordList.put("you", -0.8);
        //VD
        HashMap<String, Double> VDwordList = new HashMap<>();
        VDwordList.put("saw", -1.1);
        VDwordList.put("were", -1.1);
        VDwordList.put("wore", -1.1);
        //N
        HashMap<String, Double> NwordList = new HashMap<>();
        NwordList.put("color",-2.4);
        NwordList.put("cook",-2.4);
        NwordList.put("jobs",-2.4);
        NwordList.put("mine",-2.4);
        NwordList.put("uses",-2.4);
        NwordList.put("fish",-1.0);
        NwordList.put("saw",-1.7);
        //DET
        HashMap<String, Double> DETwordList = new HashMap<>();
        DETwordList.put("a", -1.3);
        DETwordList.put("many", -1.7);
        DETwordList.put("one", -1.7);
        DETwordList.put("the", -1.0);
        //V
        HashMap<String, Double> VwordList = new HashMap<>();
        VwordList.put("color", -2.1);
        VwordList.put("cook", -1.4);
        VwordList.put("eats", -2.1);
        VwordList.put("fish", -2.1);
        VwordList.put("has", -1.4);
        VwordList.put("uses", -2.1);

        //Creating the states
        exampleStates.put("START", new HashMap<String,Double>());
        exampleStates.put("NP", NPwordList);
        exampleStates.put("MOD", MODwordList);
        exampleStates.put("N", NwordList);
        exampleStates.put("V", VwordList);
        exampleStates.put("VD", VDwordList);
        exampleStates.put("DET", DETwordList);
        exampleStates.put("PRO", PROwordList);

        // Creating the transitions map
        HashMap<String, HashMap<String,Double>> exampleTransitions = new HashMap<>();

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("start", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("start").put("NP", -1.6);
        exampleTransitions.get("start").put("MOD", -2.3);
        exampleTransitions.get("start").put("PRO", -1.2);
        exampleTransitions.get("start").put("DET", -0.9);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("NP", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("NP").put("VD", -0.7);
        exampleTransitions.get("NP").put("V", -0.7);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("DET", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("DET").put("N",0.0);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("VD", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("VD").put("PRO", -0.4);
        exampleTransitions.get("VD").put("DET", -1.1);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("PRO", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("PRO").put("VD", -1.6);
        exampleTransitions.get("PRO").put("V", -0.5);
        exampleTransitions.get("PRO").put("MOD", -1.6);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("MOD", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("MOD").put("V", -0.7);
        exampleTransitions.get("MOD").put("PRO", -0.7);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("V", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("V").put("DET", -0.2);
        exampleTransitions.get("V").put("PRO", -1.9);

        // Add the state that contains a map of it's transitions
        exampleTransitions.put("N", new HashMap<>());
        // Adding to the map of transitions
        exampleTransitions.get("N").put("VD", -1.4);
        exampleTransitions.get("N").put("V", -0.3);


        Viterbi exampleViterbi = new Viterbi(exampleStates, exampleTransitions);
        exampleViterbi.assignPOSTags("CS10/PS5/hard-code-test.txt", "CS10/PS5/hard-code-tags.txt");


         */


        Viterbi test1 = new Viterbi("CS10/PS5/simple-train-sentences.txt", "CS10/PS5/simple-train-tags.txt");
        test1.assignPOSTags("CS10/PS5/simple-test-sentences.txt","CS10/PS5/simple-test-tags.txt");

        //Viterbi test2 = new Viterbi("CS10/PS5/brown-train-sentences.txt", "CS10/PS5/brown-train-tags.txt");
        //test2.assignPOSTags("CS10/PS5/brown-test-sentences.txt","CS10/PS5/brown-test-tags.txt");

    }
}
