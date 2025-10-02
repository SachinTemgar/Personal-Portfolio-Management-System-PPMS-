import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LivePricePanel {

    private BorderPane mainPane;
    private TextField symbolField;
    private ComboBox<String> typeCombo;
    private Label resultLabel;
    private static final String FINNHUB_API_KEY = "cvstuepr01qhup0t2av0cvstuepr01qhup0t2avg";

    public LivePricePanel() {
        this.mainPane = buildPanel();
    }

    public Pane getPane() {
        return mainPane;
    }

    private BorderPane buildPanel() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: #f8f8f8;");
        Label title = new Label("Live Asset Price");
        title.setFont(new Font("Arial", 20));
        pane.setTop(title);
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label instruction = new Label("Enter symbol and select asset type, then click 'Fetch Price':");
        symbolField = new TextField();
        symbolField.setPromptText("Symbol (e.g., AAPL, BTCUSD, etc.)");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Stock", "Crypto", "Mutual Fund");
        typeCombo.setPromptText("Select Asset Type");
        Button fetchButton = new Button("Fetch Price");
        fetchButton.setOnAction(e -> fetchAndDisplayPrice());
        resultLabel = new Label("Price info will appear here.");
        resultLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;");
        resultLabel.setWrapText(true);
        centerBox.getChildren().addAll(instruction, symbolField, typeCombo, fetchButton, resultLabel);
        pane.setCenter(centerBox);
        return pane;
    }

    private void fetchAndDisplayPrice() {
        String symbol = symbolField.getText().trim();
        String assetType = typeCombo.getValue();
        if (symbol.isEmpty()) {
            resultLabel.setText("Please enter a symbol.");
            return;
        }
        if (assetType == null) {
            resultLabel.setText("Please select an asset type (Stock, Crypto, or Mutual Fund).");
            return;
        }
        try {
            QuoteResponse quote = fetchQuote(symbol);
            if (quote == null) {
                resultLabel.setText("No data returned for symbol: " + symbol);
            } else {
                resultLabel.setText(String.format(
                    "Type: %s\nSymbol: %s\nCurrent Price: %.2f\nChange: %.2f\nPercent Change: %.2f%%",
                    assetType, symbol, quote.c, quote.d, quote.dp
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Error fetching price for " + symbol + ": " + e.getMessage());
        }
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
}
