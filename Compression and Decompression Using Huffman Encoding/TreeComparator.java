import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class TreeComparator implements Comparator<BinaryTree<Node>> {


    @Override
    public int compare(BinaryTree<Node> o1, BinaryTree<Node> o2) {
        if (o1.getData().getFrequency() == o2.getData().getFrequency()){
            return 0;
        }
        if (o1.getData().getFrequency() < o2.getData().getFrequency()){
            return -1;
        }
        else {return 1;}
        }
}
