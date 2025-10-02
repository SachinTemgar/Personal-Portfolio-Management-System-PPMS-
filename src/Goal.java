import java.util.Date;

public class Goal {
    private String name;
    private double targetValue;
    private Date deadline;
    
    public Goal(String name, double targetValue, Date deadline) {
        this.name = name;
        this.targetValue = targetValue;
        this.deadline = deadline;
    }
    
    public String getName() {
        return name;
    }
    
    public double getTargetValue() {
        return targetValue;
    }
    
    public Date getDeadline() {
        return deadline;
    }
    
   
    public double progress(double totalPortfolioValue) {
        return (totalPortfolioValue / targetValue) * 100;
    }
    
    @Override
    public String toString() {
        return name + " (Target: $" + String.format("%.2f", targetValue) + ")";
    }
}
