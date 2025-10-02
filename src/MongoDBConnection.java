import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    private static final String CONNECTION_STRING = "mongodb+srv://gaderu:aeag2324@cluster0.iirln.mongodb.net/";
    // Database name
    private static final String DATABASE_NAME = "psa";


    public static MongoDatabase getDatabase() {
        MongoClientURI uri = new MongoClientURI(CONNECTION_STRING);
        MongoClient mongoClient = new MongoClient(uri);
        return mongoClient.getDatabase(DATABASE_NAME);
    }
}
