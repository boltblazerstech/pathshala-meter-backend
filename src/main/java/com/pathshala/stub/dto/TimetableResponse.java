package com.pathshala.stub.dto;

import java.util.List;

public record TimetableResponse(
        List<TimetableSlotDto> slots
) {}
