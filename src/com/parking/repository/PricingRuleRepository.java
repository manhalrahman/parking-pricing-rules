package com.parking.repository;

import com.parking.constants.VehicleType;
import com.parking.strategy.DurationBasedPricingStrategy;
import com.parking.strategy.OvertimePricingStrategy;
import com.parking.strategy.PricingStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class PricingRuleRepository {
    private HashMap<VehicleType, List<DurationBasedPricingStrategy>> durationStrategies;
    private HashMap<VehicleType, List<OvertimePricingStrategy>> overtimeStrategies;

    public PricingRuleRepository() {
        this.durationStrategies = new HashMap<>();
        this.overtimeStrategies = new HashMap<>();
    }

    public void addDurationStrategy(VehicleType vehicleType, DurationBasedPricingStrategy strategy) {
        durationStrategies.computeIfAbsent(vehicleType, k -> new ArrayList<>());
        if(!followsUniqueTemporalConstraint(vehicleType, strategy)) {
            throw new IllegalArgumentException("Overlapping duration strategy");
        }
        durationStrategies.get(vehicleType).add(strategy);
        sortDurationStrategies(durationStrategies.get(vehicleType));
    }

    private void sortDurationStrategies(List<DurationBasedPricingStrategy> strategies) {
        strategies.sort(Comparator.comparing(DurationBasedPricingStrategy::getUpto));
    }

    private void sortOvertimeStrategies(List<OvertimePricingStrategy> strategies) {
        strategies.sort(Comparator.comparing(OvertimePricingStrategy::getAppliesAfter));
    }


    private boolean followsUniqueTemporalConstraint(VehicleType vehicleType, PricingStrategy strategy) {
        if(strategy instanceof DurationBasedPricingStrategy) {
            if(durationStrategies.get(vehicleType).isEmpty()) {
                return true;
            }
            DurationBasedPricingStrategy durationStrategy = (DurationBasedPricingStrategy) strategy;
            DurationBasedPricingStrategy existingStrategy = durationStrategies.get(vehicleType).get(durationStrategies.get(vehicleType).size() - 1);
            if(durationStrategy.getUpto().compareTo(existingStrategy.getUpto()) < 0) {
                return false;
            }
        }

        if(strategy instanceof OvertimePricingStrategy) {
            if(overtimeStrategies.get(vehicleType).isEmpty()) {
                return true;
            }
            OvertimePricingStrategy incomingOvertimeStrategy = (OvertimePricingStrategy) strategy;
            OvertimePricingStrategy existingOvertimeStrategy = overtimeStrategies.get(vehicleType).get(overtimeStrategies.get(vehicleType).size() - 1);
            if(existingOvertimeStrategy.getAppliesAfter().compareTo(incomingOvertimeStrategy.getAppliesAfter()) >= 0) {
                return false;
            }
        }
        return true;
    }

    public void addOvertimeStrategy(VehicleType vehicleType, OvertimePricingStrategy strategy) {
        overtimeStrategies.computeIfAbsent(vehicleType, k -> new ArrayList<>());
        if(!followsUniqueTemporalConstraint(vehicleType, strategy)) {
            throw new IllegalArgumentException("Overlapping overtime strategy");
        }
        overtimeStrategies.get(vehicleType).add(strategy);
        sortOvertimeStrategies(overtimeStrategies.get(vehicleType));
    }

    public HashMap<VehicleType, List<DurationBasedPricingStrategy>> getDurationStrategies() {
        return durationStrategies;
    }

    public HashMap<VehicleType, List<OvertimePricingStrategy>> getOvertimeStrategies() {
        return overtimeStrategies;
    }
} 