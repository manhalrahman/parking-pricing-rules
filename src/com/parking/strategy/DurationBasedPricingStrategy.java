package com.parking.strategy;

import com.parking.constants.VehicleType;
import com.parking.model.Vehicle;

import java.time.Duration;

public class DurationBasedPricingStrategy implements PricingStrategy {
    private final Duration upto;
    private final VehicleType vehicleType;
    private final double price;
    
    public DurationBasedPricingStrategy(VehicleType vehicleType, Duration upto, double price) {
        this.upto = upto;
        this.price = price;
        this.vehicleType = vehicleType;
    }
    
    @Override
    public double calculatePrice(Vehicle vehicle, Duration duration) {
        if (duration.compareTo(upto) > 0) {
            return 0.0;
        }
        return price;
    }

    public Duration getUpto() {
        return upto;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public double getPrice() {
        return price;
    }
} 