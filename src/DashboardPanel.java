import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.Date;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import com.google.gson.Gson;

public class DashboardPanel {
    private final PortfolioManager portfolio;
    private final Runnable updateCallback;
    private final BorderPane pane;
    
    private TextArea summaryArea;
    private PieChart allocationChart;
    private Label goalProgressLabel;
    
    private Label tickerLabel;
    
    private static final String FINNHUB_API_KEY = "cvstuepr01qhup0t2av0cvstuepr01qhup0t2avg";
    
    private static final String[] TICKER_SYMBOLS = { 
        "AAPL", "GOOG", "MSFT", "AMZN", "META", "TSLA", "NFLX" 
    };
    
    private static final int MAX_TICKER_LENGTH = 200;
    
    private TranslateTransition tickerTransition = null;
    
    public DashboardPanel(PortfolioManager portfolio, Runnable updateCallback) {
        this.portfolio = portfolio;
        this.updateCallback = updateCallback;
        this.pane = createDashboardPane();
        updateTickerPriceAndAppend();
    }
    
    public Pane getPane() {
        return pane;
    }
    
    private BorderPane createDashboardPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        
        VBox summaryBox = new VBox(5);
        Label titleLabel = new Label("Portfolio Dashboard");
        titleLabel.setFont(new Font("Arial", 18));
        summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefHeight(150);
        summaryBox.getChildren().addAll(titleLabel, summaryArea);
        
        Pane tickerPane = buildTickerPane();
        
        VBox topSection = new VBox(10);
        topSection.getChildren().addAll(summaryBox, tickerPane);
        pane.setTop(topSection);
        
        allocationChart = new PieChart();
        allocationChart.setTitle("Asset Allocation");
        allocationChart.setPrefHeight(300);
        pane.setCenter(allocationChart);
        
        VBox bottomSection = new VBox(10);
        goalProgressLabel = new Label("Goal Progress: ");
        bottomSection.getChildren().addAll(goalProgressLabel);
        pane.setBottom(bottomSection);
        
        updateDashboard();
        return pane;
    }
    
    private Pane buildTickerPane() {
        VBox tickerContainer = new VBox(5);
        tickerContainer.setPadding(new Insets(5));
        tickerContainer.setStyle("-fx-background-color: #333333;");
        
        tickerLabel = new Label("Live Prices: Loading...");
        tickerLabel.setFont(new Font("Arial", 16));
        tickerLabel.setStyle("-fx-text-fill: white;");
        
        Button updateTickerBtn = new Button("Update Prices");
        updateTickerBtn.setOnAction(e -> updateTickerPriceAndAppend());
        
        tickerContainer.getChildren().addAll(tickerLabel, updateTickerBtn);
        
        tickerContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            startTickerScroll(tickerContainer);
        });
        
        return tickerContainer;
    }
    
    private void startTickerScroll(Pane container) {
        tickerLabel.applyCss();
        tickerLabel.layout();
        
        double containerWidth = container.getWidth();
        double textWidth = tickerLabel.getWidth();
        if (textWidth == 0) return;
        
        if (tickerTransition != null) {
            tickerTransition.stop();
        }
        
        tickerLabel.setTranslateX(containerWidth);
        
        double distance = containerWidth + textWidth;
        double speed = 50.0;
        double durationSeconds = distance / speed;
        
        tickerTransition = new TranslateTransition(Duration.seconds(durationSeconds), tickerLabel);
        tickerTransition.setFromX(containerWidth);
        tickerTransition.setToX(-textWidth);
        tickerTransition.setCycleCount(TranslateTransition.INDEFINITE);
        tickerTransition.play();
    }
    
    private void updateTickerPriceAndAppend() {
        new Thread(() -> {
            StringBuilder newData = new StringBuilder();
            for (String symbol : TICKER_SYMBOLS) {
                try {
                    QuoteResponse quote = fetchQuote(symbol);
                    newData.append(symbol)
                           .append(": ")
                           .append(String.format("%.2f", quote.c))
                           .append("    ");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    newData.append(symbol)
                           .append(": N/A    ");
                }
            }
            Platform.runLater(() -> {
                String currentText = tickerLabel.getText();
                if (currentText.startsWith("Live Prices: ")) {
                    currentText = currentText.substring(13);
                }
                String updatedText = currentText + " " + newData.toString();
                if (updatedText.length() > MAX_TICKER_LENGTH) {
                    updatedText = newData.toString();
                }
                tickerLabel.setText("Live Prices: " + updatedText);
                startTickerScroll((Pane)tickerLabel.getParent());
            });
        }).start();
    }
    
    private QuoteResponse fetchQuote(String symbol) throws Exception {
        String urlStr = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + FINNHUB_API_KEY;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Finnhub request failed, HTTP status: " + status);
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        Gson gson = new Gson();
        return gson.fromJson(response.toString(), QuoteResponse.class);
    }
    
    private static class QuoteResponse {
        public double c;
        public double d;
        public double dp;
        public double h;
        public double l;
        public double o;
        public double pc;
    }
    
    public void updateDashboard() {
        double totalValue = portfolio.getTotalValue();
        ListADT<Asset> assets = portfolio.getAssets();
        StringBuilder summary = new StringBuilder();
        summary.append("Total Portfolio Value: $").append(String.format("%.2f", totalValue)).append("\n");
        summary.append("Number of Assets: ").append(assets.size()).append("\n");
        double totalReturn = 0;
        for (int i = 0; i < assets.size(); i++) {
            totalReturn += assets.get(i).getReturn();
        }
        double avgReturn = assets.size() > 0 ? totalReturn / assets.size() : 0;
        summary.append("Average Return: ").append(String.format("%.2f%%", avgReturn)).append("\n");
        summary.append("\nAssets:\n");
        for (int i = 0; i < assets.size(); i++) {
            summary.append(assets.get(i).toString()).append("\n");
        }
        summaryArea.setText(summary.toString());
        
        allocationChart.getData().clear();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            allocationChart.getData().add(new PieChart.Data(
                asset.getName() + " ($" + String.format("%.2f", asset.getValue()) + ")", asset.getValue()));
        }
        
        ListADT<Goal> goals = portfolio.getGoals();
        StringBuilder goalText = new StringBuilder("Goal Progress:\n");
        for (int i = 0; i < goals.size(); i++) {
            Goal goal = goals.get(i);
            double progress = goal.progress(totalValue);
            goalText.append(goal.getName())
                    .append(": ")
                    .append(String.format("%.2f%%", progress))
                    .append(" (Target: $")
                    .append(String.format("%.2f", goal.getTargetValue()))
                    .append(")\n");
        }
        goalProgressLabel.setText(goalText.toString());
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
