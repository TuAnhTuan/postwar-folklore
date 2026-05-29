package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.LocationDTO;
import com.postwarfolklore.backend.model.Location;
import com.postwarfolklore.backend.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAllByOrderByNameAsc()
            .stream()
            .map(LocationDTO::from)
            .toList();
    }

    public LocationDTO createLocation(String slug, String name, String region, String country) {
        Location loc = Location.builder()
            .slug(slug)
            .name(name)
            .region(region)
            .country(country != null ? country : "VN")
            .build();
        return LocationDTO.from(locationRepository.save(loc));
    }

    public void deleteLocation(Integer id) {
        if (!locationRepository.existsById(id))
            throw new NoSuchElementException("Location not found: " + id);
        locationRepository.deleteById(id);
    }
}
