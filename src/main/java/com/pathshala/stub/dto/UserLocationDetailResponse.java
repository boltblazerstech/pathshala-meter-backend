package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record UserLocationDetailResponse(
        @JsonProperty("user_id")
        UUID userId,

        @JsonProperty("user_name")
        String userName,

        @JsonProperty("user_role")
        String userRole,

        String date,

        @JsonProperty("paathshaala_id")
        UUID paathshaalaId,

        @JsonProperty("paathshaala_name")
        String paathshaalaName,

        List<LocationPointDetailDto> points
) {}
