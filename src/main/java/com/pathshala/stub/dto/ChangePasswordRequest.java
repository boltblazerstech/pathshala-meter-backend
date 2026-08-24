package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangePasswordRequest(
        @JsonProperty("current_password")
        String currentPassword,

        @JsonProperty("new_password")
        String newPassword,

        @JsonProperty("confirm_new_password")
        String confirmNewPassword
) {}
