package org.example.rideshare.controller;

import jakarta.validation.Valid;
import org.example.rideshare.dto.CreateRideRequest;
import org.example.rideshare.model.Ride;
import org.example.rideshare.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RideController {
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // USER: Create Ride
    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(Authentication auth, @Valid @RequestBody CreateRideRequest req) {
        Ride r = rideService.requestRide(auth.getName(), req);
        return ResponseEntity.ok(r);
    }

    // USER: View My Rides
    @GetMapping("/user/rides")
    public ResponseEntity<List<Ride>> myRides(Authentication auth) {
        return ResponseEntity.ok(rideService.getMyRides(auth.getName()));
    }

    // USER/DRIVER: Complete Ride
    @PostMapping("/rides/{rideId}/complete")
    public ResponseEntity<Ride> complete(@PathVariable String rideId) {
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }
}
