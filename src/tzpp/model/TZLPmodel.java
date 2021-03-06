package tzpp.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import tzpp.controller.Common;
import tzpp.controller.MainController;
import tzpp.model.graphModel.Edge;
import tzpp.model.graphModel.Node;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class TZLPmodel {
    // таблиця вартостей
    private TableCell[][] tableTZLP;
    // Список вузлів, з яких виходять стрілки
    private ArrayList<Node> outputNodes;
    private ArrayList<Double> outputNodesResource;
    // Список вузлів, в які виходять стрілки
    private ArrayList<Node> inputNodes;
    private ArrayList<Double> inputNodesResource;

    private ArrayList<Node> transitNodes;

    private GraphicsContext gc;
    private Canvas canvas;
    private Integer size;

    public TZLPmodel(Canvas canvas, Integer size) {
        this.tableTZLP = null;
        this.outputNodes = new ArrayList<>();
        this.outputNodesResource = new ArrayList<>();
        this.inputNodes = new ArrayList<>();
        this.inputNodesResource = new ArrayList<>();
        this.gc = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        this.size = size;
    }

    Integer getSize() {
        return size;
    }

    TableCell[][] getTableTZLP() {
        return tableTZLP;
    }

    public void setTableTZLP(TableCell[][] tableTZLP) {
        this.tableTZLP = tableTZLP;
    }

    ArrayList<Node> getOutputNodes() {
        return outputNodes;
    }

    public void setOutputNodes(ArrayList<Node> outputNodes) {
        this.outputNodes = outputNodes;
    }

    ArrayList<Double> getOutputNodesResource() {
        return outputNodesResource;
    }

    public void setOutputNodesResource(ArrayList<Double> outputNodesResource) {
        this.outputNodesResource = outputNodesResource;
    }

    ArrayList<Node> getInputNodes() {
        return inputNodes;
    }

    public void setInputNodes(ArrayList<Node> inputNodes) {
        this.inputNodes = inputNodes;
    }

    ArrayList<Double> getInputNodesResource() {
        return inputNodesResource;
    }

    public void setInputNodesResource(ArrayList<Double> inputNodesResource) {
        this.inputNodesResource = inputNodesResource;
    }

    public ArrayList<Node> getTransitNodes() {
        return transitNodes;
    }

    public void setTransitNodes(ArrayList<Node> transitNodes) {
        this.transitNodes = transitNodes;
    }

    public GraphicsContext getGc() {
        return gc;
    }

    public void setGc(GraphicsContext gc) {
        this.gc = gc;
    }

    public void clear(){
        tableTZLP = null;
        outputNodes = new ArrayList<>();
        outputNodesResource = new ArrayList<>();
        inputNodes = new ArrayList<>();
        inputNodesResource = new ArrayList<>();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Створює ТЗЛП з ТЗПП
    public void createTZLP(ArrayList<Node> nodes, ArrayList<Edge> edges, Double buffer){
        // визначити пункти виробництва та пункти споживання
        Set<Node> productionNodes = new TreeSet<>();
        Set<Node> consumptionNodes = new TreeSet<>();
        for (Edge edge : edges) {
            productionNodes.add(edge.getFirstNode());
            consumptionNodes.add(edge.getSecondNode());
        }
        // визначити транзитні пункти
        Set<Node> transitionNodes = new TreeSet<>(productionNodes);
        transitionNodes.retainAll(consumptionNodes);
        // Збережемо в ТЗЛП
        outputNodes = new ArrayList<>(productionNodes);
        inputNodes = new ArrayList<>(consumptionNodes);
        transitNodes = new ArrayList<>(transitionNodes);
        // Знайдемо нові виробництва
        for (Node node : outputNodes){
            Double newResource = 0.0;
            if (node.getType() == Node.NodeType.OUTPUT)
            newResource += node.getResource().doubleValue();
            if (transitNodes.stream().anyMatch(n -> n.getId().equals(node.getId())))
                newResource += buffer;
            outputNodesResource.add(newResource);
        }
        // Знайдемо нові потреби
        inputNodes.forEach(node -> {
            Double newResource = 0.0;
            if (node.getType() == Node.NodeType.INPUT)
                newResource += node.getResource().doubleValue();
            if (transitNodes.stream().anyMatch(n -> n.getId().equals(node.getId())))
                newResource+=buffer;
                inputNodesResource.add(newResource);
        });

        // Знайдемо транспортні витрати
        Integer fieldWidth = inputNodes.size();
        Integer fieldHeight = outputNodes.size();
        tableTZLP = new TableCell[fieldHeight][fieldWidth];
        for (Integer i = 0; i < fieldHeight; i++) {
            Integer firstId = outputNodes.get(i).getId();
            for (Integer j = 0; j < fieldWidth; j++) {
                Integer secondId = inputNodes.get(j).getId();
                if (firstId.equals(secondId)) {
                    tableTZLP[i][j] = new TableCell(gc, Color.LIGHTYELLOW, 0.0);
                } else {
                    Double db = edges.stream().filter(e -> e.getFirstNode().getId().equals(firstId)
                            && e.getSecondNode().getId().equals(secondId)).findFirst()
                            .map(e -> e.getValue().doubleValue()).orElseGet(MainController::getM);
                    tableTZLP[i][j] = new TableCell(gc, Color.LIGHTYELLOW, db);
                }
            }
        }
    }

    // Малює заголовки рядків
    public void drawRowName() {
        int x = inputNodesResource.size()+1;
        int i = 1;
        Common.setGc(gc);
        for (Node node : outputNodes) {
            node.draw(gc, size, Color.LIGHTBLUE, 0, i);
            Common.drawCell(Color.LIGHTGREEN, x, i++, outputNodesResource.get(i-2).toString());
        }
    }

    // Малює заголовки стовпчиків
    public void drawColumnName() {
        int y = outputNodes.size()+1;
        int i = 1;
        Common.setGc(gc);
        for (Node node : inputNodes) {
            node.draw(gc, size, Color.LIGHTBLUE, i, 0);
            Common.drawCell(Color.LIGHTCORAL, i++, y, inputNodesResource.get(i-2).toString());
        }
    }

    // Малює матрицю вартостей (транспортних витрат)
    public void drawTable(){
        for (int i = 0; i < tableTZLP.length; i++) {
            for (int j = 0; j < tableTZLP[0].length; j++) {
                tableTZLP[i][j].draw(j+1, i+1, size, Color.LIGHTYELLOW);
            }
        }
    }

    public String typeListsToString() {
        String text = "Пункти виробництва: " + outputNodes.toString();
        text += "\nПункти споживання: " + inputNodes.toString();
        text += "\nПроміжні пункти: " + transitNodes.toString();
        return text;
    }



}
