package com.parking;

import com.parking.constants.VehicleType;
import com.parking.model.Vehicle;
import com.parking.repository.PricingRuleRepository;
import com.parking.service.ParkingService;
import com.parking.strategy.DurationBasedPricingStrategy;
import com.parking.strategy.OvertimePricingStrategy;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        PricingRuleRepository repository = new PricingRuleRepository();
        setupPricingRules(repository);
        
        ParkingService parkingService = new ParkingService(repository);
        
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(3), "Car parked for 3 hours");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(5), "Car parked for 5 hours");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(6).plusMinutes(20), "Car parked for 6 hours 20 minutes");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(7), "Car parked for 7 hours");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(11), "Car parked for 11 hours");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(12), "Car parked for 12 hours");
        testParkingFee(parkingService, VehicleType.CAR, Duration.ofHours(13), "Car parked for 13 hours");
        
        testParkingFee(parkingService, VehicleType.BIKE, Duration.ofHours(3), "Bike parked for 3 hours");
        testParkingFee(parkingService, VehicleType.BIKE, Duration.ofHours(5), "Bike parked for 5 hours");
        testParkingFee(parkingService, VehicleType.BIKE, Duration.ofHours(6).plusMinutes(20), "Bike parked for 6 hours 20 minutes");
        testParkingFee(parkingService, VehicleType.BIKE, Duration.ofHours(7), "Bike parked for 7 hours");
        testParkingFee(parkingService, VehicleType.BIKE, Duration.ofHours(13), "Bike parked for 13 hours");
    }
    
    private static void setupPricingRules(PricingRuleRepository repository) {
        repository.addDurationStrategy(VehicleType.CAR, 
                new DurationBasedPricingStrategy(VehicleType.CAR, Duration.ofHours(4), 40.0));
        repository.addDurationStrategy(VehicleType.CAR, 
                new DurationBasedPricingStrategy(VehicleType.CAR, Duration.ofHours(6), 50.0));
        
        repository.addDurationStrategy(VehicleType.BIKE, 
                new DurationBasedPricingStrategy(VehicleType.BIKE, Duration.ofHours(6), 15.0));
        
        repository.addOvertimeStrategy(VehicleType.CAR, 
                new OvertimePricingStrategy(VehicleType.CAR, Duration.ofMinutes(0), Duration.ofHours(1), 10.0));
        repository.addOvertimeStrategy(VehicleType.CAR, 
                new OvertimePricingStrategy(VehicleType.CAR, Duration.ofHours(5), Duration.ofMinutes(15), 5.0));
        
        repository.addOvertimeStrategy(VehicleType.BIKE, 
                new OvertimePricingStrategy(VehicleType.BIKE, Duration.ofMinutes(0), Duration.ofHours(1), 10.0));
        repository.addOvertimeStrategy(VehicleType.BIKE,
                new OvertimePricingStrategy(VehicleType.BIKE, Duration.ofHours(5), Duration.ofMinutes(15), 5.0));
    }
    
    private static void testParkingFee(ParkingService service, VehicleType vehicleType, 
                                     Duration parkingDuration, String description) {
        try {
            String licensePlate = vehicleType.name() + "-" + System.currentTimeMillis() % 10000;
            Vehicle vehicle = new Vehicle(licensePlate, vehicleType);
            
            LocalDateTime entryTime = LocalDateTime.now();
            LocalDateTime exitTime = entryTime.plus(parkingDuration);
            
            System.out.println("\n--- " + description + " ---");
            
            service.vehicleEntry(vehicle, entryTime);
            
            double fee = service.vehicleExit(vehicle, exitTime);
            System.out.println("Parking Fee: ₹" + fee);
        } catch (Exception e) {
            System.err.println("Error testing " + description + ": " + e.getMessage());
        }
    }
} 