import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Asset implements Comparable<Asset> {
    private String name;
    private String type;
    private double currentPrice;
    private double quantity;
    private double value;
    private List<Double> priceHistory = new ArrayList<>();

    public Asset(String name, String type, double currentPrice, double quantity, Date date) {
        this.name = name;
        this.type = type;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.value = currentPrice * quantity;
        priceHistory.add(currentPrice);
    }

    public String getName() {
        return name;
    }
    			
    public String getType() {
        return type;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getValue() {
        return value;
    }

    public List<Double> getPriceHistory() {
        return priceHistory;
    }

    public void setCurrentPrice(double newPrice) {
        this.currentPrice = newPrice;
        this.value = newPrice * quantity;
        priceHistory.add(newPrice);
    }

    public double getReturn() {
        if (priceHistory.isEmpty())
            return 0;
        double initial = priceHistory.get(0);
        return ((currentPrice - initial) / initial) * 100;
    }

    public double getVolatility() {
        if (priceHistory.size() < 2)
            return 0;
        double mean = priceHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = priceHistory.stream().mapToDouble(p -> Math.pow(p - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

   
    @Override
    public int compareTo(Asset other) {
       
        return Double.compare(other.getVolatility(), this.getVolatility());
    }


    @Override
    public String toString() {
        return name + " (" + type + ") - " + quantity + " at $" + String.format("%.2f", currentPrice);
    }
}
