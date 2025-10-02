import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;


public class PPMSApp extends Application {
    private PortfolioManager portfolio = new PortfolioManager();
    private final HeapADT<Asset> heap = new MaxHeapImpl<>();
    private final GraphADT graph = new AdjacencyListGraph();
    private PriceSimulator simulator;
    private DashboardPanel dashboardPanel;

    @Override
    public void start(Stage primaryStage) {

        String loggedInUser = Session.getCurrentUser();
        System.out.println("Logged in as: " + loggedInUser);
        
        
        portfolio.loadPortfolioForUser(loggedInUser);

        
        TabPane tabPane = new TabPane();
        simulator = new PriceSimulator(portfolio);

       
        dashboardPanel = new DashboardPanel(portfolio, this::updateAllPanels);
        ManageAssetsPanel managePanel = new ManageAssetsPanel(portfolio, heap, this::updateAllPanels);
        VisualizePanel visualizePanel = new VisualizePanel(portfolio, heap, graph);
        GoalsPanel goalsPanel = new GoalsPanel(portfolio, this::updateAllPanels);
        ReportsPanel reportsPanel = new ReportsPanel(portfolio, this::updateAllPanels);
        WatchlistPanel watchlistPanel = new WatchlistPanel(portfolio, this::updateAllPanels);

       
        Tab dashboardTab = new Tab("Dashboard", dashboardPanel.getPane());
        Tab manageTab = new Tab("Manage Assets", managePanel.getPane());
        Tab visualizeTab = new Tab("Visualize", visualizePanel.getPane());
        Tab goalsTab = new Tab("Goals", goalsPanel.getPane());
        Tab reportsTab = new Tab("Reports", reportsPanel.getPane());
        Tab watchlistTab = new Tab("Watchlist", watchlistPanel.getPane());
        Tab livePriceTab = new Tab("Live Price", new LivePricePanel().getPane());
      


        tabPane.getTabs().addAll(dashboardTab, manageTab, visualizeTab, goalsTab, reportsTab, watchlistTab,livePriceTab);
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            
            Session.setCurrentUser(null);
            primaryStage.close();
            new LoginPage().start(new Stage());
        });

        
        BorderPane topBar = new BorderPane();
        topBar.setRight(logoutButton);

       
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(tabPane);
        
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("Advanced PPMS");
        primaryStage.setScene(scene);
        primaryStage.show();

        simulator.startSimulation(() -> Platform.runLater(this::updateAllPanels));
    }

    private void updateAllPanels() {
        dashboardPanel.updateDashboard();
       
    }

    public static void main(String[] args) {
        launch(args);
    }
}
