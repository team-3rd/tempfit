// src/main/java/com/example/tempfit/service/OpenAIChatbotService.java
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

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** 의상 파트 구분 */
    private enum Part { TOP, OUTER, BOTTOM, SHOES, ALL }

    /** 메인 진입 (세션 컨텍스트는 탑레벨 클래스만 사용) */
    public OpenAIChatbotMessageResponse chat(OpenAIChatbotMessageRequest request,
                                             ChatSessionContext ctx) {
        try {
            final ZoneId KST = ZoneId.of("Asia/Seoul");
            final LocalDate today = LocalDate.now(KST);
            final LocalDate tomorrow = today.plusDays(1);

            final String userMsg = request.getMessage();
            final String lower   = userMsg.toLowerCase(Locale.ROOT);

            // 성별 표준화(남=0, 여=1, 그 외/비로그인=중성)
            final String genderNorm = normalizeGender(request.getGender(), request.getSexCode());

            // ── 의도 파악 ─────────────────────────────────────────────────────────
            boolean outfitToday = matchesAnyRegex(lower,
                    "오늘.*(뭐\\s*입|입지|코디|룩|옷\\s*추천)", "(뭐\\s*입|입지|코디|룩|옷\\s*추천).*오늘");
            boolean outfitTomorrow = matchesAnyRegex(lower,
                    "내일.*(뭐\\s*입|입지|코디|룩|옷\\s*추천)", "(뭐\\s*입|입지|코디|룩|옷\\s*추천).*내일");

            boolean askWeatherToday = matchesAnyRegex(lower,
                    "오늘.*(날씨|기온|온도)", "(현재|지금)\\s*(날씨|기온|온도)");
            boolean askWeatherTomorrow = matchesAnyRegex(lower,
                    "내일.*(날씨|기온|온도)");

            Integer hourOffset = extractHourOffset(lower);
            boolean outfitByOffset     = (hourOffset != null) && matchesAnyRegex(lower, "(뭐\\s*입|입지|코디|룩|옷\\s*추천)");
            boolean askWeatherByOffset = (hourOffset != null) && matchesAnyRegex(lower, "(날씨|기온|온도)");

            LocalDateTime absoluteDT = extractAbsoluteDateTime(userMsg, KST);
            if (absoluteDT == null) absoluteDT = extractWeekdayDateTime(userMsg, KST);

            // 파트만 물었는지(“하의는?”, “신발은?” 등)
            Part focusPart = extractPart(userMsg); // null이면 전체
            boolean askedAnyPartOnly = (focusPart != null) && !matchesAnyRegex(lower, "(뭐\\s*입|입지|코디|옷\\s*추천)");

            // 지역 추출 → 좌표(없으면 요청 lat/lon 사용)
            String mentionedLoc = extractLocationKorean(userMsg);
            Double lat = request.getLat();
            Double lon = request.getLon();
            String  locNameForDisplay = request.getLocation();

            if (mentionedLoc != null && !mentionedLoc.isBlank()) {
                GeoPoint gp = geocodingService.geocode(mentionedLoc);
                if (gp != null) {
                    lat = gp.lat; lon = gp.lon; locNameForDisplay = mentionedLoc;
                }
            }

            // 시각 결정(우선순위: 상대▶절대▶내일▶오늘▶세션)
            LocalDateTime targetDT =
                    (hourOffset != null) ? LocalDateTime.now(KST).plusHours(hourOffset)
                    : (absoluteDT != null) ? absoluteDT
                    : (askWeatherTomorrow || outfitTomorrow) ? LocalDateTime.of(tomorrow, LocalTime.of(12, 0))
                    : (askWeatherToday || outfitToday) ? LocalDateTime.now(KST)
                    : (askedAnyPartOnly && ctx.getLastTargetDT() != null) ? ctx.getLastTargetDT()
                    : LocalDateTime.now(KST);

            // 좌표 보정(없으면 세션에서 계승)
            if ((lat == null || lon == null) && ctx.getLastLat() != null && ctx.getLastLon() != null) {
                lat = ctx.getLastLat(); lon = ctx.getLastLon();
                if (locNameForDisplay == null) locNameForDisplay = ctx.getLastLocationLabel();
            }

            // 요청 유형 결정
            boolean wantsOutfit = outfitToday || outfitTomorrow || outfitByOffset || askedAnyPartOnly
                                  || matchesAnyRegex(lower, "(뭐\\s*입|입지|코디|룩|옷\\s*추천)");
            boolean wantsWeatherOnly = (askWeatherToday || askWeatherTomorrow || askWeatherByOffset || absoluteDT != null)
                                       && !wantsOutfit;

            if (wantsOutfit || wantsWeatherOnly) {
                if (lat == null || lon == null) {
                    return new OpenAIChatbotMessageResponse("정확한 추천을 위해 위치 권한 또는 지역명을 알려 주세요.");
                }

                GridDTO grid = toGrid(lat, lon);
                if (grid == null) {
                    return new OpenAIChatbotMessageResponse("위치 좌표 변환에 실패했어요. 잠시 후 다시 시도해 주세요.");
                }

                List<WeatherDTO> list = weatherService.getWeatherApi(grid);
                if (list == null || list.isEmpty()) {
                    return new OpenAIChatbotMessageResponse("날씨 정보를 가져오지 못했어요. 잠시 후 다시 시도해 주세요.");
                }

                WeatherDTO point = pickClosest(list, targetDT);
                String timeLabel = formatKoreanDateTimeLabel(targetDT, KST);
                String locPrefix = (locNameForDisplay != null && !locNameForDisplay.isBlank())
                        ? locNameForDisplay + " " : "";

                // 세션 컨텍스트 업데이트(다음 질문에서 이어받기)
                ctx.setLastTargetDT(targetDT);
                ctx.setLastLat(lat);
                ctx.setLastLon(lon);
                ctx.setLastLocationLabel(locNameForDisplay);

                if (wantsWeatherOnly) {
                    return new OpenAIChatbotMessageResponse(
                            buildWeatherSummary(locPrefix + timeLabel, point));
                }

                // 의상 추천 (파트 지정 시 해당 파트만)
                String preface = buildWeatherSummary(locPrefix + timeLabel, point) + "\n";
                String ai = aiOutfitRecommendation(
                        preface, point, locNameForDisplay,
                        (point.getFcstDate() != null ? point.getFcstDate().toString() : null),
                        genderNorm, (focusPart == null ? Part.ALL : focusPart));

                if (ai == null || ai.isBlank() || ai.startsWith("(코디")) {
                    String fb = fallbackNeutralOutfit(point, genderNorm, null, (focusPart == null ? Part.ALL : focusPart));
                    return new OpenAIChatbotMessageResponse(preface + fb);
                }
                return new OpenAIChatbotMessageResponse(preface + ai);
            }

            // ── 일반 의상/패션/날씨 범위 질문은 OpenAI 위임 ───────────────────────
            String systemPolicy = ("""
                너는 TempFit의 친절한 의상·패션·날씨 전용 AI 챗봇이다. 답변 범위를 의류, 패션, 코디, 스타일링, 계절/기온·날씨에 따른 옷차림, 소재/세탁/보관, 사이즈·핏 조언으로 제한한다.
                '오늘 뭐입지?' 같은 문장은 반드시 관련이라고 간주하라. 한국어로 간결히, 상의→아우터→하의→신발 순 1~3개.
                사용자 성별이 '%s'면 그 기준(남성/여성)으로, 정보가 없거나 '중성'이면 젠더-뉴트럴하게 제안.
                """).formatted(genderNorm).replace("\n", " ").trim();

            String systemContext = buildLooseContext(request, genderNorm);

            String payload = """
                {"model":"%s","messages":[
                  {"role":"system","content":%s},
                  {"role":"system","content":%s},
                  {"role":"user","content":%s}
                ],"temperature":0.7}
                """.formatted(
                    model,
                    objectMapper.writeValueAsString(systemPolicy),
                    objectMapper.writeValueAsString(systemContext),
                    objectMapper.writeValueAsString(userMsg)
                );

            String json = openAiWebClient.post()
                    .uri(b -> b.path("/chat/completions").build())
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
                return new OpenAIChatbotMessageResponse(
                        fallbackNeutralOutfit(null, genderNorm, request.getTemperature(), Part.ALL));
            }
            JsonNode root = objectMapper.readTree(json);
            if (root.has("__transport_error") || root.has("error")) {
                return new OpenAIChatbotMessageResponse(
                        fallbackNeutralOutfit(null, genderNorm, request.getTemperature(), Part.ALL));
            }
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText("");
                if (!content.isBlank()) return new OpenAIChatbotMessageResponse(content.trim());
            }
            return new OpenAIChatbotMessageResponse(
                    fallbackNeutralOutfit(null, genderNorm, request.getTemperature(), Part.ALL));

        } catch (Exception e) {
            log.error("chat() failed", e);
            return new OpenAIChatbotMessageResponse("오류가 발생했어요. 잠시 후 다시 시도해 주세요.");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private GridDTO toGrid(double lat, double lon) {
        CoordsDTO c = new CoordsDTO(); c.setLat(lat); c.setLon(lon);
        return weatherService.changeCoords(c);
    }

    private WeatherDTO pickClosest(List<WeatherDTO> list, LocalDateTime targetDT) {
        return list.stream().min(Comparator.comparingLong(w -> {
            LocalDate d = Optional.ofNullable(w.getFcstDate()).orElse(targetDT.toLocalDate());
            LocalTime t = Optional.ofNullable(w.getFcstTime()).orElse(LocalTime.NOON);
            return Math.abs(Duration.between(LocalDateTime.of(d, t), targetDT).toMinutes());
        })).orElse(null);
    }

    private String buildWeatherSummary(String label, WeatherDTO w) {
        String t = (w.getTmp() != null) ? w.getTmp() + "℃" : "-";
        String sky = Optional.ofNullable(w.getSky()).orElse("-");
        String reh = Optional.ofNullable(w.getReh()).orElse("-");
        String wsd = Optional.ofNullable(w.getWsd()).orElse("-");
        String when = (w.getFcstDate()!=null && w.getFcstTime()!=null) ?
                String.format("(%s %02d:00)", w.getFcstDate(), w.getFcstTime().getHour()) : "";
        return String.format("%s %s 날씨: %s, 기온: %s, 습도: %s, 풍속: %s", label, when, sky, t, reh, wsd);
    }

    private String aiOutfitRecommendation(String preface, WeatherDTO point, String location, String dateIso,
                                          String genderNorm, Part focusPart) {
        try {
            String ctx = String.format(
                "지역: %s | 날짜: %s | 기온(°C): %s | 하늘상태: %s | 습도: %s | 풍속: %s | 성별: %s | focusPart: %s",
                (location==null||location.isBlank())?"미지정":location,
                (dateIso==null||dateIso.isBlank())?"미지정":dateIso,
                Optional.ofNullable(point.getTmp()).orElse("-"),
                Optional.ofNullable(point.getSky()).orElse("-"),
                Optional.ofNullable(point.getReh()).orElse("-"),
                Optional.ofNullable(point.getWsd()).orElse("-"),
                genderNorm,
                toKoLabel(focusPart)
            );

            String system = """
              너는 TempFit의 코디 어시스턴트다. 주어진 컨텍스트를 반드시 활용해 한국어로 간결 제안.
              성별에 맞춰 자연스럽게 표현하되, '중성'이면 젠더-뉴트럴.
              기본 포맷은 상의→아우터→하의→신발(각 1~2개).
              단, focusPart가 지정되면 그 파트만 1~2개로 짧게 제안하고 다른 파트는 출력하지 마라.
              """.replace("\n", " ").trim();

            String user = "컨텍스트 기반으로 코디를 제안해줘.\n컨텍스트: " + ctx;
            String payload = """
              {"model":"%s","messages":[
                {"role":"system","content":%s},
                {"role":"user","content":%s}
              ],"temperature":0.7}
            """.formatted(
                model,
                objectMapper.writeValueAsString(system),
                objectMapper.writeValueAsString(user)
            );

            String json = openAiWebClient.post()
                    .uri(b -> b.path("/chat/completions").build())
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
                return content.isBlank()? "(코디 빈 응답)" : content.trim();
            }
            return "(코디 유효한 응답 없음)";
        } catch (Exception e) {
            log.error("aiOutfitRecommendation failed", e);
            return "(코디 생성 중 오류)";
        }
    }

    private String toKoLabel(Part p) {
        if (p == null) return "전체";
        return switch (p) {
            case TOP -> "상의";
            case OUTER -> "아우터";
            case BOTTOM -> "하의";
            case SHOES -> "신발";
            default -> "전체";
        };
    }

    private boolean matchesAnyRegex(String s, String... patterns) {
        if (s == null) return false;
        for (String p : patterns) if (Pattern.compile(p).matcher(s).find()) return true;
        return false;
    }

    private Integer extractHourOffset(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{1,2})\\s*시간\\s*(뒤|후)").matcher(s);
        if (m.find()) { int h = Integer.parseInt(m.group(1)); if (h>=0 && h<=72) return h; }
        Matcher m2 = Pattern.compile("(\\d{1,2})\\s*(h|hr|hrs)\\b").matcher(s);
        if (m2.find()) { int h = Integer.parseInt(m2.group(1)); if (h>=0 && h<=72) return h; }
        return null;
    }

    private LocalDateTime extractAbsoluteDateTime(String text, ZoneId kst) {
        if (text == null) return null;
        String s = text.trim();
        LocalDate base = LocalDate.now(kst);

        Pattern pDate = Pattern.compile(
          "(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})(?:\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:[:]|시(?!간))\\s*(\\d{1,2})?)?",
          Pattern.CASE_INSENSITIVE);
        Matcher md = pDate.matcher(s);
        if (md.find()) {
            int y = Integer.parseInt(md.group(1));
            int m = Integer.parseInt(md.group(2));
            int d = Integer.parseInt(md.group(3));
            Integer hour = (md.group(5) != null) ? Integer.parseInt(md.group(5)) : 12;
            int minute = (md.group(6) != null) ? Integer.parseInt(md.group(6)) : 0;
            hour = adjustHourByMeridiem(hour, md.group(4));
            return LocalDateTime.of(y, m, d, hour, minute);
        }

        Pattern pAbs = Pattern.compile(
          "(오늘|내일|모레)?\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:(?:[:]|시(?!간))\\s*(\\d{1,2})?)",
          Pattern.CASE_INSENSITIVE);
        Matcher ma = pAbs.matcher(s);
        if (ma.find()) {
            String w = ma.group(1);
            String mer = ma.group(2);
            int h = Integer.parseInt(ma.group(3));
            int mi = (ma.group(4) != null) ? Integer.parseInt(ma.group(4)) : 0;
            if ("내일".equals(w)) base = base.plusDays(1);
            else if ("모레".equals(w)) base = base.plusDays(2);
            h = adjustHourByMeridiem(h, mer);
            return LocalDateTime.of(base, LocalTime.of(h, mi));
        }
        return null;
    }

    private LocalDateTime extractWeekdayDateTime(String text, ZoneId kst) {
        if (text == null) return null;
        String s = text.trim();
        Pattern p = Pattern.compile("(이번|다음|오는)?\\s*(월|화|수|목|금|토|일)요일\\s*(오전|오후|am|pm|a\\.m\\.|p\\.m\\.)?\\s*(\\d{1,2})\\s*(?:(?:[:]|시(?!간))\\s*(\\d{1,2})?)?",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);
        if (!m.find()) return null;

        String qual = m.group(1);
        String yoil = m.group(2);
        String mer  = m.group(3);
        int h = Integer.parseInt(m.group(4));
        int mi = (m.group(5) != null) ? Integer.parseInt(m.group(5)) : 0;

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
        LocalDate base = LocalDate.now(kst);
        LocalDate target;
        if ("다음".equals(qual)) target = base.with(TemporalAdjusters.next(dow));
        else {
            LocalDate thisWeek = base.with(TemporalAdjusters.nextOrSame(dow));
            if (thisWeek.equals(base)) {
                int nowH = LocalTime.now(kst).getHour();
                int cand = adjustHourByMeridiem(h, mer);
                target = (nowH > cand) ? base.with(TemporalAdjusters.next(dow)) : thisWeek;
            } else target = thisWeek;
        }
        int h24 = adjustHourByMeridiem(h, mer);
        return LocalDateTime.of(target, LocalTime.of(h24, mi));
    }

    private int adjustHourByMeridiem(int hour, String meridiem) {
        if (meridiem == null) return hour % 24;
        String m = meridiem.toLowerCase(Locale.ROOT);
        boolean pm = m.contains("후") || m.contains("pm") || m.contains("p.m");
        boolean am = m.contains("전") || m.contains("am") || m.contains("a.m");
        int h = hour;
        if (pm && h < 12) h += 12;
        if (am && h == 12) h = 0;
        return h % 24;
    }

    private String extractLocationKorean(String text) {
        if (text == null) return null;
        String s = text.trim();
        Matcher m1 = Pattern.compile("([가-힣A-Za-z\\d·\\-\\s]+?)(?:의)?\\s*(?:오늘|내일|모레)?\\s*(?:날씨|온도)").matcher(s);
        if (m1.find()) return m1.group(1).trim();
        Matcher m2 = Pattern.compile("(?:오늘|내일|모레)\\s*([가-힣A-Za-z\\d·\\-\\s]+?)\\s*(?:날씨|온도)").matcher(s);
        if (m2.find()) return m2.group(1).trim();
        Matcher m3 = Pattern.compile("([가-힣A-Za-z\\d·\\-\\s]+?)\\s*(?:갈건데|가는데|갈껀데|방문|여행)\\b").matcher(s);
        if (m3.find()) return m3.group(1).trim();
        return null;
    }

    private String formatKoreanDateTimeLabel(LocalDateTime ldt, ZoneId kst) {
        LocalDate base = LocalDate.now(kst);
        LocalDate d = ldt.toLocalDate();
        String day = d.equals(base) ? "오늘" : d.equals(base.plusDays(1)) ? "내일"
                : d.equals(base.plusDays(2)) ? "모레" : d.toString();
        int h24 = ldt.getHour();
        int h12 = (h24 % 12 == 0) ? 12 : h24 % 12;
        String ap = (h24 < 12) ? "오전" : "오후";
        int mi = ldt.getMinute();
        String time = (mi == 0) ? String.format("%s %d시", ap, h12) : String.format("%s %d시 %d분", ap, h12, mi);
        return day + " " + time;
    }

    private String buildLooseContext(OpenAIChatbotMessageRequest req, String genderNorm) {
        final ZoneId KST = ZoneId.of("Asia/Seoul");
        String today = LocalDate.now(KST).toString();
        String loc = (req.getLocation()==null||req.getLocation().isBlank())?"미지정":req.getLocation();
        String date = (req.getDate()==null||req.getDate().isBlank())?today:req.getDate();
        String temp = (req.getTemperature()==null)?"미지정":String.valueOf(req.getTemperature());
        String lat  = (req.getLat()==null)?"미지정":String.valueOf(req.getLat());
        String lon  = (req.getLon()==null)?"미지정":String.valueOf(req.getLon());
        return ("컨텍스트: 오늘(KST)="+today+", location="+loc+", date="+date+", temp(°C)="+temp+
                ", lat="+lat+", lon="+lon+", gender="+genderNorm+".").trim();
    }

    private String normalizeGender(String genderStr, Integer sexCode) {
        if (sexCode != null) { if (sexCode==0) return "남성"; if (sexCode==1) return "여성"; }
        if (genderStr == null) return "중성";
        String g = genderStr.trim().toLowerCase(Locale.ROOT);
        if (g.matches("^(m|male|남|남성)$")) return "남성";
        if (g.matches("^(f|female|여|여성)$")) return "여성";
        return "중성";
    }

    private Part extractPart(String text) {
        if (text == null) return null;
        String s = text;
        if (s.matches(".*(신발|운동화|스니커즈|로퍼|샌들|부츠).*")) return Part.SHOES;
        if (s.matches(".*(아우터|자켓|재킷|가디건|바람막이|코트|점퍼).*")) return Part.OUTER;
        if (s.matches(".*(하의|바지|팬츠|슬랙스|데님|진|치마|스커트).*")) return Part.BOTTOM;
        if (s.matches(".*(상의|티|티셔츠|셔츠|블라우스|니트|맨투맨|후드).*")) return Part.TOP;
        return null;
    }

    private String fallbackNeutralOutfit(WeatherDTO point, String genderNorm, Double reqTemp, Part focus) {
        Double t = null;
        try { if (point!=null && point.getTmp()!=null) t = Double.valueOf(point.getTmp().replaceAll("[^\\d.-]","")); } catch (Exception ignored) {}
        if (t == null && reqTemp != null) t = reqTemp;

        String tier;
        if (t == null) tier = "neutral";
        else if (t >= 27) tier = "hot";
        else if (t >= 20) tier = "warm";
        else if (t >= 10) tier = "cool";
        else tier = "cold";

        Map<Part, String> map = new EnumMap<>(Part.class);
        if ("hot".equals(tier)) {
            map.put(Part.TOP,    "가벼운 반팔/린넨 셔츠");
            map.put(Part.OUTER,  "얇은 셔츠/린넨 셔츠");
            map.put(Part.BOTTOM, "린넨 반바지 또는 얇은 스커트");
            map.put(Part.SHOES,  "샌들 또는 통기성 스니커즈");
        } else if ("warm".equals(tier)) {
            map.put(Part.TOP,    "얇은 긴팔 또는 반팔+셔츠");
            map.put(Part.OUTER,  "라이트 자켓/카디건");
            map.put(Part.BOTTOM, "면바지/롱스커트");
            map.put(Part.SHOES,  "스니커즈/로퍼");
        } else if ("cool".equals(tier)) {
            map.put(Part.TOP,    "맨투맨/니트");
            map.put(Part.OUTER,  "경량 자켓/트러커");
            map.put(Part.BOTTOM, "데님/슬랙스");
            map.put(Part.SHOES,  "스니커즈");
        } else if ("cold".equals(tier)) {
            map.put(Part.TOP,    "히트텍+니트");
            map.put(Part.OUTER,  "두꺼운 코트/패딩");
            map.put(Part.BOTTOM, "기모 슬랙스/데님");
            map.put(Part.SHOES,  "부츠/보온 스니커즈");
        } else {
            map.put(Part.TOP,    "반팔 또는 얇은 셔츠");
            map.put(Part.OUTER,  "가벼운 자켓");
            map.put(Part.BOTTOM, "면바지/스커트");
            map.put(Part.SHOES,  "스니커즈");
        }

        String who = switch (genderNorm) {
            case "남성" -> "[남성 기준]";
            case "여성" -> "[여성 기준]";
            default     -> "[젠더-뉴트럴]";
        };

        if (focus != null && focus != Part.ALL) {
            return who + " " + toKoLabel(focus) + ": " + map.get(focus);
        }
        return who + "\n"
            + "상의: "   + map.get(Part.TOP)    + "\n"
            + "아우터: " + map.get(Part.OUTER)  + "\n"
            + "하의: "   + map.get(Part.BOTTOM) + "\n"
            + "신발: "   + map.get(Part.SHOES);
    }
}
