package parkinglot.users;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import parkinglot.constants.AccountStatus;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = ParkingAttendant.class, name = "ATTENDANT")
})
public abstract class Account {

    @Id
    private String userName;

    private String password;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Embedded
    private Person person;

    protected Account() {}

    public Account(String userName, String password, Person person) {
        this.userName = userName;
        this.password = password;
        this.person = person;
        this.status = AccountStatus.ACTIVE;
    }

    public boolean login(String userName, String password) {
        if (status != AccountStatus.ACTIVE) return false;
        return this.userName != null && this.userName.equals(userName) && 
               this.password != null && this.password.equals(password);
    }

    // Getters and Setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
