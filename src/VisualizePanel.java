import java.util.List;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class VisualizePanel {
    private final PortfolioManager portfolio;
    private final HeapADT<Asset> heap;
    private final GraphADT graph;
    
    private final SplitPane pane;
    private TextArea heapDetails;
    private TextArea correlationDetails;
    
    public VisualizePanel(PortfolioManager portfolio, HeapADT<Asset> heap, GraphADT graph) {
        this.portfolio = portfolio;
        this.heap = heap;
        this.graph = graph;
        this.pane = createVisualizePane();
    }
    
    public SplitPane getPane() {
        return pane;
    }
    
    private SplitPane createVisualizePane() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.5);
        split.setPadding(new Insets(10));
        
        VBox heapSection = buildHeapSection();
        VBox correlationSection = buildCorrelationSection();
        
        split.getItems().addAll(heapSection, correlationSection);
        
        BarChart<String, Number> riskChart = (BarChart<String, Number>) heapSection.getChildren().get(1);
        updateRiskChart(riskChart, "5");
        
        return split;
    }
    
    private VBox buildHeapSection() {
        VBox heapSection = new VBox(10);
        Label heapLabel = new Label("Risk Analysis (Heap)");
        heapLabel.setFont(new Font("Arial", 16));
        
        BarChart<String, Number> riskChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        riskChart.setTitle("Top Riskiest Assets (Volatility)");
        riskChart.setPrefHeight(300);
        
        TextField topNField = new TextField();
        topNField.setPromptText("Top N Assets (e.g., 5)");
        Button showHeapButton = new Button("Show Riskiest Assets");
        showHeapButton.setOnAction(e -> updateRiskChart(riskChart, topNField.getText()));
        
        heapDetails = new TextArea();
        heapDetails.setEditable(false);
        heapDetails.setPrefHeight(150);
        
        heapSection.getChildren().addAll(heapLabel, riskChart, topNField, showHeapButton, heapDetails);
        return heapSection;
    }
    
    private VBox buildCorrelationSection() {
        VBox corrSection = new VBox(10);
        Label label = new Label("Correlation Heatmap");
        label.setFont(new Font("Arial", 16));
        
        ScrollPane matrixScroll = new ScrollPane();
        matrixScroll.setPrefHeight(300);
        matrixScroll.setStyle("-fx-border-color: black;");
        
        Button showMatrixButton = new Button("Show Correlation Matrix");
        showMatrixButton.setOnAction(e -> {
            GridPane matrix = buildCorrelationMatrix();
            matrixScroll.setContent(matrix);
        });
        
        Button buildGraphButton = new Button("Build Correlation Graph");
        buildGraphButton.setOnAction(e -> {
            updateCorrelations();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Graph Updated");
            alert.setContentText("Graph now has edges for assets with correlation > 0.5");
            alert.showAndWait();
        });
        
        correlationDetails = new TextArea();
        correlationDetails.setEditable(false);
        correlationDetails.setPrefHeight(150);
        correlationDetails.setText("Click 'Show Correlation Matrix' to view asset correlations.\n" +
                "Click 'Build Correlation Graph' to store them in the adjacency structure.");
        
        corrSection.getChildren().addAll(label, matrixScroll, showMatrixButton, buildGraphButton, correlationDetails);
        return corrSection;
    }
    
    private void updateRiskChart(BarChart<String, Number> chart, String topNText) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Volatility");
        
        int topN;
        try {
            topN = topNText.isEmpty() ? 5 : Integer.parseInt(topNText);
            if (topN <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            topN = 5;
        }
        
        ListADT<Asset> assets = portfolio.getAssets();
        HeapADT<Asset> tempHeap = new MaxHeapImpl<>();
        for (int i = 0; i < assets.size(); i++) {
            tempHeap.add(assets.get(i));
        }
        
        StringBuilder details = new StringBuilder("Riskiest Assets:\n");
        
        for (int i = 0; i < Math.min(topN, assets.size()); i++) {
            Asset asset = tempHeap.remove();
            if (asset != null) {
                double vol = asset.getVolatility();
                double drawdown = calculateMaxDrawdown(asset);
                
                XYChart.Data<String, Number> barData = new XYChart.Data<>(asset.getName(), vol);
                barData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        String barColor = pickBarColor(vol);
                        newNode.setStyle("-fx-bar-fill: " + barColor + ";");
                        
                        Tooltip tooltip = new Tooltip(String.format("Asset: %s\nVolatility: %.2f\nMax Drawdown: %.2f%%", asset.getName(), vol, drawdown));
                        Tooltip.install(newNode, tooltip);
                        
                        newNode.setScaleY(0);
                        ScaleTransition st = new ScaleTransition(Duration.millis(800), newNode);
                        st.setFromY(0);
                        st.setToY(1);
                        st.play();
                        
                        if (newNode.getParent() != null && newNode.getParent() instanceof Pane) {
                            Label valLabel = new Label(String.format("%.2f", vol));
                            valLabel.setStyle("-fx-font-size: 10; -fx-text-fill: black;");
                            valLabel.layoutXProperty().bind(newNode.layoutXProperty());
                            valLabel.layoutYProperty().bind(newNode.layoutYProperty().subtract(10));
                            ((Pane)newNode.getParent()).getChildren().add(valLabel);
                        }
                    }
                });
                
                series.getData().add(barData);
                details.append(String.format("%s: Volatility %.2f, Max Drawdown %.2f%%\n", asset.getName(), vol, drawdown));
            }
        }
        
        chart.getData().add(series);
        heapDetails.setText(details.toString());
    }
    
    private String pickBarColor(double volatility) {
        if (volatility < 20)
            return "green";
        else if (volatility < 40)
            return "orange";
        else
            return "red";
    }
    
    private double calculateMaxDrawdown(Asset asset) {
        List<Double> history = asset.getPriceHistory();
        if (history.size() < 2)
            return 0;
        double peak = history.get(0);
        double trough = peak;
        double maxDrawdown = 0;
        for (double price : history) {
            if (price > peak)
                peak = price;
            else if (price < trough)
                trough = price;
            double drawdown = (peak - trough) / peak * 100;
            if (drawdown > maxDrawdown)
                maxDrawdown = drawdown;
        }
        return maxDrawdown;
    }
    
    private GridPane buildCorrelationMatrix() {
        correlationDetails.clear();
        
        GridPane matrix = new GridPane();
        matrix.setHgap(5);
        matrix.setVgap(5);
        
        ListADT<Asset> assets = portfolio.getAssets();
        int n = assets.size();
        
        for (int col = 0; col < n; col++) {
            Label colLabel = new Label(assets.get(col).getName());
            colLabel.setStyle("-fx-font-weight: bold;");
            matrix.add(colLabel, col + 1, 0);
        }
        
        for (int row = 0; row < n; row++) {
            Label rowLabel = new Label(assets.get(row).getName());
            rowLabel.setStyle("-fx-font-weight: bold;");
            matrix.add(rowLabel, 0, row + 1);
        }
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Asset a1 = assets.get(row);
                Asset a2 = assets.get(col);
                double corr = computeCorrelation(a1, a2);
                
                Label cell = new Label(String.format("%.2f", corr));
                cell.setPrefWidth(60);
                cell.setPrefHeight(30);
                cell.setStyle("-fx-alignment: center;");
                
                String bgStyle = String.format("-fx-background-color: %s;", toRgbString(correlationToColor(corr)));
                cell.setStyle(cell.getStyle() + bgStyle);
                
                matrix.add(cell, col + 1, row + 1);
            }
        }
        correlationDetails.setText("Correlation matrix built for " + n + " assets.");
        return matrix;
    }
    
    private double computeCorrelation(Asset a1, Asset a2) {
        List<Double> h1 = a1.getPriceHistory();
        List<Double> h2 = a2.getPriceHistory();
        int size = Math.min(h1.size(), h2.size());
        if (size < 2)
            return 0;
        double[] x = new double[size];
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = h1.get(i);
            y[i] = h2.get(i);
        }
        double meanX = 0, meanY = 0;
        for (int i = 0; i < size; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= size;
        meanY /= size;
        double numerator = 0, denomX = 0, denomY = 0;
        for (int i = 0; i < size; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            numerator += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }
        double denominator = Math.sqrt(denomX * denomY);
        if (denominator == 0)
            return 0;
        return numerator / denominator;
    }
    
    private Color correlationToColor(double corr) {
        double fraction = (corr + 1) / 2.0;
        return Color.RED.interpolate(Color.GREEN, fraction);
    }
    
    private String toRgbString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }
    
    private void updateCorrelations() {
        ListADT<Asset> assets = portfolio.getAssets();
        graph.clear();
        for (int i = 0; i < assets.size(); i++) {
            graph.addVertex(assets.get(i).getName());
        }
        for (int i = 0; i < assets.size(); i++) {
            for (int j = i + 1; j < assets.size(); j++) {
                Asset a1 = assets.get(i);
                Asset a2 = assets.get(j);
                double corr = computeCorrelation(a1, a2);
                if (corr > 0.5) {
                    graph.addEdge(a1.getName(), a2.getName());
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Graph updated with edges for correlation > 0.5.\n");
        sb.append("Total Vertices: ").append(assets.size()).append("\n\n");
        for (int i = 0; i < assets.size(); i++) {
            String assetName = assets.get(i).getName();
            List<String> neighbors = graph.getAdjacentVertices(assetName);
            sb.append(assetName).append(" -> ");
            if (neighbors.isEmpty()) {
                sb.append("(No edges)\n");
            } else {
                sb.append(String.join(", ", neighbors)).append("\n");
            }
        }
        correlationDetails.setText(sb.toString());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.showAndWait();
    }
}
