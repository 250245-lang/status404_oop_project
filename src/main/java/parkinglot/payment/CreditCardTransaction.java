package parkinglot.payment;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardTransaction extends Payment {

    private String nameOnCard;
    private String cardNumber;
    private double cardBalance;

    // --- Required for Hibernate ---
    protected CreditCardTransaction() {
        super();
    }

    public CreditCardTransaction(double amount, String nameOnCard, String cardNumber, double cardBalance) {
        super(amount);
        this.nameOnCard = nameOnCard;
        this.cardNumber = maskCardNumber(cardNumber);
        this.cardBalance = cardBalance;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public boolean initiateTransaction() {
        System.out.printf("Processing credit card payment of $%.2f for card: %s (Name: %s)%n",
                getAmount(), cardNumber, nameOnCard);
        if (cardBalance < getAmount()) {
            setStatus(PaymentStatus.DECLINED);
            System.out.println("Credit card declined");
            return false;
        } else {
            setStatus(PaymentStatus.COMPLETED);
            cardBalance -= getAmount();
            System.out.println("Credit card payment successful.");
            return true;
        }
    }

    public String getNameOnCard() { return nameOnCard; }
    public void setNameOnCard(String nameOnCard) { this.nameOnCard = nameOnCard; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public double getCardBalance() { return cardBalance; }
    public void setCardBalance(double cardBalance) { this.cardBalance = cardBalance; }
}