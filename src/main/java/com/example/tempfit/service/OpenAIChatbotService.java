package com.example.tempfit.service;

import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.OpenAIChatbotMessageRequest;
import com.example.tempfit.dto.OpenAIChatbotMessageResponse;
import com.example.tempfit.dto.WeatherDTO;
import com.example.tempfit.service.OpenAIGeocodingService.GeoPoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chat + 서버 날씨 연동 + 로그인 성별 반영 + 비로그인 Fallback(중성 코디)
 * - 지역명 지오코딩 + 시간 파싱(상대/절대/요일) → 해당 시각의 예보로 요약/코디
 * - 성별: DB 코드(남=0, 여=1) 또는 문자열을 받아 표준화(남성/여성/중성) 후 반영
 * - 비로그인/성별미지정 시 항상 '중성'으로 코디. OpenAI가 빈 응답이면 서버가 Fallback 코디 생성.
 * - 우선순위: "문장 속 지역명" > 요청 본문 lat/lon(브라우저 위치)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIChatbotService {

    private final WebClient openAiWebClient;
    private final WeatherService weatherService;
    private final OpenAIGeocodingService geocodingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.model}")
    private String model;

    public OpenAIChatbotMessageResponse chat(OpenAIChatbotMessageRequest request) {
        try {
            final ZoneId KST = ZoneId.of("Asia/Seoul");
            final LocalDate today = LocalDate.now(KST);
            final LocalDate tomorrow = today.plusDays(1);

            final String userMsg = request.getMessage();
            final String lower = userMsg.toLowerCase(Locale.ROOT);

            // ★ 성별 표준화 (DB 코드 0/1 우선, 없으면 문자열 해석, 둘 다 없으면 '중성')
            final String genderNorm = normalizeGender(request.getGender(), request.getSexCode());

            // 1) 의도 분기
            boolean askWeatherToday = matchesAnyRegex(lower,
                    "오늘.*(날씨|기온|온도)",
                    "(날씨|기온|온도).*오늘",
                    "(현재|지금)\\s*(날씨|기온|온도)"
            );
            boolean askWeatherTomorrow = matchesAnyRegex(lower,
                    "내일.*(날씨|기온|온도)",
                    "(날씨|기온|온도).*내일"
            );
            boolean outfitToday = matchesAnyRegex(lower,
                    "오늘.*(뭐\\s*입|뭐입|입을까|입지|코디|룩|옷\\s*추천|출근룩|데이트룩)",
                    "(뭐\\s*입|뭐입|입을까|입지|코디|룩|옷\\s*추천|출근룩|데이트룩).*오늘"
            );
            boolean outfitTomorrow = matchesAnyRegex(lower,
                    "내일.*(뭐\\s*입|뭐입|입을까|입지|코디|룩|옷\\s*추천|출근룩|데이트룩)",
                    "(뭐\\s*입|뭐입|입을까|입지|코디|룩|옷\\s*추천|출근룩|데이트룩).*내일"
            );

            // 2) 상대 시각 "n시간 뒤/후"
            Integer hourOffset = extractHourOffset(lower);
            boolean askWeatherByOffset = (hourOffset != null) && matchesAnyRegex(lower, "(날씨|기온|온도)");
            boolean outfitByOffset     = (hourOffset != null) && matchesAnyRegex(lower, "(뭐\\s*입|뭐입|입을까|입지|코디|룩|옷\\s*추천)");

            // 3) 절대 시각
            LocalDateTime absoluteDT = extractAbsoluteDateTime(userMsg, KST);
            // 4) 요일 시각
            if (absoluteDT == null) absoluteDT = extractWeekdayDateTime(userMsg, KST);

            // 5) 문장 속 지역명 추출
            String mentionedLoc = extractLocationKorean(userMsg);

            // 6) 좌표 결정: "문장 지역" 최우선 → 실패 시 요청 lat/lon 사용
            Double lat = request.getLat();
            Double lon = request.getLon();
            String  locNameForDisplay = request.getLocation();

            if (mentionedLoc != null && !mentionedLoc.isBlank()) {
                GeoPoint gp = geocodingService.geocode(mentionedLoc);
                if (gp != null) {
                    lat = gp.lat;
                    lon = gp.lon;
                    locNameForDisplay = mentionedLoc;
                    log.debug("Use geocoded coords for '{}': {}, {}", mentionedLoc, lat, lon);
                } else {
                    log.warn("Geocoding failed for '{}', fallback to request lat/lon: {}, {}", mentionedLoc, lat, lon);
                }
            }

            // 7) 서버 날씨가 필요한 케이스
            boolean wantsOutfit = outfitToday || outfitTomorrow || outfitByOffset;
            boolean wantsWeatherOnly = (askWeatherToday || askWeatherTomorrow || askWeatherByOffset || absoluteDT != null)
                    && !wantsOutfit;

            if (askWeatherToday || askWeatherTomorrow || askWeatherByOffset || absoluteDT != null
                    || mentionedLoc != null || wantsOutfit) {

                if (lat == null || lon == null) {
                    // 위치가 없으면 날씨를 못 붙이므로, 바로 중성(또는 성별기준) Fallback 코디 제공
                    String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                    return new OpenAIChatbotMessageResponse(fallback);
                }

                GridDTO grid = toGrid(lat, lon);
                if (grid == null) {
                    String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                    return new OpenAIChatbotMessageResponse(fallback);
                }

                List<WeatherDTO> list = weatherService.getWeatherApi(grid);
                if (list == null || list.isEmpty()) {
                    String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                    return new OpenAIChatbotMessageResponse(fallback);
                }

                // 대상 시각(우선순위: 상대 ▶ 절대 ▶ 내일 ▶ 오늘)
                LocalDateTime targetDT =
                        (hourOffset != null) ? LocalDateTime.now(KST).plusHours(hourOffset)
                        : (absoluteDT != null) ? absoluteDT
                        : (askWeatherTomorrow || outfitTomorrow) ? LocalDateTime.of(tomorrow, LocalTime.of(12,0))
                        : LocalDateTime.now(KST);

                WeatherDTO point = pickClosest(list, targetDT);
                if (point == null) {
                    String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                    return new OpenAIChatbotMessageResponse(fallback);
                }

                String prefixLoc = (locNameForDisplay != null && !locNameForDisplay.isBlank()) ? locNameForDisplay + " " : "";
                String labelTime;
                if (hourOffset != null) {
                    labelTime = formatKoreanDateTimeLabel(targetDT, KST) + "(약 " + hourOffset + "시간 뒤)";
                } else if (absoluteDT != null) {
                    labelTime = formatKoreanDateTimeLabel(absoluteDT, KST);
                } else {
                    labelTime = targetDT.toLocalDate().equals(today) ? "오늘" : "내일";
                }

                if (wantsWeatherOnly || (mentionedLoc != null && !wantsOutfit)) {
                    return new OpenAIChatbotMessageResponse(buildWeatherSummary(prefixLoc + labelTime, point));
                }

                if (wantsOutfit) {
                    String preface = buildWeatherSummary(prefixLoc + labelTime, point) + "\n";
                    String ai = aiOutfitRecommendation(preface, point, locNameForDisplay,
                            point.getFcstDate() != null ? point.getFcstDate().toString() : null,
                            genderNorm);

                    // ★ OpenAI가 빈 응답일 때 서버 Fallback
                    if (ai == null || ai.isBlank() || ai.startsWith("(코디")) {
                        String fallback = fallbackNeutralOutfit(point, genderNorm, null);
                        return new OpenAIChatbotMessageResponse(preface + fallback);
                    }
                    return new OpenAIChatbotMessageResponse(preface + ai);
                }
            }

            // 8) 일반 의상/날씨 범위 질문 → OpenAI 위임 (성별 지시 포함)
            String systemPolicy = ("""
                너는 TempFit의 친절한 의상·패션·날씨 전용 AI 챗봇이다. 답변 범위를 의류, 패션, 코디, 스타일링, 계절/기온·날씨에 따른 옷차림, 소재/세탁/보관, 사이즈·핏 조언으로 제한한다.
                판단은 넓게 하여 '오늘 뭐입지?' 같은 문장은 반드시 관련 있다고 간주하라. 날씨/온도를 물으면 답하라.
                범위를 벗어난 요청엔 한 문장으로만 거절: '의상 관련 질문에만 답변드려요.' (예: 오늘 기온에 맞는 코디 추천)
                한국어로 간결하게 답하고, 가능하면 상의→아우터→하의→신발 순으로 1~3개 제안.
                사용자 성별이 '%s'면 그 기준(남성/여성)으로 자연스럽게 코디하고, 정보가 없거나 '중성'이면 젠더-뉴트럴하게.
                """).formatted(genderNorm).replace("\n", " ").trim();

            String systemContext = buildLooseContext(request, genderNorm);

            String userMsgJson = objectMapper.writeValueAsString(userMsg);
            String payload = """
                {
                  "model": "%s",
                  "messages": [
                    { "role": "system", "content": %s },
                    { "role": "system", "content": %s },
                    { "role": "user",   "content": %s }
                  ],
                  "temperature": 0.7
                }
                """.formatted(
                    model,
                    objectMapper.writeValueAsString(systemPolicy),
                    objectMapper.writeValueAsString(systemContext),
                    userMsgJson
                );

            String json = openAiWebClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/chat/completions").build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> {
                        log.error("OpenAI API transport error", ex);
                        return Mono.just("{\"__transport_error\":\"" + ex.getMessage() + "\"}");
                    })
                    .block();

            if (json == null) {
                // 최종 Fallback
                String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                return new OpenAIChatbotMessageResponse(fallback);
            }
            JsonNode root = objectMapper.readTree(json);
            if (root.has("__transport_error")) {
                String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                return new OpenAIChatbotMessageResponse(fallback);
            }
            if (root.has("error")) {
                String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                return new OpenAIChatbotMessageResponse(fallback);
            }
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText("");
                if (content.isBlank()) {
                    String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
                    return new OpenAIChatbotMessageResponse(fallback);
                }
                return new OpenAIChatbotMessageResponse(content.trim());
            }
            String fallback = fallbackNeutralOutfit(null, genderNorm, request.getTemperature());
            return new OpenAIChatbotMessageResponse(fallback);

        } catch (Exception e) {
            log.error("chat() failed", e);
            String fallback = fallbackNeutralOutfit(null, normalizeGender(request.getGender(), request.getSexCode()),
                    request.getTemperature());
            return new OpenAIChatbotMessageResponse(fallback);
        }
    }

    // ===== Helpers =====

    private GridDTO toGrid(double lat, double lon) {
        CoordsDTO coords = new CoordsDTO();
        coords.setLat(lat);
        coords.setLon(lon);
        return weatherService.changeCoords(coords);
    }

    /** 요청 시각(LocalDateTime)에 가장 가까운 예보 한 점 선택 */
    private WeatherDTO pickClosest(List<WeatherDTO> list, LocalDateTime targetDT) {
        return list.stream()
                .min(Comparator.comparingLong(w -> {
                    LocalDate date = (w.getFcstDate() != null) ? w.getFcstDate() : targetDT.toLocalDate();
                    LocalTime time = (w.getFcstTime() != null) ? w.getFcstTime() : LocalTime.NOON;
                    LocalDateTime dt = LocalDateTime.of(date, time);
                    return Math.abs(java.time.Duration.between(dt, targetDT).toMinutes());
                }))
                .orElse(null);
    }

    private String buildWeatherSummary(String label, WeatherDTO w) {
        String t   = (w.getTmp() != null) ? w.getTmp() + "℃" : "-";
        String sky = (w.getSky() != null) ? w.getSky() : "-";
        String reh = (w.getReh() != null) ? w.getReh() : "-";
        String wsd = (w.getWsd() != null) ? w.getWsd() : "-";
        String when = (w.getFcstDate() != null && w.getFcstTime() != null)
                ? String.format("(%s %02d:00)", w.getFcstDate(), w.getFcstTime().getHour())
                : "";
        return String.format("%s %s 날씨: %s, 기온: %s, 습도: %s, 풍속: %s", label, when, sky, t, reh, wsd);
    }

    /** OpenAI에게 '서버 날씨 값'을 기반으로 코디 요청 (성별 반영) */
    private String aiOutfitRecommendation(String preface, WeatherDTO point, String location, String dateIso, String genderNorm) {
        try {
            String ctx = String.format(
                    "지역: %s | 날짜: %s | 기온(°C): %s | 하늘상태: %s | 습도: %s | 풍속: %s | 성별: %s",
                    (location == null || location.isBlank()) ? "미지정" : location,
                    (dateIso == null || dateIso.isBlank()) ? "미지정" : dateIso,
                    Optional.ofNullable(point.getTmp()).orElse("-"),
                    Optional.ofNullable(point.getSky()).orElse("-"),
                    Optional.ofNullable(point.getReh()).orElse("-"),
                    Optional.ofNullable(point.getWsd()).orElse("-"),
                    genderNorm
            );

            String system = """
                너는 TempFit의 의상 코디 어시스턴트다. 반드시 주어진 '지역/날짜/날씨/기온/성별' 값을 사용하여 한국어로 간결하게 코디를 제안하라.
                성별이 '남성'이면 남성 중심의 아이템/핏 예시를, '여성'이면 여성 중심의 아이템/핏 예시를 자연스럽게 사용한다.
                성별이 '중성'이면 젠더-뉴트럴하게 제안한다. 필요 시 성별 경계를 넘는 제안도 가능.
                가능하면 상의→아우터→하의→신발 순서로 1~3개씩 제안하고, 마지막에 소재/레이어링 팁 1줄 추가.
                구체 브랜드/모델명은 피하고 일반명으로 답하라.
                """.replace("\n", " ").trim();

            String user = "다음 컨텍스트를 사용해 해당 시각/지역/성별 기준으로 코디를 제안해줘.\n컨텍스트: " + ctx;

            String payload = """
                {
                  "model": "%s",
                  "messages": [
                    { "role": "system", "content": %s },
                    { "role": "user",   "content": %s }
                  ],
                  "temperature": 0.7
                }
                """.formatted(
                    model,
                    objectMapper.writeValueAsString(system),
                    objectMapper.writeValueAsString(user)
                );

            String json = openAiWebClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/chat/completions").build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (json == null) return "(코디 생성 실패)";
            JsonNode root = objectMapper.readTree(json);
            if (root.has("error")) return "(코디 생성 오류)";
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText("");
                return content.isBlank() ? "(코디 빈 응답)" : content.trim();
            }
            return "(코디 유효한 응답 없음)";
        } catch (Exception e) {
            log.error("aiOutfitRecommendation failed", e);
            return "(코디 생성 중 오류)";
        }
    }

    private boolean matchesAnyRegex(String s, String... patterns) {
        if (s == null) return false;
        for (String p : patterns) {
            if (Pattern.compile(p).matcher(s).find()) return true;
        }
        return false;
    }

    private Integer extractHourOffset(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{1,2})\\s*시간\\s*(뒤|후)").matcher(s);
        if (m.find()) {
            try {
                int h = Integer.parseInt(m.group(1));
                if (h >= 0 && h <= 72) return h;
            } catch (NumberFormatException ignored) {}
        }
        Matcher m2 = Pattern.compile("(\\d{1,2})\\s*(h|hr|hrs)\\b").matcher(s);
        if (m2.find()) {
            try {
                int h = Integer.parseInt(m2.group(1));
                if (h >= 0 && h <= 72) return h;
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    /**
     * 절대 시각 파싱(‘시’ 또는 ‘:’ 필수):
     *  - "오전 9시", "오후 3시 30분", "오늘/내일/모레 + (오전/오후) HH[:mm]"
     *  - "2025-08-12[ HH:mm]", "2025/8/12 오후 3시", "2025.08.12 09:00"
     *  - ⚠️ '3시간'의 '3'이 시간으로 잡히지 않도록 '시(?!간)' + ‘시/:’ 강제
     */
    private LocalDateTime extractAbsoluteDateTime(String text, ZoneId kst) {
        if (text == null) return null;
        String s = text.trim();
        LocalDate base = LocalDate.now(kst);

        Pattern pDate = Pattern.compile(
                "(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})" +
                "(?:\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:[:]|시(?!간))\\s*(\\d{1,2})?)?",
                Pattern.CASE_INSENSITIVE);
        Matcher md = pDate.matcher(s);
        if (md.find()) {
            int year  = Integer.parseInt(md.group(1));
            int month = Integer.parseInt(md.group(2));
            int day   = Integer.parseInt(md.group(3));
            Integer hour = null, minute = 0;
            String mer = md.group(4);
            if (md.group(5) != null) hour = Integer.parseInt(md.group(5));
            if (md.group(6) != null) minute = Integer.parseInt(md.group(6));
            if (hour == null) hour = 12; // 시간 미지정이면 정오
            hour = adjustHourByMeridiem(hour, mer);
            return LocalDateTime.of(year, month, day, hour, minute);
        }

        Pattern pAbs = Pattern.compile(
                "(오늘|내일|모레)?\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:(?:[:]|시(?!간))\\s*(\\d{1,2})?)\\s*(?:분)?",
                Pattern.CASE_INSENSITIVE);
        Matcher ma = pAbs.matcher(s);
        if (ma.find()) {
            String dayWord = ma.group(1);
            String mer = ma.group(2);
            int hour = Integer.parseInt(ma.group(3));
            int minute = (ma.group(4) != null) ? Integer.parseInt(ma.group(4)) : 0;

            if (dayWord != null) {
                switch (dayWord) {
                    case "오늘" -> base = base;
                    case "내일" -> base = base.plusDays(1);
                    case "모레" -> base = base.plusDays(2);
                    default -> {}
                }
            }
            hour = adjustHourByMeridiem(hour, mer);
            return LocalDateTime.of(base, LocalTime.of(hour, minute));
        }

        return null;
    }

    /**
     * 요일 + 시각 파싱:
     *  - "금요일 오후 3시", "다음 수요일 9시", "오는 토요일 18시 30분"
     *  - qualifier 없으면 "다가오는" 해당 요일(오늘 그 요일이고 시간이 지났으면 다음 주)
     */
    private LocalDateTime extractWeekdayDateTime(String text, ZoneId kst) {
        if (text == null) return null;
        String s = text.trim();

        Pattern p = Pattern.compile(
                "(이번|다음|오는)?\\s*(월|화|수|목|금|토|일)요일\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:(?:[:]|시(?!간))\\s*(\\d{1,2})?)?\\s*(?:분)?",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);
        if (!m.find()) return null;

        String qual = m.group(1); // 이번/다음/오는
        String yoil = m.group(2);
        String mer  = m.group(3);
        int hour    = Integer.parseInt(m.group(4));
        int minute  = (m.group(5) != null) ? Integer.parseInt(m.group(5)) : 0;

        DayOfWeek dow = switch (yoil) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            case "일" -> DayOfWeek.SUNDAY;
            default -> null;
        };
        if (dow == null) return null;

        LocalDate base = LocalDate.now(kst);
        LocalDate target;

        if ("다음".equals(qual)) {
            target = base.with(TemporalAdjusters.next(dow));
        } else if ("이번".equals(qual) || "오는".equals(qual) || qual == null) {
            LocalDate thisWeek = base.with(TemporalAdjusters.nextOrSame(dow));
            if (thisWeek.equals(base)) {
                int nowH = LocalTime.now(kst).getHour();
                int candH = adjustHourByMeridiem(hour, mer);
                if (nowH > candH) {
                    target = base.with(TemporalAdjusters.next(dow));
                } else {
                    target = thisWeek;
                }
            } else {
                target = thisWeek;
            }
        } else {
            target = base.with(TemporalAdjusters.nextOrSame(dow));
        }

        int h24 = adjustHourByMeridiem(hour, mer);
        return LocalDateTime.of(target, LocalTime.of(h24, minute));
    }

    /** 12시 표기 보정 + 오전/오후(am/pm) 보정 */
    private int adjustHourByMeridiem(int hour, String meridiem) {
        if (meridiem == null) return hour % 24;
        String m = meridiem.toLowerCase(Locale.ROOT);
        boolean isPM = m.contains("후") || m.contains("pm") || m.contains("p.m");
        boolean isAM = m.contains("전") || m.contains("am") || m.contains("a.m");
        int h = hour;
        if (isPM && h < 12) h += 12;
        if (isAM && h == 12) h = 0;
        return h % 24;
    }

    /** 한국어 문장 속 지역명 추출(휴리스틱) */
    private String extractLocationKorean(String text) {
        if (text == null) return null;
        String trimmed = text.trim();

        // 1) "부산 중구 날씨", "강남구 온도", "서울의 날씨"
        Pattern p1 = Pattern.compile("([가-힣A-Za-z\\d·\\-\\s]+?)(?:의)?\\s*(?:오늘|내일|모레)?\\s*(?:날씨|온도)");
        Matcher m1 = p1.matcher(trimmed);
        if (m1.find()) {
            String loc = m1.group(1).trim();
            if (!loc.isBlank() && !loc.contains("오늘") && !loc.contains("내일") && !loc.contains("모레")) return loc;
        }

        // 2) "내일 서울 날씨", "오늘 부산 온도"
        Pattern p2 = Pattern.compile("(?:오늘|내일|모레)\\s*([가-힣A-Za-z\\d·\\-\\s]+?)\\s*(?:날씨|온도)");
        Matcher m2 = p2.matcher(trimmed);
        if (m2.find()) {
            String loc = m2.group(1).trim();
            if (!loc.isBlank()) return loc;
        }

        // 3) "OOO 갈건데/가는데/방문 ..." 등
        Pattern p3 = Pattern.compile("([가-힣A-Za-z\\d·\\-\\s]+?)\\s*(?:갈건데|가는데|갈껀데|갈\\s*예정|방문|여행)\\b");
        Matcher m3 = p3.matcher(trimmed);
        if (m3.find()) {
            String loc = m3.group(1).trim();
            if (!loc.isBlank()) return loc;
        }

        return null;
    }

    private String formatKoreanDateTimeLabel(LocalDateTime ldt, ZoneId kst) {
        LocalDate base = LocalDate.now(kst);
        LocalDate d = ldt.toLocalDate();
        String day = d.equals(base) ? "오늘"
                : d.equals(base.plusDays(1)) ? "내일"
                : d.equals(base.plusDays(2)) ? "모레"
                : d.toString();
        int h24 = ldt.getHour();
        int h12 = h24 % 12 == 0 ? 12 : h24 % 12;
        String ap = (h24 < 12) ? "오전" : "오후";
        int min = ldt.getMinute();
        String time = (min == 0) ? String.format("%s %d시", ap, h12)
                : String.format("%s %d시 %d분", ap, h12, min);
        return day + " " + time;
    }

    private String buildLooseContext(OpenAIChatbotMessageRequest req, String genderNorm) {
        final ZoneId KST = ZoneId.of("Asia/Seoul");
        String today = LocalDate.now(KST).toString();
        String loc = (req.getLocation() == null || req.getLocation().isBlank()) ? "미지정" : req.getLocation();
        String date = (req.getDate() == null || req.getDate().isBlank()) ? today : req.getDate();
        String temp = (req.getTemperature() == null) ? "미지정" : String.valueOf(req.getTemperature());
        String lat  = (req.getLat() == null) ? "미지정" : String.valueOf(req.getLat());
        String lon  = (req.getLon() == null) ? "미지정" : String.valueOf(req.getLon());

        return ("컨텍스트: 오늘(KST)=" + today +
                ", location=" + loc +
                ", date=" + date +
                ", temp(°C)=" + temp +
                ", lat=" + lat +
                ", lon=" + lon +
                ", gender=" + genderNorm +
                ". '오늘/내일/모레/요일/오전/오후' 등은 한국표준시 기준으로 해석할 것.")
                .trim();
    }

    /** DB 코드(0/1)와 문자열을 모두 받아 표준화: 남성/여성/중성 */
    private String normalizeGender(String genderStr, Integer sexCode) {
        if (sexCode != null) {
            if (sexCode == 0) return "남성";
            if (sexCode == 1) return "여성";
        }
        if (genderStr == null) return "중성";
        String g = genderStr.trim().toLowerCase(Locale.ROOT);
        if (g.matches("^(m|male|남|남성)$")) return "남성";
        if (g.matches("^(f|female|여|여성)$")) return "여성";
        if (g.contains("중성") || g.contains("neutral") || g.contains("젠더리스")) return "중성";
        return "중성";
    }

    /** 온도/날씨 기반 서버 Fallback 코디(빈 응답 방지). point/reqTemp 둘 중 있는 값 사용 */
    private String fallbackNeutralOutfit(WeatherDTO point, String genderNorm, Double reqTemp) {
        Double t = null;
        try {
            if (point != null && point.getTmp() != null) t = Double.valueOf(point.getTmp().replaceAll("[^\\d.-]", ""));
        } catch (Exception ignored) {}
        if (t == null && reqTemp != null) t = reqTemp;

        String tier;
        if (t == null) {
            tier = "neutral";
        } else if (t >= 27) {
            tier = "hot";
        } else if (t >= 20) {
            tier = "warm";
        } else if (t >= 10) {
            tier = "cool";
        } else {
            tier = "cold";
        }

        // 기본은 중성 코디. 성별 있으면 살짝 톤만 바꿔 표현(아이템은 비슷)
        String who = switch (genderNorm) {
            case "남성" -> "남성 기준 코디";
            case "여성" -> "여성 기준 코디";
            default -> "젠더-뉴트럴 코디";
        };

        String body;
        switch (tier) {
            case "hot" -> body = "상의: 가벼운 반팔 티셔츠(통기성 소재)\n아우터: 필요 시 얇은 셔츠/린넨 셔츠\n하의: 린넨 반바지 또는 얇은 스커트\n신발: 샌들/가벼운 스니커즈\n팁: 밝은색·린넨/쿨맥스 등 시원한 소재 권장";
            case "warm" -> body = "상의: 얇은 긴팔 티 또는 반팔+얇은 셔츠\n아우터: 라이트 자켓/카디건\n하의: 면바지/롱스커트\n신발: 스니커즈/로퍼\n팁: 실내 냉방 대비 얇은 아우터 한 벌";
            case "cool" -> body = "상의: 맨투맨/니트\n아우터: 경량 자켓/트러커\n하의: 데님/슬랙스\n신발: 스니커즈\n팁: 아침·저녁 쌀쌀하니 레이어링";
            case "cold" -> body = "상의: 히트텍+니트\n아우터: 두꺼운 코트/패딩\n하의: 기모 슬랙스/데님\n신발: 부츠/보온성 스니커즈\n팁: 목도리·장갑 등 보온 소품";
            default -> body = "상의: 반팔 또는 얇은 셔츠\n아우터: 가벼운 자켓\n하의: 면바지/스커트\n신발: 스니커즈\n팁: 상황에 맞게 레이어링";
        }

        return String.format("[%s]\n%s", who, body);
    }
}
