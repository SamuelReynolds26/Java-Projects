import java.io.*;
import java.util.*;

/**
 * Compresses and decompresses files using Huffman encoding
 * PS-2, Dartmouth CS 10, Winter 2022
 *
 * @author Sam Reynolds
 */

public class FileHandler {
    protected BufferedReader text;
    protected HashMap<Character, Integer> freqMap;
    protected PriorityQueue<BinaryTree<Node>> priorityQueue;
    protected BinaryTree<Node> codeTree;
    protected Map<Character, String> codeMap;
    protected String compressedTextFileName;
    protected String decompressedTextFileName;
    protected String fileName;

    public FileHandler(String fileName) {
        try {
            this.fileName = fileName;
            text = new BufferedReader(new FileReader(fileName));

        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
        }
    }

    public void getFrequencies() {
        freqMap = new HashMap<Character, Integer>();
        try {
            boolean doneReading = false;
            while (!doneReading) {
                int currLetterNum = text.read();
                if (currLetterNum == -1){
                    doneReading = true;
                }
                char currLetter = (char)currLetterNum;
                    // Check to see if we have seen this word before, update wordCounts appropriately
                    if (freqMap.containsKey(currLetter)) {
                        // Have seen this word before, increment the count
                        freqMap.put(currLetter, freqMap.get(currLetter) + 1);
                    } else {
                        // Have not seen this word before, add the new word
                        freqMap.put(currLetter, 1);
                    }
                }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the file, if possible
        try {
            text.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    public void setPriorityQueue() { // not sure what variables to put in
        // for each key in freMap
        // create new tree with the letter as key, and freq as value
        //based on the value of each key, sort it in a sorted array
        priorityQueue = new PriorityQueue<BinaryTree<Node>>(freqMap.size(), new TreeComparator());

        for (char key : freqMap.keySet()) {
            BinaryTree<Node> charTree = new BinaryTree<>(new Node(key, freqMap.get(key))); // for each unique key, make a binary tree with the key as value
            priorityQueue.add(charTree);
        }
    }

    public void createCodeTree() {
        if (priorityQueue.size() != 0) {
            while (priorityQueue.size() > 1) {
                BinaryTree<Node> tree1 = priorityQueue.remove();
                BinaryTree<Node> tree2 = priorityQueue.remove();
                Node sumNode = new Node(tree1.getData().getFrequency() + tree2.getData().getFrequency());
                BinaryTree<Node> newSumTree = new BinaryTree<>(sumNode, tree1, tree2);
                priorityQueue.add(newSumTree);
            }
            codeTree = priorityQueue.remove();
        }
    }

    public void createCodeMap() {
        TreeMap<Character, String> result = new TreeMap<>();
        if (codeTree != null) {
            createCodeMapHelper(result, codeTree, "");
        }
        codeMap = result;
    }

    public void createCodeMapHelper(TreeMap<Character, String> mapOfCodes, BinaryTree<Node> t, String code) {
        if (t.isLeaf()) {
            mapOfCodes.put(t.getData().getLetter(), code);
        }
        if (t.hasLeft()) {
            createCodeMapHelper(mapOfCodes, t.getLeft(), code + "0");
        }
        if (t.hasRight()) {
            createCodeMapHelper(mapOfCodes, t.getRight(), code + "1");
        }
    }

    public void compress(String outputFileName) throws Exception {
        getFrequencies();
        setPriorityQueue();
        createCodeTree();
        createCodeMap();

        compressedTextFileName = outputFileName;
        System.out.println(compressedTextFileName);
        BufferedBitWriter output = new BufferedBitWriter(compressedTextFileName);
        text = new BufferedReader(new FileReader(fileName));

        try {
            if (!codeMap.isEmpty()) {
                int currLetterNum = text.read();
                while (currLetterNum != -1) {
                    // grabs the code for the current letter (in string form)
                    char currLetter = (char) currLetterNum;
                    String code = codeMap.get(currLetter);
                    for (int i = 0; i < code.length(); i++) {
                        if (code.charAt(i) == ('1')) {
                            output.writeBit(true);
                        } else {
                            output.writeBit(false);
                        }

                    }
                    currLetterNum = text.read();
                }
            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the file, if possible
        try {
            text.close();
            output.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    public void decompress(String outputFileName) throws Exception {
        decompressedTextFileName = outputFileName;
        BufferedBitReader input = new BufferedBitReader(compressedTextFileName);
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedTextFileName));
        BinaryTree<Node> tree = codeTree;

        try {
            while (input.hasNext()) {
                boolean currBit = input.readBit();
                if (!currBit) {
                    tree = tree.getLeft();
                } else {
                    tree = tree.getRight();
                }
                if (tree.isLeaf()) {
                    output.write(tree.getData().getLetter());
                    tree = codeTree;
                }
            }
        } catch (FileNotFoundException exception) {
            System.out.println("File not found in decompression");
        } catch (IOException exception) {
            System.out.println("IO exception in decompression");
        } catch (java.lang.Exception exception) {
            System.out.println("java.lang.Exception in decompression");
        }
        input.close();
        output.close();
    }

    @Override
    public String toString() {
        return "FileHandler{" +
                "compressedText='" + compressedTextFileName + '\'' +
                '}';
    }

    public static void main(String[] args) throws Exception {
        FileHandler empty = new FileHandler("CS10/PS3/WarAndPeace.txt");
        empty.compress("CS10/PS3/WarAndPeaceOutput.txt");
        empty.decompress("CS10/PS3/WarAndPeaceOutPut2.txt");
    }
}

// public static void main(String[] args) {
//   FileHandler driver = new FileHandler("PSET3/USConstitution.txt");
// String filename = "PSET3/USConstitution.txt";

//  try {
//      driver.getFrequencies();
//     driver.createCodeTree();
//   driver.createCodeMap();

// driver.compress("PSET3/USConstitutionOutput.txt");
//driver.decompress("PSET3/USConstitutionOutput2.txt");
//  }
//   catch (FileNotFoundException exception) {
//         System.out.println("File not found");
//     }
///     catch (IOException exception) {
//         System.out.println("IO exception");
//     }
//    catch (java.lang.Exception exception) {
//        System.out.println(exception);
//    }
//  }
//}


