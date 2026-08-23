package com.pathshala.stub.service;

import com.pathshala.stub.dto.ParsedCoordinate;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Google Maps link into lat/lng coordinates.
 *
 * Priority order (all patterns extract an ATOMIC lat+lng pair from a
 * single source — never one value from one pattern and the other from another):
 *
 * 1. !3d{lat}!4d{lng} inside a data= parameter (Place pin — most reliable)
 * 2. !1d{lng}!2d{lat} inside a data= parameter (Directions destination)
 * 3. q={lat},{lng} query parameter (pin-drop share links)
 * 4. @{lat},{lng},{zoom} viewport pattern (fallback — lower confidence)
 * 5. Shortened URLs (maps.app.goo.gl, goo.gl/maps) → resolve redirect → re-run 1–4
 * 6. If nothing matches → throw IllegalArgumentException
 */
@Service
public class MapLinkParser {

    // Pattern 1: !3d<latitude>!4d<longitude> in data= segments (Place pin)
    private static final Pattern PLACE_PATTERN =
            Pattern.compile("!3d(-?[\\d.]+)!4d(-?[\\d.]+)");

    // Pattern 2: !1d<longitude>!2d<latitude> in data= segments (Directions)
    // The !1d value is longitude, !2d value is latitude
    private static final Pattern DATA_PATTERN =
            Pattern.compile("!1d(-?[\\d.]+)!2d(-?[\\d.]+)");

    // Pattern 3: q=<lat>,<lng> query parameter
    private static final Pattern Q_PATTERN =
            Pattern.compile("[?&]q=(-?[\\d.]+),(-?[\\d.]+)");

    // Pattern 4: @<lat>,<lng>,<zoom>z viewport
    private static final Pattern VIEWPORT_PATTERN =
            Pattern.compile("@(-?[\\d.]+),(-?[\\d.]+),");

    // Shortened URL hosts
    private static final Pattern SHORTENED_HOST =
            Pattern.compile("^(maps\\.app\\.goo\\.gl|goo\\.gl)$", Pattern.CASE_INSENSITIVE);

    /**
     * Parse a Google Maps link into coordinates.
     *
     * @param mapLink a full Google Maps URL (or shortened goo.gl link)
     * @return ParsedCoordinate with lat, lng, and confidence level
     * @throws IllegalArgumentException if the link cannot be parsed
     */
    public ParsedCoordinate parseMapLink(String mapLink) {
        if (mapLink == null || mapLink.isBlank()) {
            throw new IllegalArgumentException("Map link must not be null or blank");
        }

        String url = mapLink.trim();

        // Step 5: If shortened URL, resolve the redirect first
        if (isShortenedUrl(url)) {
            url = resolveRedirect(url);
        }

        // Step 1: Look for !3d<lat>!4d<lng> (Place pin — most reliable)
        Matcher placeMatcher = PLACE_PATTERN.matcher(url);
        if (placeMatcher.find()) {
            double lat = Double.parseDouble(placeMatcher.group(1));
            double lng = Double.parseDouble(placeMatcher.group(2));
            return new ParsedCoordinate(lat, lng, "parsed");
        }

        // Step 2: Look for !1d<lng>!2d<lat> in data= parameter (Directions)
        Matcher dataMatcher = DATA_PATTERN.matcher(url);
        if (dataMatcher.find()) {
            double lng = Double.parseDouble(dataMatcher.group(1));
            double lat = Double.parseDouble(dataMatcher.group(2));
            return new ParsedCoordinate(lat, lng, "parsed");
        }

        // Step 3: Look for q=<lat>,<lng>
        Matcher qMatcher = Q_PATTERN.matcher(url);
        if (qMatcher.find()) {
            double lat = Double.parseDouble(qMatcher.group(1));
            double lng = Double.parseDouble(qMatcher.group(2));
            return new ParsedCoordinate(lat, lng, "parsed");
        }

        // Step 4: Fallback to @<lat>,<lng>,<zoom> viewport
        Matcher viewportMatcher = VIEWPORT_PATTERN.matcher(url);
        if (viewportMatcher.find()) {
            double lat = Double.parseDouble(viewportMatcher.group(1));
            double lng = Double.parseDouble(viewportMatcher.group(2));
            return new ParsedCoordinate(lat, lng, "fallback");
        }

        // Step 5: Nothing matched — throw
        throw new IllegalArgumentException(
                "Could not parse coordinates from map link: " + mapLink
                        + " — please check the link format");
    }

    private boolean isShortenedUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null && SHORTENED_HOST.matcher(host).matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Follow HTTP 3xx redirects to resolve a shortened Google Maps URL
     * into the full URL. Does NOT download the page body — only reads
     * the Location header from the redirect response.
     */
    private String resolveRedirect(String shortenedUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    URI.create(shortenedUrl).toURL().openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location != null && !location.isBlank()) {
                    // Some redirects are relative — handle that edge case
                    if (location.startsWith("/")) {
                        URI original = URI.create(shortenedUrl);
                        location = original.getScheme() + "://" + original.getHost() + location;
                    }
                    // Recursively resolve in case of multiple redirects
                    if (isShortenedUrl(location)) {
                        return resolveRedirect(location);
                    }
                    return location;
                }
            }
            conn.disconnect();
            // If no redirect, return original URL and let the regex patterns try it
            return shortenedUrl;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to resolve shortened URL: " + shortenedUrl + " — " + e.getMessage(), e);
        }
    }
}
