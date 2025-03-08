package com.parking.service;

import com.parking.model.Vehicle;
import com.parking.constants.VehicleType;
import com.parking.repository.PricingRuleRepository;
import com.parking.strategy.DurationBasedPricingStrategy;
import com.parking.strategy.OvertimePricingStrategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing parking operations
 */
public class ParkingService {
    private final PricingRuleRepository ruleRepository;
    private final Map<String, LocalDateTime> parkedVehicles;
    
    public ParkingService(PricingRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        this.parkedVehicles = new HashMap<>();
    }
    
    public boolean vehicleEntry(Vehicle vehicle, LocalDateTime entryTime) {
        if (parkedVehicles.containsKey(vehicle.getLicensePlate())) {
            return false;
        }
        
        parkedVehicles.put(vehicle.getLicensePlate(), entryTime);
        return true;
    }
    
    public double vehicleExit(Vehicle vehicle, LocalDateTime exitTime) {
        String licensePlate = vehicle.getLicensePlate();
        LocalDateTime entryTime = parkedVehicles.get(licensePlate);
        
        if (entryTime == null) {
            throw new IllegalStateException("Vehicle was not registered as parked: " + licensePlate);
        }
        
        parkedVehicles.remove(licensePlate);
        
        Duration parkingDuration = Duration.between(entryTime, exitTime);
        
        return calculateFinalBillForVehicletype(vehicle.getType(), parkingDuration);
    }

    private double calculateFinalBillForVehicletype(VehicleType vehicleType, Duration parkingDuration) {
        List<DurationBasedPricingStrategy> durationStrategies = ruleRepository.getDurationStrategies().get(vehicleType);
        List<OvertimePricingStrategy> overtimeStrategies = ruleRepository.getOvertimeStrategies().get(vehicleType);

        double totalPrice = 0.0;
        
        if (durationStrategies == null || durationStrategies.isEmpty()) {
            return 0.0;
        }
        
        durationStrategies.sort((a, b) -> a.getUpto().compareTo(b.getUpto()));
        
        long parkingDurationSeconds = parkingDuration.toSeconds();
        
        DurationBasedPricingStrategy appliedDurationStrategy = null;
        for (int i = durationStrategies.size() - 1; i >= 0; i--) {
            DurationBasedPricingStrategy strategy = durationStrategies.get(i);
            long uptoSeconds = strategy.getUpto().toSeconds();
            
            if (parkingDurationSeconds >= uptoSeconds) {
                appliedDurationStrategy = strategy;
                totalPrice = strategy.getPrice();
                break;
            } else if (i == 0 && parkingDurationSeconds < uptoSeconds) {
                appliedDurationStrategy = strategy;
                totalPrice = strategy.getPrice();
            }
        }
        
        if (appliedDurationStrategy == null) {
            return 0.0;
        }
        
        long maxDurationSeconds = appliedDurationStrategy.getUpto().toSeconds();
        long overtimeDurationSeconds = parkingDurationSeconds - maxDurationSeconds;
        
        if (overtimeDurationSeconds > 0 && overtimeStrategies != null && !overtimeStrategies.isEmpty()) {
            overtimeStrategies.sort((a, b) -> a.getAppliesAfter().compareTo(b.getAppliesAfter()));
            
            long remainingOvertimeSeconds = overtimeDurationSeconds;
            
            for (int i = 0; i < overtimeStrategies.size(); i++) {
                OvertimePricingStrategy currentStrategy = overtimeStrategies.get(i);
                long appliesAfterSeconds = currentStrategy.getAppliesAfter().toSeconds();
                
                long endBoundarySeconds;
                if (i < overtimeStrategies.size() - 1) {
                    endBoundarySeconds = overtimeStrategies.get(i + 1).getAppliesAfter().toSeconds();
                } else {
                    endBoundarySeconds = Long.MAX_VALUE;
                }
                
                if (remainingOvertimeSeconds > appliesAfterSeconds) {
                    long timeToChargeSeconds = Math.min(remainingOvertimeSeconds, endBoundarySeconds) - appliesAfterSeconds;
                    
                    if (timeToChargeSeconds > 0) {
                        long timeUnitSeconds = currentStrategy.getTimeUnit().toSeconds();
                        long wholeUnits = timeToChargeSeconds / timeUnitSeconds;
                        
                        totalPrice += wholeUnits * currentStrategy.getRatePerUnit();
                    }
                }
            }
        }
        
        return totalPrice;
    }
} 