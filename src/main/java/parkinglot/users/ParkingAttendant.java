package parkinglot.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.models.ParkingTicket;
import parkinglot.models.ParkingRate;
import parkinglot.payment.CashTransaction;
import parkinglot.payment.Payment;
import parkinglot.payment.PaymentStatus;


@Entity
@DiscriminatorValue("ATTENDANT")
public class ParkingAttendant extends Account {

    public ParkingAttendant(String userName, String password, Person person) {
        super(userName, password, person);
    }
    protected ParkingAttendant(){super();}

    public boolean processTicket(ParkingTicket ticket, ParkingRate rate) {
        if (ticket == null) {
            System.out.println("Invalid ticket.");
            return false;
        }
        long durationMinutes = ticket.getParkingDurationMinutes()+1;
        double fee = rate.calculateFee(durationMinutes);
        System.out.printf("Attendant %s processing ticket %s | Duration: %d min | Fee: $%.2f%n",
                getUserName(), ticket.getTicketNumber(), durationMinutes, fee);
        return true;
    }

    public boolean acceptCashPayment(ParkingTicket ticket, ParkingRate rate, double cashTendered) {
        if (ticket.isPaid()) {
            System.out.println("Ticket " + ticket.getTicketNumber() + " is already paid.");
            return false;
        }
        long durationMinutes = ticket.getParkingDurationMinutes()+1;
        double fee = rate.calculateFee(durationMinutes);

        CashTransaction tx = new CashTransaction(fee, cashTendered);
        boolean success = tx.initiateTransaction();
        if (success) {
            ticket.markPaid(fee);
        }
        return success;
    }
}