import java.util.List;

public interface GraphADT {
    void addVertex(String vertex);
    void addEdge(String from, String to);
    boolean removeVertex(String vertex);
    boolean removeEdge(String from, String to);
    List<String> getAdjacentVertices(String vertex);
    void clear();
}
