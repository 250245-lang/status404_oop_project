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
import parkinglot.users.Admin;
import parkinglot.users.Person;
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
    public CommandLineRunner initData(AccountRepository accountRepo, ParkingLotRepository parkingLotRepo) {
        return args -> {
            if (accountRepo.count() == 0) {
                Person adminInfo = new Person("System Admin",
                        new Location("Movarounnahra street 1", "Tashkent", "TSH", "100", "UZB"),
                        "admin@newuu.uz", "998901234567");

                Admin admin = new Admin("admin", "admin", adminInfo);
                accountRepo.save(admin);

                System.out.println("Default Admin account created: admin/admin");
            }

            if (parkingLotRepo.count() == 0) {
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

                lot.addParkingFloor(f1);
                lot.addParkingFloor(f2);
                
                parkingLotRepo.save(lot);
                System.out.println("Demo Parking Lot created with 2 floors!");
            }
        };
    }
}
