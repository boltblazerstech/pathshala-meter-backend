package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangePasswordRequest(
        @com.fasterxml.jackson.annotation.JsonAlias("currentPassword")
        @JsonProperty("current_password")
        String currentPassword,

        @com.fasterxml.jackson.annotation.JsonAlias("newPassword")
        @JsonProperty("new_password")
        String newPassword,

        @com.fasterxml.jackson.annotation.JsonAlias("confirmNewPassword")
        @JsonProperty("confirm_new_password")
        String confirmNewPassword
) {}
