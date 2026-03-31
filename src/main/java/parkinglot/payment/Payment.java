package parkinglot.payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashTransaction.class, name = "CASH"),
        @JsonSubTypes.Type(value = CreditCardTransaction.class, name = "CREDIT_CARD")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // SQLite will auto-increment this

    private LocalDateTime creationDate;
    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    // Standard no-args constructor for JPA
    protected Payment() {
        this.creationDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public Payment(double amount) {
        this();
        this.amount = amount;
    }

    public abstract boolean initiateTransaction();

    // Getters and Setters
    public LocalDateTime getCreationDate() { return creationDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

}
