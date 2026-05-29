package com.postwarfolklore.backend.controller;

import com.postwarfolklore.backend.dto.LocationDTO;
import com.postwarfolklore.backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /** Public: frontend dùng để render danh sách tab filter */
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    /** Admin: thêm địa điểm mới không cần redeploy */
    @PostMapping("/admin/locations")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<LocationDTO> createLocation(
        @RequestParam String slug,
        @RequestParam String name,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String country
    ) {
        return ResponseEntity.ok(locationService.createLocation(slug, name, region, country));
    }

    /** Admin: xóa địa điểm */
    @DeleteMapping("/admin/locations/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
