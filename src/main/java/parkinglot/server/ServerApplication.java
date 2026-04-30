package parkinglot.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import parkinglot.models.Location;
import parkinglot.models.ParkingFloor;
import parkinglot.models.ParkingLot;
import parkinglot.models.spots.*;
import parkinglot.users.*;
import parkinglot.server.repository.AccountRepository;
import parkinglot.server.repository.ParkingLotRepository;

@SpringBootApplication
@ComponentScan(basePackages = {"parkinglot"})
@EntityScan(basePackages = {"parkinglot"})
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ParkingLotRepository repo, AccountRepository accountRepo) {
        return args -> {
            if (repo.count() == 0) {
                ParkingLot lot = new ParkingLot("L1", "University Lot",
                        new Location("123 Uni St", "Tashkent", "UZ", "1000", "UZB"));

                ParkingFloor f1 = new ParkingFloor("Floor 1");
                f1.addParkingSlot(new CompactSpot("C101"));
                f1.addParkingSlot(new CompactSpot("C102"));
                f1.addParkingSlot(new MotorbikeSpot("M101"));
                f1.addParkingSlot(new ElectricSpot("E101"));
                f1.addParkingSlot(new LargeSpot("L101"));

                ParkingFloor f2 = new ParkingFloor("Floor 2");
                f2.addParkingSlot(new CompactSpot("C201"));
                f2.addParkingSlot(new HandicappedSpot("H201"));
                f2.addParkingSlot(new LargeSpot("L201"));

                ParkingFloor f3 = new ParkingFloor("Basement 1");
                f3.addParkingSlot(new ElectricSpot("E001"));
                f3.addParkingSlot(new MotorbikeSpot("M001"));

                lot.addParkingFloor(f1);
                lot.addParkingFloor(f2);
                lot.addParkingFloor(f3);
                repo.save(lot);
                System.out.println("Demo Parking Lot created in SQLite!");
            }
            if (accountRepo.count() == 0) {
                Person adminInfo = new Person("System Admin",
                        new Location("Movarounnahra street 1", "Tashkent", "TSH", "100", "UZB"),
                        "admin@newuu.uz", "998901234567");

                Admin admin = new Admin("admin", "admin", adminInfo);
                accountRepo.save(admin);

                // Add a Parking Attendant
                Person attInfo = new Person("John Attendant", null, "john@parking.com", "998901112233");
                ParkingAttendant attendant = new ParkingAttendant("attendant", "attendant", attInfo);
                accountRepo.save(attendant);

                System.out.println("Default accounts created:");
                System.out.println("Admin: admin/admin");
                System.out.println("Attendant: attendant/attendant");
            }
        };
    }
}