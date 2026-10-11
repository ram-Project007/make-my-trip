package com.makemytrip.makemytrip.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
public record RoomPreferenceRequest(@NotBlank @Size(max=100) String preferredRoomType, @Min(1) Integer minimumCapacity, @Size(max=30) List<@NotBlank @Size(max=60) String> preferredAmenities, Boolean preferPremium) {}
