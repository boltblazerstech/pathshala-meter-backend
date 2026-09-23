package com.pathshala.stub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SettingsDto(
    @JsonProperty("fetch_interval_minutes")
    Integer fetchIntervalMinutes,

    @JsonProperty("pre_buffer_minutes")
    Integer preBufferMinutes,

    @JsonProperty("post_buffer_minutes")
    Integer postBufferMinutes,

    @JsonProperty("radius_leave_home_meters")
    Integer radiusLeaveHomeMeters,

    @JsonProperty("radius_reach_school_meters")
    Integer radiusReachSchoolMeters,

    @JsonProperty("radius_leave_school_meters")
    Integer radiusLeaveSchoolMeters,

    @JsonProperty("radius_reach_home_meters")
    Integer radiusReachHomeMeters
) {}
