package parkinglot.server.controller;


import parkinglot.constants.VehicleType;
import parkinglot.models.ParkingLot;
import parkinglot.models.ParkingTicket;
import parkinglot.models.vehicles.Car;
import parkinglot.models.vehicles.Motorbike;
import parkinglot.models.vehicles.Vehicle;
import parkinglot.payment.CashTransaction;
import parkinglot.server.repository.ParkingLotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import parkinglot.constants.ParkingSpotType;
import parkinglot.models.ParkingFloor;
import parkinglot.models.spots.*;
import java.util.List;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin
public class ParkingController {

    @Autowired
    private ParkingLotRepository lotRepo;

    @GetMapping("/status")
    public ParkingLot getStatus() {
        return lotRepo.findAll().getFirst();
    }

    // --- Admin Operations ---

    @PostMapping("/floors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ParkingFloor addFloor(@RequestParam String name) {
        System.out.println("Admin Request: Add Floor - " + name);
        ParkingLot lot = lotRepo.findAll().getFirst();
        if (lot.getFloors().stream().anyMatch(f -> f.getName().equalsIgnoreCase(name))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Floor already exists");
        }
        ParkingFloor floor = new ParkingFloor(name);
        lot.addParkingFloor(floor);
        lotRepo.save(lot);
        return floor;
    }

