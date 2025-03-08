package com.parking.strategy;

import com.parking.constants.VehicleType;
import com.parking.model.Vehicle;

import java.time.Duration;

/**
 * Implements a pricing strategy for overtime charges
 * This applies when the parking duration exceeds the max duration of all duration-based strategies
 * The price increases per time unit (hour, minute, etc.)
 */
public class OvertimePricingStrategy implements PricingStrategy {
    private final Duration appliesAfter;
    private final Duration timeUnit;
    private final double ratePerUnit;
    private final VehicleType vehicleType;
    
    public OvertimePricingStrategy(VehicleType vehicleType, Duration appliesAfter, Duration timeUnit, double ratePerUnit) {
        this.appliesAfter = appliesAfter;
        this.timeUnit = timeUnit;
        this.ratePerUnit = ratePerUnit;
        this.vehicleType = vehicleType;
    }
    
    @Override
    public double calculatePrice(Vehicle vehicle, Duration duration) {
        // Calculate how many time units
        long units = (duration.toSeconds() + timeUnit.toSeconds() - 1) / timeUnit.toSeconds(); // Ceiling division
        
        return ratePerUnit * units;
    }

    
    public Duration getTimeUnit() {
        return timeUnit;
    }

    public Duration getAppliesAfter() {
        return appliesAfter;
    }
    
    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
} 