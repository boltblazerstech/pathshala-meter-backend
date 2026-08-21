package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
    @JsonProperty("phone_number") String phoneNumber,
    String password
) {}
