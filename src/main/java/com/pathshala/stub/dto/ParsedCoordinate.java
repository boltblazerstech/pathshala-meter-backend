package com.pathshala.stub.dto;

/**
 * Result of parsing a Google Maps link into coordinates.
 *
 * @param lat        latitude in decimal degrees
 * @param lng        longitude in decimal degrees
 * @param confidence "parsed" if extracted from a reliable pattern (!1d!2d or q=),
 *                   "fallback" if extracted from the viewport @lat,lng pattern
 */
public record ParsedCoordinate(double lat, double lng, String confidence) {}
