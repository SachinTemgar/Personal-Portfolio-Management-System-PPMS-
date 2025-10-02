import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class AdminPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Page - Create New User");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        Label usernameLabel = new Label("New Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("New Password:");
        PasswordField passwordField = new PasswordField();
        Button createUserButton = new Button("Create User");
        Label messageLabel = new Label();

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            primaryStage.close();
            new LoginPage().start(new Stage());
        });

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(createUserButton, 1, 2);
        grid.add(messageLabel, 1, 3);

        BorderPane root = new BorderPane();
        root.setCenter(grid);
        root.setBottom(logoutButton);
        BorderPane.setMargin(logoutButton, new Insets(10));

        createUserButton.setOnAction(e -> {
            String newUsername = usernameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
            } else {
                try {
                    createNewUser(newUsername, newPassword);
                    messageLabel.setText("User created successfully!");
                    usernameField.clear();
                    passwordField.clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Error: " + ex.getMessage());
                }
            }
        });

        Scene scene = new Scene(root, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createNewUser(String username, String password) throws Exception {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> usersCollection = db.getCollection("user");

        Document userDoc = new Document("username", username)
                .append("password", password);

        Document query = new Document("username", username);
        if (usersCollection.find(query).first() != null) {
            throw new Exception("User already exists.");
        }

        usersCollection.insertOne(userDoc);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
