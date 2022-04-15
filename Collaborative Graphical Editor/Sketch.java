import java.util.ArrayList;
import java.util.TreeMap;

public class Sketch {
    TreeMap<Integer,Shape> sketch = new TreeMap<>(); // each string: shapeType,color(as an int),parameters for shape type;

    public synchronized void addToSketch(Integer shapeID, Shape shape){
        sketch.put(shapeID,shape);
    }

    public synchronized void removeFromSketch(int shapeID) {
        sketch.remove(shapeID);
    }

    public synchronized int getShapeID(int x, int y){
        int resultID = -1;
        for (Integer shapeID: sketch.descendingKeySet()){
            if (sketch.get(shapeID).contains(x,y)){
                return shapeID;
            }
        }
        return resultID;
    }

    public synchronized TreeMap<Integer,Shape> getSketchMap(){
        return sketch;
    }
}
