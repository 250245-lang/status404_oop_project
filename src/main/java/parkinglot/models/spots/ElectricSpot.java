package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.models.vehicles.Vehicle;

@Entity
@DiscriminatorValue("ELECTRIC")
public class ElectricSpot extends ParkingSpot {
    @jakarta.persistence.OneToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @jakarta.persistence.JoinColumn(name = "panel_id")
    private parkinglot.hardware.ElectricPanel electricPanel;

    protected ElectricSpot(){super();}

    public ElectricSpot(String number) {
        super(number, ParkingSpotType.ELECTRIC);
        this.electricPanel = new parkinglot.hardware.ElectricPanel(number);
    }

    public parkinglot.hardware.ElectricPanel getElectricPanel() { return electricPanel; }

    @Override
    public boolean assignVehicle(Vehicle vehicle) {
        if (vehicle.getType() != VehicleType.ELECTRIC) {
            System.out.println("Electric spot is reserved for electric vehicles only. Received: " + vehicle.getType());
            return false;
        }
        return super.assignVehicle(vehicle);
    }
}