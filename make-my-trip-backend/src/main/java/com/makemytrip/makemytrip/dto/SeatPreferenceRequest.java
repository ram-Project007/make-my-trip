package com.makemytrip.makemytrip.dto;
import com.makemytrip.makemytrip.models.Seat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
public record SeatPreferenceRequest(Seat.SeatClass preferredClass, Seat.SeatType preferredType, Boolean preferWindow, Boolean preferAisle, @Min(1) @Max(100) Integer preferredRow) {}
