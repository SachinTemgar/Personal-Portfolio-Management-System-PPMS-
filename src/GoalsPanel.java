import java.time.ZoneOffset;
import java.util.Date;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class GoalsPanel {
    private final PortfolioManager portfolio;
    private final Runnable updateCallback;
    private final VBox pane;

    private TableView<Goal> goalsTable;

    public GoalsPanel(PortfolioManager portfolio, Runnable updateCallback) {
        this.portfolio = portfolio;
        this.updateCallback = updateCallback;
        this.pane = createGoalsPane();
    }

    public Pane getPane() {
        return pane;
    }

    private VBox createGoalsPane() {
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Investment Goals");
        titleLabel.setFont(new Font("Arial", 16));

        TextField goalNameField = new TextField();
        goalNameField.setPromptText("Goal Name");

        TextField targetField = new TextField();
        targetField.setPromptText("Target Value");

        DatePicker deadlinePicker = new DatePicker();

        Button addGoalButton = new Button("Add Goal");
        addGoalButton.setOnAction(e -> {
            try {
                String name = goalNameField.getText().trim();
                double target = Double.parseDouble(targetField.getText());
                Date deadline = Date.from(
                    deadlinePicker.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)
                );
                portfolio.addGoal(name, target, deadline);
                updateCallback.run();
                goalNameField.clear();
                targetField.clear();
            } catch (Exception ex) {
                showAlert("Error", "Invalid goal input: " + ex.getMessage());
            }
        });

        goalsTable = new TableView<>();
        goalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Goal, String> nameCol = new TableColumn<>("Goal Name");
        nameCol.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getName());
        });

        TableColumn<Goal, Number> targetCol = new TableColumn<>("Target Value");
        targetCol.setCellValueFactory(cellData -> {
            return new SimpleDoubleProperty(cellData.getValue().getTargetValue());
        });

        TableColumn<Goal, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(cellData -> {
            Date d = cellData.getValue().getDeadline();
            return new SimpleStringProperty(d != null ? d.toString() : "N/A");
        });

        TableColumn<Goal, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cellData -> {
            double totalValue = portfolio.getTotalValue();
            double progress = cellData.getValue().progress(totalValue);
            String progressText = String.format("%.2f%%", progress);
            return new SimpleStringProperty(progressText);
        });

        goalsTable.getColumns().addAll(nameCol, targetCol, deadlineCol, progressCol);

        Button showGoalsButton = new Button("Show Goals");
        showGoalsButton.setOnAction(e -> refreshGoalsTable());

        mainBox.getChildren().addAll(
            titleLabel,
            goalNameField, targetField, deadlinePicker,
            addGoalButton,
            goalsTable,
            showGoalsButton
        );
        return mainBox;
    }

    private void refreshGoalsTable() {
        goalsTable.getItems().clear();
        ListADT<Goal> goals = portfolio.getGoals();
        for (int i = 0; i < goals.size(); i++) {
            goalsTable.getItems().add(goals.get(i));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
