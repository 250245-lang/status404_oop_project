package parkinglot.payment;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CASH")
public class CashTransaction extends Payment {

    private double cashTendered;

    // --- Hibernate Required No-args Constructor ---
    protected CashTransaction() {
        super();
    }

    public CashTransaction(double amount, double cashTendered) {
        super(amount);
        this.cashTendered = cashTendered;
    }

    @Override
    public boolean initiateTransaction() {
        if (cashTendered < getAmount()) {
            System.out.println("Insufficient cash tendered. Required: $" + getAmount()
                    + ", Tendered: $" + cashTendered);
            setStatus(PaymentStatus.DECLINED);
            return false;
        }
        setStatus(PaymentStatus.COMPLETED);
        double change = cashTendered - getAmount();
        System.out.printf("Cash payment of $%.2f accepted. Change: $%.2f%n", getAmount(), change);
        return true;
    }

    public double getCashTendered() { return cashTendered; }
    public void setCashTendered(double cashTendered) { this.cashTendered = cashTendered; }

    public double getChange() {
        return Math.max(0, cashTendered - getAmount());
    }

}
