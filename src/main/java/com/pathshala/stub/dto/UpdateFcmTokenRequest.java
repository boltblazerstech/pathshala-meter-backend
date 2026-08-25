package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateFcmTokenRequest(
    @JsonProperty("fcm_token") String fcmToken
) {}
