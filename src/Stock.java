public class Stock {
    private String symbol;
    private int shares;
    private double value;

    public Stock(String symbol, int shares, double value) {
        this.symbol = symbol;
        this.shares = shares;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getShares() {
        return shares;
    }

    public double getValue() {
        return value;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return symbol + ": " + shares + " shares @ $" + value;
    }
}
