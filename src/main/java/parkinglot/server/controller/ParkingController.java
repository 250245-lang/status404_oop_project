package parkinglot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import parkinglot.constants.VehicleType;
import parkinglot.models.ParkingLot;
import parkinglot.models.ParkingTicket;
import parkinglot.models.vehicles.Vehicle;
import parkinglot.models.vehicles.*;
import parkinglot.server.repository.ParkingLotRepository;
import parkinglot.server.repository.VehicleRepository;
import parkinglot.payment.CreditCardTransaction;

/**
 * Controller handling parking entry, exit, and fee payment.
 */
@RestController
@RequestMapping("/api/parking")
@CrossOrigin
public class ParkingController extends BaseController {

    @Autowired
    private ParkingLotRepository lotRepo;

    @Autowired
    private VehicleRepository vehicleRepo;

    @GetMapping("/status")
    public ResponseEntity<ParkingLot> getStatus() {
        ParkingLot lot = lotRepo.findAll().stream().findFirst().orElse(null);
        return success(lot);
    }

    @PostMapping("/issue-ticket")
    public ResponseEntity<ParkingTicket> issueTicket(@RequestParam String license, @RequestParam VehicleType type) {
        ParkingLot lot = lotRepo.findAll().stream().findFirst().orElseThrow();
        
        Vehicle v = vehicleRepo.findById(license).orElse(null);
        if (v == null) {
            v = switch (type) {
                case CAR -> new Car(license);
                case TRUCK -> new Truck(license);
                case MOTORBIKE -> new Motorbike(license);
                case VAN -> new Van(license);
                case ELECTRIC -> new ElectricVehicle(license);
            };
        }

        ParkingTicket ticket = lot.vehicleEntry(v);
        if (ticket != null) {
            lotRepo.save(lot);
            return success(ticket);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parking Lot is full");
        }
    }

    @GetMapping("/calculate-fee/{ticketNumber}")
    public ResponseEntity<Double> calculateFee(@PathVariable String ticketNumber) {
        ParkingLot lot = lotRepo.findAll().stream().findFirst().orElseThrow();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        if (ticket == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found");

        double fee = lot.calculateFee(ticket);
        return success(fee);
    }

    @PostMapping("/pay")
    public ResponseEntity<String> pay(@RequestParam String ticketNumber, @RequestParam double amount, @RequestParam String method) {
        ParkingLot lot = lotRepo.findAll().stream().findFirst().orElseThrow();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        if (ticket == null) return error("Ticket not found", HttpStatus.NOT_FOUND);
        if (ticket.isPaid()) return error("Already paid", HttpStatus.BAD_REQUEST);

        double correctFee = lot.calculateFee(ticket);

        if (method.equalsIgnoreCase("CASH")) {
            if (amount < correctFee) return error("Insufficient amount", HttpStatus.BAD_REQUEST);
            ticket.markPaid(correctFee);
            lotRepo.save(lot);
            return success("Paid with cash. Change: " + (amount - correctFee));
        } else if (method.equalsIgnoreCase("CREDIT")) {
            CreditCardTransaction payment = new CreditCardTransaction(correctFee, "Demo User", "0000-0000-0000-0000", 123);
            if (payment.initiateTransaction()) {
                ticket.markPaid(correctFee);
                lotRepo.save(lot);
                return success("Paid with credit card");
            }
        }
        return error("Payment failed", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/exit")
    public ResponseEntity<String> exitVehicle(@RequestParam String ticketNumber) {
        ParkingLot lot = lotRepo.findAll().stream().findFirst().orElseThrow();
        ParkingTicket ticket = lot.findTicket(ticketNumber);

        if (ticket == null) return error("Ticket not found", HttpStatus.NOT_FOUND);
        if (!ticket.isPaid()) return error("Ticket not paid", HttpStatus.BAD_REQUEST);

        Vehicle v = vehicleRepo.findById(ticket.getVehicleLicense()).orElse(null);
        if (v == null) return error("Vehicle not found", HttpStatus.NOT_FOUND);

        if (lot.vehicleExit(v)) {
            lotRepo.save(lot);
            return success("Goodbye!");
        } else {
            return error("Exit failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
