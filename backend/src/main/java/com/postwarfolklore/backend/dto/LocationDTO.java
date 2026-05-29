package com.postwarfolklore.backend.dto;

import com.postwarfolklore.backend.model.Location;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LocationDTO {
    private Integer id;
    private String slug;
    private String name;
    private String region;
    private String country;

    public static LocationDTO from(Location loc) {
        return LocationDTO.builder()
            .id(loc.getId())
            .slug(loc.getSlug())
            .name(loc.getName())
            .region(loc.getRegion())
            .country(loc.getCountry())
            .build();
    }
}
