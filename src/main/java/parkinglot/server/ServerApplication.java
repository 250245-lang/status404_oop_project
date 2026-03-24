package parkinglot.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import parkinglot.models.Location;
import parkinglot.users.Admin;
import parkinglot.users.Person;
import parkinglot.server.repository.AccountRepository;

@SpringBootApplication
@ComponentScan(basePackages = {"parkinglot"})
@EntityScan(basePackages = {"parkinglot"})
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepo) {
        return args -> {
            if (accountRepo.count() == 0) {
                Person adminInfo = new Person("System Admin",
                        new Location("Movarounnahra street 1", "Tashkent", "TSH", "100", "UZB"),
                        "admin@newuu.uz", "998901234567");

                Admin admin = new Admin("admin", "admin", adminInfo);
                accountRepo.save(admin);

                System.out.println("Default Admin account created: admin/admin");
            }
        };
    }
}
