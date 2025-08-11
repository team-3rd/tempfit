package com.example.tempfit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 지역명 → 위/경도 지오코딩
 * - 기본은 오픈스트리트맵 Nominatim 사용(무료). 상용 전환 시 카카오/네이버 등으로 교체 권장.
 * - 교체 시 메서드 시그니처(geocode)만 유지하면 OpenAIChatbotService와의 결합부 변경 불필요.
 */
@Slf4j
@Service
public class OpenAIGeocodingService {
        private final ObjectMapper mapper = new ObjectMapper();

    public static class GeoPoint {
        public final double lat;
        public final double lon;
        public final String displayName;
        public GeoPoint(double lat, double lon, String displayName) {
            this.lat = lat; this.lon = lon; this.displayName = displayName;
        }
    }

    public GeoPoint geocode(String query) {
        if (query == null || query.isBlank()) return null;
        try {
            // 한국 내 검색을 우선 가정
            String q = URLEncoder.encode(query + ", Korea", StandardCharsets.UTF_8);
            String urlStr = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=" + q;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Nominatim 정책상 User-Agent 필수
            conn.setRequestProperty("User-Agent", "TempFit/1.0 (support@tempfit.example)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(7000);

            int code = conn.getResponseCode();
            if (code != 200) {
                log.warn("Geocoding HTTP {}", code);
                return null;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonNode arr = mapper.readTree(br);
                if (arr.isArray() && arr.size() > 0) {
                    JsonNode first = arr.get(0);
                    double lat = Double.parseDouble(first.path("lat").asText());
                    double lon = Double.parseDouble(first.path("lon").asText());
                    String display = first.path("display_name").asText("");
                    return new GeoPoint(lat, lon, display);
                }
            }
        } catch (Exception e) {
            log.error("geocode failed: {}", query, e);
        }
        return null;
    }
}
