package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResetFieldPasswordRequest(
        @JsonProperty("new_password") String newPassword
) {}