    @DeleteMapping("/floors/{name}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteFloor(@PathVariable String name) {
        System.out.println("Admin Request: Delete Floor - " + name);
        ParkingLot lot = lotRepo.findAll().getFirst();
        boolean removed = lot.getFloors().removeIf(f -> f.getName().equals(name));
        if (!removed) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Floor not found");
        }
        lotRepo.save(lot);
    }

    @PostMapping("/floors/{floorName}/spots")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void addSpot(@PathVariable String floorName, @RequestParam String number, @RequestParam ParkingSpotType type) {
        System.out.println("Admin Request: Add Spot - Floor: " + floorName + ", Num: " + number);
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingFloor floor = lot.getFloors().stream()
                .filter(f -> f.getName().equals(floorName))
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Floor not found"));

        if (lot.getFloors().stream().flatMap(f -> f.getSpots().stream()).anyMatch(s -> s.getNumber().equals(number))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Spot number already exists in the building");
        }

        ParkingSpot spot = createSpot(number, type);
        floor.addParkingSlot(spot);
        lotRepo.save(lot);
    }

    @DeleteMapping("/floors/{floorName}/spots/{spotNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteSpot(@PathVariable String floorName, @PathVariable String spotNumber) {
        System.out.println("Admin Request: Delete Spot - Floor: " + floorName + ", Num: " + spotNumber);
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingFloor floor = lot.getFloors().stream()
                .filter(f -> f.getName().equals(floorName))
                .findFirst()
                .orElseThrow();

        floor.getSpots().removeIf(s -> s.getNumber().equals(spotNumber));
        lotRepo.save(lot);
    }

    @PostMapping("/rates")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void updateRates(@RequestBody parkinglot.models.ParkingRate newRate) {
        System.out.println("Admin Request: Update Rates");
        ParkingLot lot = lotRepo.findAll().getFirst();
        parkinglot.models.ParkingRate rate = lot.getParkingRate();
        rate.setFirstHourRate(newRate.getFirstHourRate());
        rate.setSecondHourRate(newRate.getSecondHourRate());
        rate.setThirdHourRate(newRate.getThirdHourRate());
        rate.setRemainingHourRate(newRate.getRemainingHourRate());
        rate.setElectricChargingRate(newRate.getElectricChargingRate());
        lotRepo.save(lot);
    }

    @PostMapping("/spots/{spotNumber}/charging/start")
    public void startCharging(@PathVariable String spotNumber) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ElectricSpot spot = findElectricSpot(lot, spotNumber);
        if (spot != null && !spot.isFree()) {
            // Cannot charge if ticket is already paid or completed
            if (spot.getCurrentVehicle() != null && spot.getCurrentVehicle().getTicket() != null) {
                ParkingTicket ticket = spot.getCurrentVehicle().getTicket();
                if (ticket.isPaid() || ticket.getStatus() == parkinglot.constants.ParkingTicketStatus.COMPLETED) {
                    throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, 
                        "Cannot start charging: Ticket is already paid or completed.");
                }
            }
            spot.getElectricPanel().startCharging();
            lotRepo.save(lot);
        }
    }

    @PostMapping("/spots/{spotNumber}/charging/stop")
    public void stopCharging(@PathVariable String spotNumber) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ElectricSpot spot = findElectricSpot(lot, spotNumber);
        if (spot != null && spot.getElectricPanel().isCharging()) {
            spot.getElectricPanel().stopCharging();
            // Sync to ticket
            if (spot.getCurrentVehicle() != null && spot.getCurrentVehicle().getTicket() != null) {
                ParkingTicket ticket = spot.getCurrentVehicle().getTicket();
                ticket.setChargingMinutes(spot.getElectricPanel().getTotalChargedMinutes());
            }
            lotRepo.save(lot);
        }
    }

    private ElectricSpot findElectricSpot(ParkingLot lot, String number) {
        return lot.getFloors().stream()
                .flatMap(f -> f.getSpots().stream())
                .filter(s -> s.getNumber().equals(number) && s instanceof ElectricSpot)
                .map(s -> (ElectricSpot) s)
                .findFirst()
                .orElse(null);
    }

    private ParkingSpot createSpot(String number, ParkingSpotType type) {
        return switch (type) {
            case HANDICAPPED -> new HandicappedSpot(number);
            case LARGE -> new LargeSpot(number);
            case MOTORBIKE -> new MotorbikeSpot(number);
            case ELECTRIC -> new ElectricSpot(number);
            default -> new CompactSpot(number);
        };
    }

    @Autowired
    private parkinglot.server.repository.VehicleRepository vehicleRepo;

    // --- Operations ---

    @PostMapping("/issue-ticket")
    public ParkingTicket issueTicket(@RequestParam String license, @RequestParam(required = false) VehicleType type) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        
        Vehicle v = vehicleRepo.findById(license).orElse(null);
        
        if (v == null) {
            if (type == null) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "VEHICLE_NOT_FOUND");
            }
            v = switch (type) {
                case CAR -> new Car(license);
                case TRUCK -> new parkinglot.models.vehicles.Truck(license);
                case VAN -> new parkinglot.models.vehicles.Van(license);
                case MOTORBIKE -> new Motorbike(license);
                case ELECTRIC -> new parkinglot.models.vehicles.ElectricVehicle(license);
            };
        } else {
            // Check if this vehicle is currently parked (has an active, unpaid ticket)
            boolean alreadyParked = lot.getActiveTickets().stream()
                    .anyMatch(t -> t.getVehicleLicense().equals(license) &&
                              t.getStatus() == parkinglot.constants.ParkingTicketStatus.ACTIVE);
            if (alreadyParked) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "ALREADY_PARKED: Vehicle " + license + " already has an active ticket.");
            }
        }
        
        ParkingTicket ticket = lot.vehicleEntry(v);
        if (ticket != null) lotRepo.save(lot);
        return ticket;
    }

    @GetMapping("/ticket/{ticketNumber}")
    public ParkingTicket getTicket(@PathVariable String ticketNumber) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        if (ticket == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Ticket not found");
        }
        return ticket;
    }

    @GetMapping("/calculate-fee/{ticketNumber}")
    public double calculateFee(@PathVariable String ticketNumber) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        if (ticket == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Ticket not found");
        }
        // Mock: treat each minute of parking as one hour
        long mockHours = ticket.getParkingDurationMinutes()+1;
        double parkingFee = lot.getParkingRate().calculateFee(mockHours * 60);
        
        // Add charging fee: $electricChargingRate per mock hour (minute)
        double chargingFee = ticket.getChargingMinutes() * lot.getParkingRate().getElectricChargingRate();
        
        return parkingFee + chargingFee;
    }


    @PostMapping("/pay")
    public String pay(@RequestParam String ticketNumber, @RequestParam double amount, @RequestParam String method) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        if (ticket == null) return "Failed: Ticket not found.";
        if (ticket.isPaid()) return "Failed: Ticket is already paid.";

        // Check if currently charging
        ElectricSpot spot = findElectricSpot(lot, ticket.getSpotNumber());
        if (spot != null && spot.getElectricPanel().isCharging()) {
            return "Failed: Vehicle is still charging. Stop charging at the EV Panel before paying.";
        }

        // Calculate the correct fee server-side (mock: 1 minute = 1 hour)
        long mockHours = ticket.getParkingDurationMinutes()+1;
        double parkingFee = lot.getParkingRate().calculateFee(mockHours * 60);
        double chargingFee = ticket.getChargingMinutes() * lot.getParkingRate().getElectricChargingRate();
        double correctFee = parkingFee + chargingFee;

        if (method.equalsIgnoreCase("CASH")) {
            if (amount < correctFee) {
                return String.format("Failed: Insufficient amount. Required: $%.2f", correctFee);
            }
            double change = Math.round((amount - correctFee) * 100.0) / 100.0;
            ticket.markPaid(correctFee);
            lotRepo.save(lot);
            return String.format("Success. Charged: $%.2f. Change: $%.2f", correctFee, change);
        } else if (method.equalsIgnoreCase("CREDIT")) {
            parkinglot.payment.CreditCardTransaction payment = new parkinglot.payment.CreditCardTransaction(correctFee, "Mock Name", "0000-0000-0000-0000", 100);
            if (payment.initiateTransaction()) {
                ticket.markPaid(correctFee);
                lotRepo.save(lot);
                return String.format("Success. Credit Card charged: $%.2f", correctFee);
            }
        }
        return "Failed: Invalid payment method.";
    }

    @PostMapping("/exit")
    public String exitVehicle(@RequestParam String ticketNumber) {
        ParkingLot lot = lotRepo.findAll().getFirst();
        ParkingTicket ticket = lot.findTicket(ticketNumber);
        
        if (ticket == null) return "Failed: Ticket not found.";
        if (!ticket.isPaid()) return "Failed: Ticket is not paid yet.";
        
        // Find the actual vehicle parked in the assigned spot
        Vehicle actualVehicle = null;
        for (parkinglot.models.ParkingFloor floor : lot.getFloors()) {
            for (parkinglot.models.spots.ParkingSpot spot : floor.getSpots()) {
                if (spot.getNumber().equals(ticket.getSpotNumber())) {
                    actualVehicle = spot.getCurrentVehicle();
                    break;
                }
            }
            if (actualVehicle != null) break;
        }

        if (actualVehicle == null) return "Failed: Vehicle not found in the assigned spot.";
        
        lot.vehicleExit(actualVehicle);
        ticket.setStatus(parkinglot.constants.ParkingTicketStatus.COMPLETED);
        lotRepo.save(lot);
        
        return "Success: Gate opened. Goodbye!";
    }
}