public class Node {
    protected char letter;
    protected int frequency;

    public Node (int frequency){
        this.frequency = frequency;
    }
    public Node(char letter, int frequency){
        this.letter = letter;
        this.frequency = frequency;
    }

    public char getLetter(){
        return letter;
    }

    public int getFrequency() {
        return frequency;
    }
}

