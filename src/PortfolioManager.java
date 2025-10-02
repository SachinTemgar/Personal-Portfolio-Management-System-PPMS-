import java.io.IOException;
import java.util.Date;
import java.util.List;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

public class PortfolioManager {
    private ListADT<Asset> assets = new ArrayListImpl<>();
    private ListADT<Goal> goals = new ArrayListImpl<>();
    private ListADT<Asset> watchlist = new ArrayListImpl<>();

    public void loadPortfolioForUser(String username) {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");
        Document query = new Document("username", username);
        Document portfolioDoc = portfolioCollection.find(query).first();

        assets.clear();
        goals.clear();

        if (portfolioDoc != null && portfolioDoc.containsKey("stocks")) {
            @SuppressWarnings("unchecked")
            List<Document> stockDocs = (List<Document>) portfolioDoc.get("stocks");
            for (Document d : stockDocs) {
                String symbol = d.getString("symbol");
                Number sharesNumber = d.get("shares", Number.class);
                int shares = (sharesNumber != null) ? sharesNumber.intValue() : 0;
                Number number = d.get("value", Number.class);
                double value = (number != null) ? number.doubleValue() : 0.0;
                assets.add(new Asset(symbol, "Stock", value, shares, new Date()));
            }
        }

        if (portfolioDoc != null && portfolioDoc.containsKey("goals")) {
            @SuppressWarnings("unchecked")
            List<Document> goalDocs = (List<Document>) portfolioDoc.get("goals");
            for (Document d : goalDocs) {
                String goalName = d.getString("name");
                Number targetNum = d.get("targetValue", Number.class);
                double targetValue = (targetNum != null) ? targetNum.doubleValue() : 0.0;
                String deadlineStr = d.getString("deadline");
                Date deadlineDate = null;
                try {
                    deadlineDate = Date.from(java.time.Instant.parse(deadlineStr));
                } catch (Exception e) {
                    System.err.println("Error parsing deadline: " + e.getMessage());
                    deadlineDate = new Date();
                }
                goals.add(new Goal(goalName, targetValue, deadlineDate));
            }
        }
    }

    public ListADT<Asset> getAssets() {
        return assets;
    }

    public void deleteAsset(String assetName) {
        for (int i = 0; i < assets.size(); i++) {
            if (assets.get(i).getName().equals(assetName)) {
                assets.remove(assets.get(i));
                break;
            }
        }
        
        String currentUser = Session.getCurrentUser();
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");
        
        portfolioCollection.updateOne(
            new Document("username", currentUser),
            new Document("$pull", new Document("stocks", new Document("symbol", assetName)))
        );
        
        loadPortfolioForUser(currentUser);
    }

    public double getTotalValue() {
        double total = 0;
        for (int i = 0; i < assets.size(); i++) {
            total += assets.get(i).getValue();
        }
        return total;
    }

    public void loadFromFile(String filename) throws IOException {
        System.out.println("loadFromFile not implemented");
    }

    public void addGoal(String name, double target, Date deadline) {
        Goal newGoal = new Goal(name, target, deadline);
        goals.add(newGoal);
        
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");
        
        String deadlineISO = deadline.toInstant().toString();
        
        Document goalDoc = new Document("name", name)
                .append("targetValue", target)
                .append("deadline", deadlineISO);
        
        String currentUser = Session.getCurrentUser();
        
        UpdateOptions options = new UpdateOptions().upsert(true);
        
        portfolioCollection.updateOne(
                new Document("username", currentUser),
                new Document("$push", new Document("goals", goalDoc)),
                options
        );
    }

    public ListADT<Goal> getGoals() {
        return goals;
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
        
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");
        
        Document stockDoc = new Document("symbol", asset.getName())
                .append("shares", asset.getQuantity())
                .append("value", asset.getCurrentPrice());
        
        String currentUser = Session.getCurrentUser();
        
        UpdateOptions options = new UpdateOptions().upsert(true);
        portfolioCollection.updateOne(
                new Document("username", currentUser),
                new Document("$push", new Document("stocks", stockDoc)),
                options
        );
    }

    public void editAsset(Asset oldAsset, Asset updatedAsset) {
        String currentUser = Session.getCurrentUser();
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");

        UpdateOptions options = new UpdateOptions().upsert(true);

        portfolioCollection.updateOne(
            new Document("username", currentUser),
            new Document("$pull", new Document("stocks", new Document("symbol", oldAsset.getName()))),
            options
        );

        Document updatedAssetDoc = new Document("symbol", updatedAsset.getName())
            .append("shares", updatedAsset.getQuantity())
            .append("value", updatedAsset.getCurrentPrice());

        portfolioCollection.updateOne(
            new Document("username", currentUser),
            new Document("$push", new Document("stocks", updatedAssetDoc)),
            options
        );

        loadPortfolioForUser(currentUser);
    }

    public void removeAsset(String assetName) {
        for (int i = 0; i < assets.size(); i++) {
            if (assets.get(i).getName().equals(assetName)) {
                assets.remove(assets.get(i));
                break;
            }
        }
        
        String currentUser = Session.getCurrentUser();
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> portfolioCollection = db.getCollection("portfolio");
        
        portfolioCollection.updateOne(
            new Document("username", currentUser),
            new Document("$pull", new Document("stocks", new Document("symbol", assetName)))
        );
        
        loadPortfolioForUser(currentUser);
    }

    public void addToWatchlist(Asset asset) {
        watchlist.add(asset);
    }

    public void removeFromWatchlist(String assetName) {
        for (int i = 0; i < watchlist.size(); i++) {
            if (watchlist.get(i).getName().equals(assetName)) {
                watchlist.remove(watchlist.get(i));
                break;
            }
        }
    }

    public ListADT<Asset> getWatchlist() {
        return watchlist;
    }

    public void rebalancePortfolio(double goal) {
        System.out.println("rebalancePortfolio not implemented");
    }

    public void saveToFile(String filename) throws IOException {
        System.out.println("saveToFile not implemented");
    }
}
