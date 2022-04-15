import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*
This game takes a list of actors and adds them to a graph. You can then try to guess
how connected different actors are.

@author: Sam Reynolds
Date: 2/22/22


 */

public class KevinBaconGame {
    HashMap<String,String> hmActors = new HashMap<>();
    HashMap<String,String> hmMovies = new HashMap<>();

    ArrayList<String> actorsList = new ArrayList<>();
    ArrayList<String> moviesList = new ArrayList<>();
    HashMap<String, List<String>> moviesToActors = new HashMap<>(); // Map< Movie, Set<Actors> >

    // Doesn't change
    Graph<String,Set<String>> gameGraph;
    // Updated with a u command
    String root = "Kevin Bacon";
    // Updated with a u command
    Graph<String,Set<String>> pathTree;
    // Doesn't change
    HashMap<String,Integer> degreeMap = new HashMap<>();
    // Doesn't change
    HashMap<String,Double> averageSeparationMap = new HashMap<>();

    boolean playing = true;

    public KevinBaconGame(String actors, String movies, String actorMovies){
        createHM(actors, hmActors);
        createHM(movies, hmMovies);

        for (String actor: hmActors.keySet()){
            actorsList.add(hmActors.get(actor));
        }

        for (String movie: hmMovies.keySet()){
            moviesList.add(hmActors.get(movie));
        }
        System.out.println("Creating moviesToActors");
        createMovieActors(actorMovies, moviesToActors);
        System.out.println("Building gameGraph" + "\n");
        gameGraph = buildGraph();
        System.out.println("Creating averageSeparationMap");
        setAverageSeparationMap();
        System.out.println("Creating pathTree");
        pathTree = bfs(gameGraph, root);
        setDegreeMap();

    }

