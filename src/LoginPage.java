import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Page");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        TextField passwordTextField = new TextField();
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        ToggleButton toggleButton = new ToggleButton("Show");
        toggleButton.setOnAction(e -> {
            if (toggleButton.isSelected()) {
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                passwordTextField.setVisible(true);
                passwordTextField.setManaged(true);
                toggleButton.setText("Hide");
            } else {
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
                toggleButton.setText("Show");
            }
        });
        Button loginButton = new Button("Login");
        Label messageLabel = new Label();
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(passwordTextField, 1, 1);
        grid.add(toggleButton, 2, 1);
        grid.add(loginButton, 1, 2);
        grid.add(messageLabel, 1, 3);
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if ("admin".equals(username) && "admin".equals(password)) {
                messageLabel.setText("Admin login successful!");
                Session.setCurrentUser(username);
                Platform.runLater(() -> {
                    try {
                        new AdminPage().start(new Stage());
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else if (validateLogin(username, password)) {
                messageLabel.setText("Login successful!");
                Session.setCurrentUser(username);
                Platform.runLater(() -> {
                    try {
                        new PPMSApp().start(new Stage());
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        });
        Scene scene = new Scene(grid, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean validateLogin(String username, String password) {
        try {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("user");
            Document query = new Document("username", username)
                                .append("password", password);
            Document userDoc = collection.find(query).first();
            return userDoc != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
