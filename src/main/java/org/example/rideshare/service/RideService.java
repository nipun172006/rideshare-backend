package org.example.rideshare.service;

import org.example.rideshare.dto.CreateRideRequest;
import org.example.rideshare.exception.BadRequestException;
import org.example.rideshare.exception.NotFoundException;
import org.example.rideshare.model.Ride;
import org.example.rideshare.model.User;
import org.example.rideshare.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RideService {
    private final RideRepository rideRepository;
    private final UserService userService;

    public RideService(RideRepository rideRepository, UserService userService) {
        this.rideRepository = rideRepository;
        this.userService = userService;
    }

    public Ride requestRide(String username, CreateRideRequest req) {
        User u = userService.getByUsername(username);
        if (u == null || !"ROLE_USER".equals(u.getRole())) {
            throw new BadRequestException("only ROLE_USER can request rides");
        }
        Ride r = new Ride();
        r.setUserId(u.getId());
        r.setPickupLocation(req.getPickupLocation());
        r.setDropLocation(req.getDropLocation());
        r.setStatus("REQUESTED");
        r.setCreatedAt(new Date());
        return rideRepository.save(r);
    }

    public List<Ride> getPendingRequestsForDriver() {
        return rideRepository.findByStatus("REQUESTED");
    }

    public Ride acceptRide(String username, String rideId) {
        User driver = userService.getByUsername(username);
        if (driver == null || !"ROLE_DRIVER".equals(driver.getRole())) {
            throw new BadRequestException("only ROLE_DRIVER can accept rides");
        }
        Ride r = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("ride not found"));
        if (!"REQUESTED".equals(r.getStatus())) {
            throw new BadRequestException("ride must be REQUESTED to accept");
        }
        r.setDriverId(driver.getId());
        r.setStatus("ACCEPTED");
        return rideRepository.save(r);
    }

    public Ride completeRide(String rideId) {
        Ride r = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("ride not found"));
        if (!"ACCEPTED".equals(r.getStatus())) {
            throw new BadRequestException("ride must be ACCEPTED to complete");
        }
        r.setStatus("COMPLETED");
        return rideRepository.save(r);
    }

    public List<Ride> getMyRides(String username) {
        User u = userService.getByUsername(username);
        if (u == null) throw new NotFoundException("user not found");
        return rideRepository.findByUserId(u.getId());
    }
}
