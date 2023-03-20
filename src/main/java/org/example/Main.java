package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //long startTime = System.nanoTime();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(new File(args[0]));

        //Get distinct nodes to work with the values
        JsonNode nodesNode = rootNode.get("nodes");
        JsonNode elements = rootNode.get("elements");
        JsonNode values = rootNode.get("values");

        //Map the Nodes to a List, to iterate over it to generate a TreeSet for each Node
        List<TreeSet<Element>> prioNodes = createNodeList(nodesNode);

        //Create a Map for elementobjects to easier find them by ID and add the values to each element
        Map<Integer, Element> elementsMap = createElementMap(elements, values);

        //Add each element to the nodes that it has
        for (Element element : elementsMap.values()) {
            for (int nodeId : element.getNodes()) {
                prioNodes.get(nodeId).add(element);
            }
        }

        //Check if any neighbor is bigger than this element
        for (Element element : elementsMap.values()) {
            checkNeighbors(element, prioNodes);
        }

        prioNodes.removeIf(treeSet -> treeSet.size() == 0);
        TreeSet<Element> viewSpots = new TreeSet<>();

        //Adds the remaining elements to a new tree
        for (TreeSet<Element> set : prioNodes) {
            viewSpots.add(set.first());
        }

        List<Element> elementList = new ArrayList<>();
        viewSpots = (TreeSet<Element>) viewSpots.descendingSet();

        //Adds the first N elements of the tree to a list
        for (int i = 0; i < Integer.valueOf(args[1]); i++) {
            if (!viewSpots.isEmpty())
                elementList.add(viewSpots.pollFirst());
            else
                break;
        }

        //changes mapper to use the ignoring rule for nodes property of element
        mapper.addMixIn(Element.class, IgnoreNodesPropertyMixin.class);

        //generates a json String
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(elementList);

        System.out.println(json);

        /*long endTime = System.nanoTime();
        long executionTime = endTime - startTime;
        Duration duration = Duration.ofNanos(executionTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        long milliseconds = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis();

        String formattedTime = String.format("%d:%02d.%03d", minutes, seconds, milliseconds);
        System.out.println("Execution time: " + formattedTime);*/
    }

    private static List<TreeSet<Element>> createNodeList(JsonNode nodesNode) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Node> nodes = mapper.readValue(nodesNode.toString(), new TypeReference<List<Node>>() {
        });
        List<TreeSet<Element>> prioNodes = new ArrayList<>();

        for (Node node : nodes) {
            prioNodes.add(new TreeSet<>(new ElementValueComparator()));
        }

        return prioNodes;
    }

    private static Map<Integer, Element> createElementMap(JsonNode elements, JsonNode values) {
        ObjectMapper mapper = new ObjectMapper();
        Map<Integer, Element> elementsMap = new HashMap<>();

        for (JsonNode element : elements) {
            int id = element.get("id").asInt();
            elementsMap.put(id, mapper.convertValue(element, Element.class));
        }

        for (JsonNode value : values) {
            int id = value.get("element_id").asInt();
            double eleValue = value.get("value").asDouble();
            elementsMap.get(id).setValue(eleValue);
        }

        return elementsMap;
    }

    /***
     *
     * @param element Element to check
     * @param prioNodes List of Nodes to check
     *
     * This function gets an element and iterate over all nodes it has, to check, if the element is always on the first place (the element is the greatest spot in its neighborhood)
     * If not the element is removed from all nodes
     */
    private static void checkNeighbors(Element element, List<TreeSet<Element>> prioNodes) {
        for (int node : element.getNodes()) {
            if (prioNodes.get(node).descendingSet().first() != element) {
                removeElementFromTree(element, prioNodes);
                break;
            }
        }
    }

    /***
     *
     * @param element Element to check
     * @param prioNodes List of Nodes to remove the element from
     *
     * This function removes an element from all nodes
     */
    private static void removeElementFromTree(Element element, List<TreeSet<Element>> prioNodes) {
        for (int node : element.getNodes()) {
            prioNodes.get(node).remove(element);
        }
    }

    //Class to ignore nodes in the json output
    static abstract class IgnoreNodesPropertyMixin {
        @JsonIgnore
        abstract String getNodes();
    }
}