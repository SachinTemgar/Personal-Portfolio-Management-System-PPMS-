import java.util.Date;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class ManageAssetsPanel {
    private final PortfolioManager portfolio;
    private final HeapADT<Asset> heap;
    private final Runnable updateCallback;
    private final BorderPane pane;
    private TableView<Asset> assetTable;
    
    private StackADT<Asset> assetUndoStack = new ArrayStackImpl<>();

    public ManageAssetsPanel(PortfolioManager portfolio, HeapADT<Asset> heap, Runnable updateCallback) {
        this.portfolio = portfolio;
        this.heap = heap;
        this.updateCallback = updateCallback;
        this.pane = createManagePane();
    }

    public Pane getPane() {
        return pane;
    }

    private BorderPane createManagePane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        VBox inputSection = new VBox(10);
        Label titleLabel = new Label("Manage Assets");
        titleLabel.setFont(new Font("Arial", 16));

        TextField nameField = new TextField();
        nameField.setPromptText("Asset Name");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Stock", "Crypto", "Mutual Fund");
        typeBox.setPromptText("Asset Type");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Initial Price");

        Button addButton = new Button("Add Asset");
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String type = typeBox.getValue();
                double quantity = Double.parseDouble(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                if (name.isEmpty() || type == null || quantity <= 0 || price <= 0) {
                    throw new IllegalArgumentException("All fields must be valid.");
                }
                Asset asset = new Asset(name, type, price, quantity, new Date());
                portfolio.addAsset(asset);
                heap.add(asset);
                updateCallback.run();
                updateAssetTable();
                clearFields(nameField, quantityField, priceField);
            } catch (Exception ex) {
                showAlert("Error", "Invalid input: " + ex.getMessage());
            }
        });

        Button editButton = new Button("Edit Selected Asset");
        editButton.setOnAction(e -> editSelectedAsset(nameField, typeBox, quantityField, priceField));

        Button sortButton = new Button("Sort Assets");
        sortButton.setOnAction(e -> {
            ListADT<Asset> currentAssets = portfolio.getAssets();
            ListADT<Asset> sortedAssets = SortingUtil.mergeSort(currentAssets);
            assetTable.getItems().clear();
            for (int i = 0; i < sortedAssets.size(); i++) {
                assetTable.getItems().add(sortedAssets.get(i));
            }
        });
        
        Button undoDeleteButton = new Button("Undo Delete");
        undoDeleteButton.setOnAction(e -> {
            if (!assetUndoStack.isEmpty()) {
                Asset assetToRestore = assetUndoStack.pop();
                portfolio.addAsset(assetToRestore);
                updateCallback.run();
                updateAssetTable();
            } else {
                showAlert("Info", "No asset to undo deletion.");
            }
        });
        
        inputSection.getChildren().addAll(
            titleLabel, nameField, typeBox, quantityField, priceField, 
            addButton, editButton, sortButton, undoDeleteButton
        );

        assetTable = new TableView<>();
        TableColumn<Asset, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Asset, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Asset, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        TableColumn<Asset, Double> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Asset, Double> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        assetTable.getColumns().addAll(nameCol, typeCol, priceCol, quantityCol, valueCol);
        updateAssetTable();

        VBox actionSection = new VBox(10);
        Button deleteButton = new Button("Delete Selected Asset");
        deleteButton.setOnAction(e -> {
            Asset selected = assetTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                assetUndoStack.push(selected);
                portfolio.deleteAsset(selected.getName());
                updateCallback.run();
                updateAssetTable();
            } else {
                showAlert("Warning", "No asset selected.");
            }
        });

        Button deleteAllButton = new Button("Delete All Assets");
        deleteAllButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete all assets?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                ListADT<Asset> assets = portfolio.getAssets();
                for (int i = 0; i < assets.size(); i++) {
                    assetUndoStack.push(assets.get(i));
                }
                while (!portfolio.getAssets().isEmpty()) {
                    portfolio.deleteAsset(portfolio.getAssets().get(0).getName());
                }
                updateCallback.run();
                updateAssetTable();
            }
        });

        actionSection.getChildren().addAll(deleteButton, deleteAllButton);

        pane.setLeft(inputSection);
        pane.setCenter(assetTable);
        pane.setRight(actionSection);

        assetTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                nameField.setText(newValue.getName());
                typeBox.setValue(newValue.getType());
                quantityField.setText(String.valueOf(newValue.getQuantity()));
                priceField.setText(String.valueOf(newValue.getCurrentPrice()));
            }
        });

        return pane;
    }

    private void editSelectedAsset(TextField nameField, ComboBox<String> typeBox, TextField quantityField, TextField priceField) {
        try {
            String oldName = nameField.getText();
            String newType = typeBox.getValue();
            double newQuantity = Double.parseDouble(quantityField.getText());
            double newPrice = Double.parseDouble(priceField.getText());
            
            if (oldName.isEmpty() || newType == null || newQuantity <= 0 || newPrice <= 0) {
                throw new IllegalArgumentException("All fields must be valid.");
            }
            
            Asset oldAsset = null;
            ListADT<Asset> assets = portfolio.getAssets();
            for (int i = 0; i < assets.size(); i++) {
                if (assets.get(i).getName().equals(oldName)) {
                    oldAsset = assets.get(i);
                    break;
                }
            }
            
            if (oldAsset == null) {
                showAlert("Warning", "Asset not found.");
                return;
            }
            
            Asset updatedAsset = new Asset(oldName, newType, newPrice, newQuantity, new Date());
            portfolio.editAsset(oldAsset, updatedAsset);
            
            updateCallback.run();
            updateAssetTable();
            clearFields(nameField, quantityField, priceField);
            
        } catch (Exception ex) {
            showAlert("Error", "Edit failed: " + ex.getMessage());
        }
    }

    private void resetPriceHistory(TextField nameField) {
        String name = nameField.getText();
        ListADT<Asset> assets = portfolio.getAssets();
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            if (asset.getName().equals(name)) {
                asset.getPriceHistory().clear();
                asset.getPriceHistory().add(asset.getCurrentPrice());
                updateCallback.run();
                break;
            }
        }
    }

    private void updateAssetTable() {
        assetTable.getItems().clear();
        ListADT<Asset> assets = portfolio.getAssets();
        for (int i = 0; i < assets.size(); i++) {
            assetTable.getItems().add(assets.get(i));
        }
    }

    private void clearFields(TextField nameField, TextField quantityField, TextField priceField) {
        nameField.clear();
        quantityField.clear();
        priceField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
