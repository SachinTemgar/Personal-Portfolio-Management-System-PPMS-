import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class AdjacencyListGraph implements GraphADT {

    private final Map<String, List<String>> adjacencyMap = new HashMap<>();

    @Override
    public void addVertex(String vertex) {
        adjacencyMap.putIfAbsent(vertex, new ArrayList<>());
    }

    @Override
    public void addEdge(String from, String to) {
        addVertex(from);
        addVertex(to);
        adjacencyMap.get(from).add(to);
        adjacencyMap.get(to).add(from);
    }

    @Override
    public boolean removeVertex(String vertex) {
        if (!adjacencyMap.containsKey(vertex)) {
            return false;
        }
        adjacencyMap.remove(vertex);
        for (List<String> neighbors : adjacencyMap.values()) {
            neighbors.remove(vertex);
        }
        return true;
    }

    @Override
    public boolean removeEdge(String from, String to) {
        if (!adjacencyMap.containsKey(from) || !adjacencyMap.containsKey(to)) {
            return false;
        }
        boolean removedFrom = adjacencyMap.get(from).remove(to);
        boolean removedTo = adjacencyMap.get(to).remove(from);
        return removedFrom && removedTo;
    }

    @Override
    public List<String> getAdjacentVertices(String vertex) {
        return adjacencyMap.getOrDefault(vertex, new ArrayList<>());
    }
    @Override
    public void clear() {
        adjacencyMap.clear();
    }
}