    // Used to read in and create the maps for actors / movies and their IDs
    public void createHM(String fileName, HashMap<String,String> map){
        // Reads in file
        try{
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            try {
                boolean doneReading = false;
                while (!doneReading) {
                    String currLine = file.readLine();
                    if (currLine == null) {
                        doneReading = true;
                    }
                    String[] splitLine = currLine.split("\\|");
                    // adds name and ID to map
                    map.put(splitLine[0], splitLine[1]);
                }
            } catch (IOException e) {
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                file.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        } catch (Exception e){
            System.out.println();
        }
    }
    // Creates the movie/actor map
    public void createMovieActors(String fileName, HashMap<String, List<String>> map){
        try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            try {
                boolean doneReading = false;
                while (!doneReading) {
                    String currLine = file.readLine();
                    if (currLine == null) {
                        doneReading = true;
                    }
                    String[] splitLine = currLine.split("\\|");
                    String movieID = splitLine[0];
                    String actorID = splitLine[1];
                    if (moviesToActors.containsKey(hmMovies.get(movieID))){ // if moviesToActors contains a movie
                        // add the actor to the set for that movie
                        moviesToActors.get(hmMovies.get(movieID)).add(hmActors.get(actorID)); //this must be the fucked up line
                    }
                    else {
                        ArrayList<String> listOfActors = new ArrayList<>(); // create a new list
                        listOfActors.add(hmActors.get(actorID)); // add the movie to the list
                        moviesToActors.put(hmMovies.get(movieID), listOfActors); // add the new set at the actor
                    }

                }
            } catch (IOException e) {
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                file.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        }
        catch (Exception e){
            System.out.println();
        }
    }

    // Builds the graph to be used in the game
    public AdjacencyMapGraph<String,Set<String>> buildGraph(){
        AdjacencyMapGraph<String,Set<String>> newGraph = new AdjacencyMapGraph<>();
        for (String actor: actorsList){
            newGraph.insertVertex(actor);
        }
        for (String movie: moviesToActors.keySet()){
            for (String actor1 : moviesToActors.get(movie)){
                for (String actor2 : moviesToActors.get(movie)){
                    if (!actor1.equals(actor2)){
                        if (!newGraph.hasEdge(actor1,actor2)){
                            Set<String> newSet = new HashSet<>();
                            newSet.add(movie);
                            newGraph.insertUndirected(actor1,actor2,newSet);
                        }
                        else{
                            newGraph.getLabel(actor1,actor2).add(movie);
                        }
                    }
                }
            }
        }
        return newGraph;
    }


    /** Breadth First Search
     *
     * @param g -- graph to search
     * @param source -- starting vertex
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source){
        //System.out.println("\nBreadth First Search from " + source);
        Graph<V,E> pathTree = new AdjacencyMapGraph<>(); //initialize backTrack
        pathTree.insertVertex(source); //load source vertex
        Set<V> visited = new HashSet<V>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<V>(); //queue to implement BFS

        try{
            queue.add(source); //enqueue source vertex
            visited.add(source); //add source to visited Set
            while (!queue.isEmpty()) { //loop until no more vertices
                V u = queue.remove(); //dequeue
                for (V v : g.outNeighbors(u)) { //loop over out neighbors
                    if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                        visited.add(v); //add neighbor to visited Set
                        queue.add(v); //enqueue neighbor
                        pathTree.insertVertex(v); //save that this vertex was discovered
                        pathTree.insertDirected(v,u,g.getLabel(v,u)); // from prior vertex
                        // something is wrong with the edge
                    }
                }
            }
        } catch(Exception e) {
            System.out.println("Source not in graph.");
        }
        return pathTree;
    }

    // get the path from a given vertex to the current center of the universe
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v){
        List<V> path = new ArrayList<>();
        V currVertex = v;
        while (tree.outDegree(currVertex) > 0){
            path.add(currVertex);
            for (V vertex : tree.outNeighbors(currVertex)){
                currVertex = vertex;
            }
        }
        path.add(currVertex);

        return path;
    }

    // checks to see what vertices are in the game graph but are not in the current path tree graph
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        Set<V> nonOverLappingVertices = new HashSet<>();
        for (V value1: graph.vertices()){
            if (!subgraph.hasVertex(value1)){
                nonOverLappingVertices.add(value1);
            }
        }
        return nonOverLappingVertices;
    }

    // calculates the average separation for a given vertex in a graph using totalSeparation
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root){
        double tS = totalSeparation(tree, root,0);
        return tS/ tree.numVertices();
    }

    // calculates the total separation for a given vertex in a graph
    public static <V,E> double totalSeparation(Graph<V,E> tree, V root, double separation){
        double totalSeparationNum = separation;
        for (V vertex : tree.inNeighbors(root)) {
            totalSeparationNum += totalSeparation(tree, vertex, separation+1);
        }
        return totalSeparationNum;
    }

    // Stores the average separtion for each vertex in the graph so that it only has to be done once and does not have
    // to be recalculated each time.
    public void setAverageSeparationMap(){
        for (String vertex: gameGraph.vertices()){
            Graph<String,Set<String>> newPathTree = new AdjacencyMapGraph<>();
            newPathTree = bfs(gameGraph,vertex);
            averageSeparationMap.put(vertex,averageSeparation(newPathTree,vertex));
        }
    }

    // Stores the average outDegree for each vertex in the graph so that it only has to be done once and does not have
    // to be recalculated each time.
    public void setDegreeMap(){
        for (String vertex: gameGraph.vertices()){
            degreeMap.put(vertex,gameGraph.outDegree(vertex));
        }
    }

    // This method sets up the gameflow.
    public void playGame(){
        // Opening message of the game explaining the different commands
        System.out.println("Welcome to the Kevin Bacon Game.");
        System.out.println("Commands:\n" +
                "\t" + "c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" +
                "\t" + "d <low> <high>: list actors sorted by degree, with degree between low and high\n" +
                "\t" + "i: list actors with infinite separation from the current center\n" +
                "\t" + "p <name>: find path from <name> to current center of the universe\n" +
                "\t" + "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" +
                "\t" + "u <name>: make <name> the center of the universe\n" +
                "\t" + "q: quit game");
        System.out.println("Enter a command letter followed by a space then the parameters. Capitalize the name.");
        System.out.println("\n" + root + " is now the center of universe, connected to " + (pathTree.numVertices() - 1) + "/" + gameGraph.numVertices() + "actors with an average separation of " + averageSeparationMap.get(root));

        // Gameplay logic.
        while (playing){
            System.out.println("\n" + root + " game >");
            Scanner newPlayerInputScanner = new Scanner(System.in);
            //handle scanner
            String newPlayerInputString = newPlayerInputScanner.nextLine();
            String[] newPlayerInputSplit = newPlayerInputString.split(" ");
            int inputLength = newPlayerInputSplit.length;
            String givenCommand = newPlayerInputSplit[0].toLowerCase();
            if (givenCommand.equals("c")){
                // centers of the universe, sorted by average separation
                try {
                    Integer inputNum = Integer.parseInt(newPlayerInputSplit[1]);
                    System.out.println(newPlayerInputSplit[1]);
                    /**
                     * If the input number is greater than zero, the following code will return the wanted number of
                     * names with the lowest avg separation scores - ie. the "top scores"
                     */
                    if (inputNum > 0) {
                        // create an array list of actor names
                        ArrayList<String> sortedBySeparation = new ArrayList<>();
                        ArrayList<String> result = new ArrayList<>();
                        for (String vertex : pathTree.vertices()) {
                            sortedBySeparation.add(vertex);
                        }
                        // sort the actor names based on the averageSeparation double (this one is low to high) - ie. lowest separation
                        sortedBySeparation.sort((String vertex1, String vertex2) -> (int) Math.signum(averageSeparationMap.get(vertex1) - averageSeparationMap.get(vertex2)));

                        for (int i = 0; i < inputNum; i++) {
                            result.add(sortedBySeparation.remove(0));
                        }
                        System.out.println(result);
                    }

                    /**
                     * If the input number is less than zero, the following code will return the wanted number of
                     * names with the highest avg separation scores - ie. the "worst scores"
                     */
                    else if (inputNum < 0) {
                        // create an array list of actor names
                        ArrayList<String> sortedBySeparation = new ArrayList<>();
                        ArrayList<String> result = new ArrayList<>();
                        for (String vertex : pathTree.vertices()) {
                            sortedBySeparation.add(vertex);
                        }
                        // sort the actor names based on the averageSeparation double (this one is high to low) - ie. largest separation
                        sortedBySeparation.sort((String vertex1, String vertex2) -> (int) Math.signum(averageSeparationMap.get(vertex2) - averageSeparationMap.get(vertex1)));
                        for (int i = 0; i > inputNum; inputNum++) {
                            result.add(sortedBySeparation.remove(0));
                        }
                        System.out.println(result);
                    }
                } catch (Exception e){
                    System.out.println("Input not an integer. Command only accepts integers.");
                }
            }
            else if (givenCommand.equals("d")){
                try{
                    int minDegree = Integer.parseInt(newPlayerInputSplit[1]);
                    int maxDegree = Integer.parseInt(newPlayerInputSplit[2]);
                    if (minDegree < 0 || maxDegree <0){
                        System.out.println("Negatives not accepted for this command.");
                    } else{
                        ArrayList<String> sortedByDegree = new ArrayList<>();
                        ArrayList<String> result = new ArrayList<>();
                        for (String vertex: gameGraph.vertices()){
                            sortedByDegree.add(vertex);
                        }
                        // sort the actor names based on the averageSeparation double (this one is low to high)
                        sortedByDegree.sort((String vertex1, String vertex2) -> (int)Math.signum(degreeMap.get(vertex1) - degreeMap.get(vertex2)));

                        for (String vertex: sortedByDegree){
                            if (degreeMap.get(vertex) == maxDegree){
                                break;
                            }
                            if (minDegree < degreeMap.get(vertex)){
                                result.add(vertex);
                            }
                        }

                        System.out.println(result);
                    }
                } catch (Exception e){
                    System.out.println("Command only excepts integers as inputs.");
                }
            }
            else if (givenCommand.equals("i")){
                System.out.println(missingVertices(gameGraph, pathTree));
            }
            else if (givenCommand.equals("p")){
                String vertex = "";
                for (int i = 1; i < inputLength-1; i++){
                    vertex += newPlayerInputSplit[i] + " ";
                }
                vertex += newPlayerInputSplit[inputLength-1];
                if (pathTree.hasVertex(vertex)){
                    List<String> vertexPath = getPath(pathTree, vertex);
                    String vertexPathWrittenOut = "";//vertex + " appeared in [" + gameGraph.getLabel(vertex,vertexPath.get(0)) +
                    //"] with " + vertexPath.get(0);
                    for (int i = 1; i < vertexPath.size(); i++){
                        vertexPathWrittenOut = vertexPathWrittenOut + vertexPath.get(i-1) + " appeared in [" +
                                gameGraph.getLabel(vertexPath.get(i-1), vertexPath.get(i)) + "] with " +
                                vertexPath.get(i) + "\n";
                    }
                    String result = root + "Game >" + "\n" +
                            vertex + "\n" +
                            vertex + "'s number: " + (vertexPath.size()-1) + "\n" +
                            vertexPathWrittenOut;
                    System.out.println(result);
                }
                else {
                    System.out.println("Path tree does not contain vertex. Try another vertex!");
                }
            }
            else if (givenCommand.equals("s")){
                try{
                    double minSeparation = Double.parseDouble(newPlayerInputSplit[1]);
                    double maxSeparation = Double.parseDouble(newPlayerInputSplit[2]);
                    if (minSeparation < 0 || maxSeparation <0){
                        System.out.println("Negative numbers not accepted for this command.");
                    } else{
                        ArrayList<String> sortedBySeparation = new ArrayList<>();
                        ArrayList<String> result = new ArrayList<>();
                        for (String vertex: gameGraph.vertices()){
                            sortedBySeparation.add(vertex);
                        }
                        // sort the actor names based on the averageSeparation double (this one is low to high)
                        sortedBySeparation.sort((String vertex1, String vertex2) -> (int)Math.signum(averageSeparationMap.get(vertex1) - averageSeparationMap.get(vertex2)));

                        for (String vertex: sortedBySeparation){
                            if (averageSeparationMap.get(vertex) >= maxSeparation){
                                break;
                            }
                            if (minSeparation < averageSeparationMap.get(vertex)){
                                result.add(vertex);
                            }
                        }
                        System.out.println(result);
                    }
                } catch (Exception e){
                    System.out.println("Command only excepts integers as inputs.");
                }
            }
            else if (givenCommand.equals("u")){
                String vertex = "";
                for (int i = 1; i < inputLength-1; i++){
                    vertex += newPlayerInputSplit[i] + " ";
                }
                vertex += newPlayerInputSplit[inputLength-1];
                if (gameGraph.hasVertex(vertex)){
                    root = vertex;
                    pathTree = bfs(gameGraph,root);
                    System.out.println(root + " is now the center of universe, connected to " + (pathTree.numVertices() - 1) + "/" + gameGraph.numVertices() + "actors with an average separation of " + averageSeparationMap.get(root));
                }
                else{
                    System.out.println("Game graph does not contain the vertex. Try another!");
                }
            }
            else if (givenCommand.equals("q")){
                System.out.println("Game quit.");
                playing = false;
            }
            else {
                System.out.println("Unrecognized command. Try another character.");
            }

        }
    }

    public static void main(String[] args) {
        KevinBaconGame newGame = new KevinBaconGame("CS10/PS4/actors.txt", "CS10/PS4/movies.txt", "CS10/PS4/movie-actors.txt");
        newGame.playGame();
    }
}


