import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class WatchlistPanel {

    private final PortfolioManager portfolio;
    private final Runnable updateCallback;

    private BorderPane mainPane;
    private TableView<Asset> watchlistTable;
    private TextArea newsArea;
    private TextField filterField;

    private static final String FINNHUB_API_KEY = "cvstuepr01qhup0t2av0cvstuepr01qhup0t2avg";

    public WatchlistPanel(PortfolioManager portfolio, Runnable updateCallback) {
        this.portfolio = portfolio;
        this.updateCallback = updateCallback;
        this.mainPane = buildMainPane();
    }

    public Pane getPane() {
        return mainPane;
    }

    private BorderPane buildMainPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: #f8f8f8;");

        TitledPane inputPane = buildInputSection();
        inputPane.setCollapsible(false);

        VBox watchlistCard = buildWatchlistSection();
        VBox newsCard = buildNewsSection();

        pane.setLeft(inputPane);
        pane.setCenter(watchlistCard);
        pane.setRight(newsCard);

        BorderPane.setMargin(inputPane, new Insets(0, 10, 0, 0));
        BorderPane.setMargin(watchlistCard, new Insets(0, 10, 0, 10));
        BorderPane.setMargin(newsCard, new Insets(0, 0, 0, 10));

        return pane;
    }

    private TitledPane buildInputSection() {
        VBox inputContent = new VBox(10);
        inputContent.setPadding(new Insets(10));

        Label titleLabel = new Label("Add Asset to Watchlist");
        titleLabel.setFont(new Font("Arial", 16));

        TextField nameField = new TextField();
        nameField.setPromptText("Symbol or Asset Name (e.g., AAPL)");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Stock", "Crypto", "Mutual Fund");
        typeBox.setPromptText("Select Type");

        TextField priceField = new TextField();
        priceField.setPromptText("Initial Price");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String type = typeBox.getValue();
                double price = Double.parseDouble(priceField.getText());
                if (name.isEmpty() || type == null || price <= 0) {
                    throw new IllegalArgumentException("All fields must be valid.");
                }
                Asset asset = new Asset(name, type, price, 0, new Date());
                portfolio.addToWatchlist(asset);

                animateAssetAddition(asset);
                updateWatchlistTable();
                updateNewsArea(name);

                nameField.clear();
                priceField.clear();
            } catch (Exception ex) {
                showAlert("Error", "Invalid input: " + ex.getMessage());
            }
        });

        filterField = new TextField();
        filterField.setPromptText("Filter watchlist (by name)...");
        filterField.textProperty().addListener((obs, oldVal, newVal) -> filterWatchlist(newVal));

        inputContent.getChildren().addAll(
            titleLabel, nameField, typeBox, priceField, addButton,
            new Label("Search:"),
            filterField
        );

        TitledPane tPane = new TitledPane("Manage Watchlist", inputContent);
        tPane.setExpanded(true);
        tPane.setStyle("-fx-font-weight: bold; -fx-background-color: #ffffff;"
                     + "-fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8;");
        return tPane;
    }

    private VBox buildWatchlistSection() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd;"
                    + "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label tableLabel = new Label("Current Watchlist");
        tableLabel.setFont(new Font("Arial", 16));

        watchlistTable = new TableView<>();
        watchlistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Asset, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Asset, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Asset, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        watchlistTable.getColumns().addAll(nameCol, typeCol, priceCol);
        watchlistTable.setPlaceholder(new Label("No assets in watchlist"));

        updateWatchlistTable();

        Button removeBtn = new Button("Remove Selected");
        removeBtn.setOnAction(e -> {
            Asset selected = watchlistTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                portfolio.removeFromWatchlist(selected.getName());
                updateWatchlistTable();
                newsArea.clear();
            } else {
                showAlert("Warning", "No asset selected to remove.");
            }
        });

        card.getChildren().addAll(tableLabel, watchlistTable, removeBtn);
        return card;
    }

    private VBox buildNewsSection() {
        VBox newsCard = new VBox(10);
        newsCard.setPadding(new Insets(10));
        newsCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd;"
                         + "-fx-border-radius: 8; -fx-background-radius: 8;");
        newsCard.setPrefWidth(420);

        Label newsLabel = new Label("Latest News");
        newsLabel.setFont(new Font("Arial", 16));

        newsArea = new TextArea();
        newsArea.setEditable(false);
        newsArea.setPrefHeight(400);
        newsArea.setWrapText(true);

        newsCard.getChildren().addAll(newsLabel, newsArea);
        return newsCard;
    }

    private void updateWatchlistTable() {
        watchlistTable.getItems().clear();
        ListADT<Asset> watchlist = portfolio.getWatchlist();
        for (int i = 0; i < watchlist.size(); i++) {
            watchlistTable.getItems().add(watchlist.get(i));
        }
    }

    private void animateAssetAddition(Asset asset) {
        ScaleTransition st = new ScaleTransition(Duration.millis(400), watchlistTable);
        st.setFromX(0.95);
        st.setToX(1.0);
        st.setFromY(0.95);
        st.setToY(1.0);
        st.play();

        FadeTransition ft = new FadeTransition(Duration.millis(400), watchlistTable);
        ft.setFromValue(0.7);
        ft.setToValue(1.0);
        ft.play();
    }

    private void filterWatchlist(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateWatchlistTable();
            return;
        }
        query = query.toLowerCase();

        watchlistTable.getItems().clear();
        ListADT<Asset> watchlist = portfolio.getWatchlist();
        for (int i = 0; i < watchlist.size(); i++) {
            Asset asset = watchlist.get(i);
            if (asset.getName().toLowerCase().contains(query)) {
                watchlistTable.getItems().add(asset);
            }
        }
    }

    private void updateNewsArea(String assetName) {
        try {
            
            long twoWeeksMillis = 14L * 24 * 60 * 60 * 1000;
            Date fromDateObj = new Date(System.currentTimeMillis() - twoWeeksMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String from = sdf.format(fromDateObj);
            String to = sdf.format(new Date());

            
            System.out.println("Fetching news for: " + assetName);
            System.out.println("From: " + from + ", To: " + to);

           
            String json = fetchCompanyNews(assetName, from, to, FINNHUB_API_KEY);
            System.out.println("Received JSON: " + json);  
            
            if (json == null || json.isEmpty()) {
                newsArea.setText("No news found or error for: " + assetName);
                return;
            }

            Gson gson = new Gson();
            FinnhubNewsArticle[] articles = gson.fromJson(json, FinnhubNewsArticle[].class);

            if (articles == null || articles.length == 0) {
                newsArea.setText("No recent news for " + assetName + ".");
                return;
            }

            StringBuilder sb = new StringBuilder("Recent News for " + assetName + ":\n");
            for (int i = 0; i < articles.length && i < 5; i++) {
                FinnhubNewsArticle art = articles[i];
                Date dateObj = new Date(art.datetime * 1000L);
                sb.append(String.format(
                    "\nHeadline: %s\nDate: %s\nSource: %s\nURL: %s\n",
                    art.headline,
                    sdf.format(dateObj),
                    art.source,
                    art.url
                ));
            }
            newsArea.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            newsArea.setText("Failed to fetch news for " + assetName + ": " + e.getMessage());
        }
    }

    private String fetchCompanyNews(String symbol, String from, String to, String apiKey) throws Exception {
        String urlStr = String.format(
            "https://finnhub.io/api/v1/company-news?symbol=%s&from=%s&to=%s&token=%s",
            symbol, from, to, apiKey
        );
        
        System.out.println("Request URL: " + urlStr); 

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Finnhub request failed with status: " + status);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        return response.toString();
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void openLinkInBrowser(String urlString) {
        try {
            Desktop.getDesktop().browse(new URI(urlString));
        } catch (Exception e) {
            showAlert("Open Link Error", e.getMessage());
        }
    }

   
    private static class FinnhubNewsArticle {
        public String category;
        public long datetime;
        public String headline;
        public String id;
        @SerializedName("image")
        public String imageUrl;
        public String related;
        public String source;
        public String summary;
        public String url;
    }
}
