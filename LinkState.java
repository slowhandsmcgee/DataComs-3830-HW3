import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class LinkState {
    private final Path filePath;
    private final Graph graph;
    private final float MAX = Float.MAX_VALUE;

    private LinkState(Path filePath) {
        this.graph = new Graph();
        this.filePath = filePath;
    }

    private class Node {
        private final int id;
        private final Map<Node, Float> destinations = new HashMap<>();
        private float totalCost = MAX;
        private List<Node> shortestPath = new LinkedList<>();

        Node(int id) {
            this.id = id;
        }

        private void addDest(Node dest, float cost) {
            destinations.put(dest, cost);
        }

        int getID() {
            return this.id;
        }

        Map<Node, Float> getDest() {
            return this.destinations;
        }

        List<Node> getShortestPath() {
            return this.shortestPath;
        }

        float getTotalCost() {
            return this.totalCost;
        }

        void setTotalCost(float totalCost) {
            this.totalCost = totalCost;
        }

        void setShortestPath(List<Node> shortestPath) {
            this.shortestPath = shortestPath;
        }
    }

    private class Graph {
        private final Set<Node> nodes;

        Graph() {
            this.nodes = new HashSet<>();
        }

        void addNode(Node node) {
            this.nodes.add(node);
        }

        Node getNode(int id) {
            for (Node search : this.nodes) {
                if (search.getID() == id) {
                    return search;
                }
            }
            return null;
        }

        boolean contains(int id) {
            for (Node search : this.nodes) {
                if (search.getID() == id) {
                    return true;
                }
            }
            return false;
        }

        void printShortestPaths(int sourceNode) {
            String base = "shortest path to node ";
            List<Node> nodeList = nodes.stream().sorted(Comparator.comparingInt(Node::getID)).collect(Collectors.toList());

            for (Node node : nodeList) {
                if (node.getID() != sourceNode) {
                    StringBuilder str = new StringBuilder();
                    if(node.getTotalCost() == MAX) {
                        str.append(node.getID()).append(" does not exist");
                    } else {
                        str.append(node.getID()).append(" is ");
                        List<Node> pathList = new ArrayList<>(node.shortestPath);
                        for (Node node3 : pathList) {
                            str.append(node3.getID()).append("->");
                        }
                        str.append(node.getID()).append(" with cost ").append(node.getTotalCost());
                    }
                    System.out.println(base + str);
                }
            }
        }
    }

    private void setupGraph() throws IOException, InputMismatchException {
        List<String> file = Files.readAllLines(filePath);
        file.remove(0);

        for (String line : file) {
            String[] parts = line.split(" ");
            int node1 = Integer.valueOf(parts[0]);
            int node2 = Integer.valueOf(parts[1]);
            float cost = Float.valueOf(parts[2]);

            if (this.graph.contains(node1)) {
                Node node_1 = this.graph.getNode(node1);
                if (this.graph.contains(node2)) {
                    node_1.addDest(this.graph.getNode(node2), cost);
                }
                else {
                    Node node_2 = new Node(node2);
                    node_1.addDest(node_2, cost);
                    this.graph.addNode(node_2);
                }
            }
            else {
                Node node_1 = new Node(node1);
                if (this.graph.contains(node2)) {
                    node_1.addDest(this.graph.getNode(node2), cost);
                }
                else {
                    Node node_2 = new Node(node2);
                    this.graph.addNode(node_2);
                    node_1.addDest(node_2, cost);
                }
                this.graph.addNode(node_1);
            }
        }
    }

    private Graph getGraph() {
        return this.graph;
    }

    private void calculateShortestPath(int sourceNode) {
        Node source = this.graph.getNode(sourceNode);
        source.setTotalCost(0.0f);
        Set<Node> doneChecking = new HashSet<>();
        Set<Node> needToCheck = new HashSet<>();
        needToCheck.add(source);

        while (needToCheck.size() != 0) {
            Node currentNode = getLowestNodeCost(needToCheck);
            needToCheck.remove(currentNode);

            for (Map.Entry<Node, Float> destAndCost : currentNode.getDest().entrySet()) {
                if (!doneChecking.contains(destAndCost.getKey())) {
                    calcMinCost(destAndCost, currentNode);
                    needToCheck.add(destAndCost.getKey());
                }
            }
            doneChecking.add(currentNode);
        }
    }

    private Node getLowestNodeCost(Set<Node> unsettledNodes) {
        Node lowestNodeCost = new Node(-1);
        lowestNodeCost.setTotalCost(MAX);
        for (Node node : unsettledNodes) {
            if (node.getTotalCost() < lowestNodeCost.getTotalCost())
                lowestNodeCost = node;
        }
        return lowestNodeCost;
    }

    private void calcMinCost(Map.Entry<Node, Float> destAndCost, Node sourceNode) {
        if (sourceNode.getTotalCost() + destAndCost.getValue() < destAndCost.getKey().getTotalCost()) {
            destAndCost.getKey().setTotalCost(sourceNode.getTotalCost() + destAndCost.getValue());
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            destAndCost.getKey().setShortestPath(shortestPath);
        }
    }

    public static void main(String[] args) {
        try {
            Path filePath = Paths.get(args[0]);
            int sourceNode = Integer.valueOf(args[1]);
            LinkState linkState = new LinkState(filePath);
            linkState.setupGraph();
            linkState.calculateShortestPath(sourceNode);
            linkState.getGraph().printShortestPaths(sourceNode);
        }
        catch (IOException ioe) {
            System.out.println("Invalid input.");
        }
    }
}