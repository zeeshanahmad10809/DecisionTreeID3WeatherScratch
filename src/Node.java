import java.util.ArrayList;

public class Node {
    public String name;

    public boolean is_leaf;
    public double entropy;
    public double IG;
    public String edge_name;
    public ArrayList<Node> links;

    public Node(){
        links = new ArrayList<>();
        is_leaf = false;

    }

}


