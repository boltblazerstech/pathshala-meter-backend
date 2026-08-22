package com.pathshala.stub.service;

import com.pathshala.stub.dto.ParsedCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapLinkParserTest {

    private MapLinkParser parser;

    @BeforeEach
    void setUp() {
        parser = new MapLinkParser();
    }

    // ── Pattern 1: !1d<lng>!2d<lat> (Directions link) ────────────

    @Test
    void directionsLink_extractsDestinationCoordinates() {
        // Real Dhanbad ISKCON Directions link
        String link = "https://www.google.com/maps/dir//ISKCON+Dhanbad/"
                + "@23.805703,86.4534458,17z/data=!4m2!4m1"
                + "!3e0!1d86.4534458!2d23.805703";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(23.805703, result.lat(), 0.000001);
        assertEquals(86.4534458, result.lng(), 0.000001);
        assertEquals("parsed", result.confidence());
    }

    @Test
    void directionsLink_prefersDataOverViewport() {
        // This link has BOTH @viewport AND !1d!2d data — !1d!2d should win
        String link = "https://www.google.com/maps/dir/Current+Location/Destination/"
                + "@22.0,85.0,12z/data=!3m1!4b1!4m5!1d86.4534458!2d23.805703";

        ParsedCoordinate result = parser.parseMapLink(link);

        // Should pick the !1d!2d destination, not the @22.0,85.0 viewport
        assertEquals(23.805703, result.lat(), 0.000001);
        assertEquals(86.4534458, result.lng(), 0.000001);
        assertEquals("parsed", result.confidence());
    }

    @Test
    void directionsLink_negativeLongitude() {
        // Western hemisphere example
        String link = "https://www.google.com/maps/dir//Place/"
                + "data=!1d-73.985428!2d40.748817";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(40.748817, result.lat(), 0.000001);
        assertEquals(-73.985428, result.lng(), 0.000001);
        assertEquals("parsed", result.confidence());
    }

    // ── Pattern 2: q=<lat>,<lng> (Pin-drop share) ────────────────

    @Test
    void qParameterLink_extractsCoordinates() {
        String link = "https://www.google.com/maps?q=23.80496,86.45602";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(23.80496, result.lat(), 0.00001);
        assertEquals(86.45602, result.lng(), 0.00001);
        assertEquals("parsed", result.confidence());
    }

    @Test
    void qParameterLink_withOtherParams() {
        String link = "https://www.google.com/maps?hl=en&q=28.6139,77.2090&z=15";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(28.6139, result.lat(), 0.0001);
        assertEquals(77.2090, result.lng(), 0.0001);
        assertEquals("parsed", result.confidence());
    }

    // ── Pattern 3: @<lat>,<lng>,<zoom> (Viewport fallback) ───────

    @Test
    void viewportOnly_fallsBackWithLowerConfidence() {
        // A link that has ONLY the @viewport — no !1d!2d, no q=
        String link = "https://www.google.com/maps/@23.8049565,86.4560206,17z";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(23.8049565, result.lat(), 0.0000001);
        assertEquals(86.4560206, result.lng(), 0.0000001);
        assertEquals("fallback", result.confidence());
    }

    @Test
    void viewportOnly_differentZoomLevel() {
        String link = "https://www.google.com/maps/@28.6139,77.2090,12z";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(28.6139, result.lat(), 0.0001);
        assertEquals(77.2090, result.lng(), 0.0001);
        assertEquals("fallback", result.confidence());
    }

    // ── Error cases ──────────────────────────────────────────────

    @Test
    void nullLink_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseMapLink(null));
    }

    @Test
    void blankLink_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseMapLink("   "));
    }

    @Test
    void unparseableLink_throwsClearError() {
        String link = "https://www.google.com/maps/place/SomePlace";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parseMapLink(link)
        );

        assertTrue(ex.getMessage().contains("Could not parse coordinates"));
        assertTrue(ex.getMessage().contains(link));
    }

    @Test
    void randomUrl_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parseMapLink("https://www.example.com/nothing-here"));
    }

    // ── Priority order ───────────────────────────────────────────

    @Test
    void dataPattern_hasHigherPriorityThanQ() {
        // Link has both q= and !1d!2d — !1d!2d should win
        String link = "https://www.google.com/maps?q=10.0,20.0&data=!1d86.45!2d23.80";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(23.80, result.lat(), 0.01);
        assertEquals(86.45, result.lng(), 0.01);
        assertEquals("parsed", result.confidence());
    }

    @Test
    void qPattern_hasHigherPriorityThanViewport() {
        // Link has both q= and @viewport — q= should win with "parsed"
        String link = "https://www.google.com/maps/@10.0,20.0,15z?q=23.80,86.45";

        ParsedCoordinate result = parser.parseMapLink(link);

        assertEquals(23.80, result.lat(), 0.01);
        assertEquals(86.45, result.lng(), 0.01);
        assertEquals("parsed", result.confidence());
    }
}
