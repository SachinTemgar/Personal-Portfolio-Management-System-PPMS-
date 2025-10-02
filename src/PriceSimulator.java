import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PriceSimulator {
    private Random rand = new Random();
    private PortfolioManager portfolio;
    private Timer timer;

    public PriceSimulator(PortfolioManager portfolio) {
        this.portfolio = portfolio;
    }

    public void startSimulation(Runnable updateCallback) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ListADT<Asset> assets = portfolio.getAssets();
                for (int i = 0; i < assets.size(); i++) {
                    Asset asset = assets.get(i);
                    double change = (rand.nextDouble() - 0.5) * 10;
                    asset.setCurrentPrice(Math.max(1, asset.getCurrentPrice() + change));
                }
                ListADT<Asset> watchlist = portfolio.getWatchlist();
                for (int i = 0; i < watchlist.size(); i++) {
                    Asset asset = watchlist.get(i);
                    double change = (rand.nextDouble() - 0.5) * 10;
                    asset.setCurrentPrice(Math.max(1, asset.getCurrentPrice() + change));
                }
                updateCallback.run();
            }
        }, 0, 2000);
    }

    public void stopSimulation() {
        if (timer != null) timer.cancel();
    }
}