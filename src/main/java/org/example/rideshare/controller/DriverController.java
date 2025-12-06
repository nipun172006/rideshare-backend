package org.example.rideshare.controller;

import org.example.rideshare.model.Ride;
import org.example.rideshare.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/driver")
public class DriverController {
    private final RideService rideService;

    public DriverController(RideService rideService) {
        this.rideService = rideService;
    }

    // DRIVER: View Pending Requests
    @GetMapping("/rides/requests")
    public ResponseEntity<List<Ride>> pending() {
        return ResponseEntity.ok(rideService.getPendingRequestsForDriver());
    }

    // DRIVER: Accept Ride
    @PostMapping("/rides/{rideId}/accept")
    public ResponseEntity<Ride> accept(Authentication auth, @PathVariable String rideId) {
        return ResponseEntity.ok(rideService.acceptRide(auth.getName(), rideId));
    }
}
