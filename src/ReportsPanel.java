import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class ReportsPanel {
    private final PortfolioManager portfolio;
    private final Runnable updateCallback;
    private final VBox pane;

    public ReportsPanel(PortfolioManager portfolio, Runnable updateCallback) {
        this.portfolio = portfolio;
        this.updateCallback = updateCallback;
        this.pane = createReportsPane();
    }

    public Pane getPane() {
        return pane;
    }

    private VBox createReportsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label titleLabel = new Label("Reports & Rebalancing");
        titleLabel.setFont(new Font("Arial", 16));

        
        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> {
            try {
                exportPortfolioToCSV("portfolio.csv");
                showAlert("Success", "Portfolio exported to portfolio.csv");
            } catch (IOException ex) {
                showAlert("Error", "Export failed: " + ex.getMessage());
            }
        });

        
        TextField goalField = new TextField();
        goalField.setPromptText("Target Allocation Value");
        Button rebalanceButton = new Button("Rebalance Portfolio");
        rebalanceButton.setOnAction(e -> {
            try {
                double goal = Double.parseDouble(goalField.getText());
               
                rebalancePortfolio(goal);
                updateCallback.run();
            } catch (Exception ex) {
                showAlert("Error", "Invalid goal value: " + ex.getMessage());
            }
        });

        pane.getChildren().addAll(titleLabel, exportButton, goalField, rebalanceButton);
        return pane;
    }

 
    private void exportPortfolioToCSV(String fileName) throws IOException {
      
        FileWriter writer = new FileWriter(fileName, false);
        
      
        writer.append("Asset Name,Asset Type,Current Price,Quantity,Value\n");
        ListADT<Asset> assets = portfolio.getAssets();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            writer.append(asset.getName()).append(",");
            writer.append(asset.getType()).append(",");
            writer.append(String.format("%.2f", asset.getCurrentPrice())).append(",");
            writer.append(String.valueOf(asset.getQuantity())).append(",");
            writer.append(String.format("%.2f", asset.getValue())).append("\n");
        }
        
       
        writer.append("\nGoal Name,Target Value,Deadline\n");
        ListADT<Goal> goals = portfolio.getGoals();
        for (int i = 0; i < goals.size(); i++) {
            Goal goal = goals.get(i);
            writer.append(goal.getName()).append(",");
            writer.append(String.format("%.2f", goal.getTargetValue())).append(",");
          
            writer.append(goal.getDeadline().toString()).append("\n");
        }
        writer.flush();
        writer.close();
    }

    
    public void rebalancePortfolio(double targetPortfolioValue) {
      
        double currentTotalValue = portfolio.getTotalValue();
       
        ListADT<Asset> assets = portfolio.getAssets();
        int n = assets.size();
        if (n == 0) {
            showAlert("Info", "No assets to rebalance.");
            return;
        }
        
        double targetAssetValue = targetPortfolioValue / n;
        StringBuilder suggestions = new StringBuilder("Rebalance Suggestions:\n");
        for (int i = 0; i < n; i++) {
            Asset asset = assets.get(i);
            double currentAssetValue = asset.getValue();
            double diff = currentAssetValue - targetAssetValue;
            if (diff > 0) {
                suggestions.append(asset.getName())
                           .append(": Sell $")
                           .append(String.format("%.2f", diff))
                           .append("\n");
            } else if (diff < 0) {
                suggestions.append(asset.getName())
                           .append(": Buy $")
                           .append(String.format("%.2f", -diff))
                           .append("\n");
            } else {
                suggestions.append(asset.getName())
                           .append(": Balanced\n");
            }
        }
        
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rebalance Suggestions");
        alert.setHeaderText("Target Portfolio Value: $" + String.format("%.2f", targetPortfolioValue));
        alert.setContentText(suggestions.toString());
        alert.showAndWait();
    }
    
   
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
