package com.parking.strategy;

import com.parking.model.Vehicle;

import java.time.Duration;

public interface PricingStrategy {
    double calculatePrice(Vehicle vehicle, Duration duration);
} 