package com.example.tempfit.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.WeatherDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WeatherService {

    // 날씨 API
    public List<WeatherDTO> getWeatherApi(GridDTO dto) {
        // 날짜, 시간 포맷팅
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        // 발효일자 설정
        LocalDate date = null;
        if (LocalTime.now().isBefore(LocalTime.of(3, 0, 0))) {
            date = LocalDate.now().minusDays(1);
        } else {
            date = LocalDate.now();
        }

        // 발효시간 리스트 설정
        LocalTime time = LocalTime.now();
        LocalTime[] timeList = new LocalTime[8];
        timeList[0] = LocalTime.of(2, 0, 0);
        for (int i = 1; i < 8; i++) {
            timeList[i] = timeList[i - 1].plusHours(3);
        }

        // API url 설정
        String apiUrl = "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst?pageNo=1&numOfRows=1000&dataType=XML";
        String base_date = date.format(dateFormatter);
        String base_time = "";

        if (date == LocalDate.now().minusDays(1)) {
            base_time = LocalTime.of(23, 0, 0).format(timeFormatter);
        } else {
            for (LocalTime localTime : timeList) {
                if (localTime.getHour() == time.getHour()) {
                    base_time = localTime.minusHours(3).format(timeFormatter);
                } else if (time.getHour() - localTime.getHour() < 3 && time.getHour() - localTime.getHour() > 0
                        && localTime.getHour() != time.getHour()) {
                    base_time = localTime.format(timeFormatter);
                }
            }
        }

        // 좌표 얻기
        String nx = dto.getNx();
        String ny = dto.getNy();
        String authKey = "jemgMb34RtGpoDG9-EbRYw";

        try {
            String url = apiUrl
                    + "&base_date=" + base_date
                    + "&base_time=" + base_time
                    + "&nx=" + nx
                    + "&ny=" + ny
                    + "&authKey=" + authKey;

            // API 연동
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            System.out.println(url);
            // API 에서 받은 값 String 형태로 변환 후 리턴
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String input;

                while ((input = bufferedReader.readLine()) != null) {
                    stringBuffer.append(input);
                }
                bufferedReader.close();
                // System.out.println(stringBuffer.toString());
                return weatherDataParsing(stringBuffer.toString());
            } else {
                System.out.println("GET 요청 실패");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<WeatherDTO> weatherDataParsing(String weatherData) {
        List<WeatherDTO> weatherList = new ArrayList<>();

        try {
            // XML 형식의 데이터 파싱(parsing) - item 태그의 부분(날씨 데이터) 가져오기
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(weatherData.getBytes(StandardCharsets.UTF_8)));
            NodeList items = document.getElementsByTagName("item");

            // 데이터를 임시로 담아둘 리스트 설정
            List<String> ptyList = new ArrayList<>();
            List<String> skyList = new ArrayList<>();
            List<String> tmpList = new ArrayList<>();
            List<String> popList = new ArrayList<>();
            List<String> rehList = new ArrayList<>();
            List<String> wsdList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            List<String> timeList = new ArrayList<>();

            // 예보자료 카테고리, 예보일, 예보시간, 예보 정보 추출
            for (int i = 0; i < items.getLength(); i++) {
                String category = items.item(i).getChildNodes().item(2).getTextContent();

                String fcstDate = items.item(i).getChildNodes().item(3).getTextContent();
                String fcstTime = items.item(i).getChildNodes().item(4).getTextContent();

                String value = items.item(i).getChildNodes().item(5).getTextContent();

                // 예보 정보 코드값 변경 후 리스트에 모으기
                if (category.equals("PTY")) {
                    ptyList.add(value);
                } else if (category.equals("SKY")) {
                    skyList.add(value);
                } else if (category.equals("TMP")) {
                    tmpList.add(value);
                } else if (category.equals("POP")) {
                    popList.add(value);
                } else if (category.equals("REH")) {
                    rehList.add(value);
                } else if (category.equals("WSD")) {
                    wsdList.add(value);
                    dateList.add(fcstDate);
                    timeList.add(fcstTime);
                } else
                    continue;
            }

            LocalTime parsedTime = LocalTime.parse(items.item(0).getChildNodes().item(4).getTextContent(),
                    DateTimeFormatter.ofPattern("HHmm"));
            int j = 2;
            if (parsedTime.plusHours(1).getHour() == LocalTime.now().getHour()) {
                ptyList.remove(0);
                skyList.remove(0);
                tmpList.remove(0);
                popList.remove(0);
                rehList.remove(0);
                wsdList.remove(0);
                dateList.remove(0);
                timeList.remove(0);
            } else if (parsedTime.plusHours(2).getHour() == LocalTime.now().getHour()) {
                while (j > 0) {
                    ptyList.remove(0);
                    skyList.remove(0);
                    tmpList.remove(0);
                    popList.remove(0);
                    rehList.remove(0);
                    wsdList.remove(0);
                    dateList.remove(0);
                    timeList.remove(0);
                    j--;
                }
            }

            // 모은 리스트의 값을 순서대로 DTO에 저장
            for (int i = 0; i < rehList.size(); i++) {
                WeatherDTO weatherDTO = new WeatherDTO();

                switch (ptyList.get(i)) {
                    case "0":
                        weatherDTO.setPty("강수없음");
                        break;
                    case "1":
                        weatherDTO.setPty("비");
                        break;
                    case "2":
                        weatherDTO.setPty("비 또는 눈");
                        break;
                    case "3":
                        weatherDTO.setPty("눈");
                        break;
                    case "5":
                        weatherDTO.setPty("이슬비");
                        break;
                    case "6":
                        weatherDTO.setPty("이슬비 또는 싸라기눈");
                        break;
                    case "7":
                        weatherDTO.setPty("싸라기눈");
                        break;
                }

                switch (skyList.get(i)) {
                    case "1":
                        weatherDTO.setSky("맑음");
                        break;
                    case "3":
                        weatherDTO.setSky("구름 많음");
                        break;
                    case "4":
                        weatherDTO.setSky("흐림");
                        break;
                }

                weatherDTO.setPop(popList.get(i));

                weatherDTO.setTmp(tmpList.get(i));
                weatherDTO.setReh(rehList.get(i) + "%");
                weatherDTO.setWsd(wsdList.get(i) + "m/s");

                // 예보 날짜 및 시간 파싱
                LocalDate parseDate = LocalDate.parse(dateList.get(i), DateTimeFormatter.BASIC_ISO_DATE);
                LocalTime parseTime = LocalTime.parse(timeList.get(i), DateTimeFormatter.ofPattern("HHmm"));
                weatherDTO.setFcstDate(parseDate);
                weatherDTO.setFcstTime(parseTime);

                // 리스트에 DTO 값 넣기
                weatherList.add(weatherDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(weatherList.toString());
        return weatherList;
    }

    // 좌표 변환
    public GridDTO changeCoords(CoordsDTO dto) {
        // API 주소
        String url = "https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat" + "?lon="
                + dto.getLon() + "&lat=" + dto.getLat() + "&help=0&authKey=jemgMb34RtGpoDG9-EbRYw";

        try {
            // API 연동
            GridDTO gridDTO = new GridDTO();
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            System.out.println(url);

            // API 에서 받은 값 String 형태로 변환
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String input;

                while ((input = bufferedReader.readLine()) != null) {
                    stringBuffer.append(input);
                }

                bufferedReader.close();
                // 받은 좌표값 DTO에 저장
                String coordData = stringBuffer.toString().replaceAll("\\\\s", "");
                gridDTO.setNx(coordData.substring(71, 73));
                gridDTO.setNy(coordData.substring(75, 78));

                System.out.println(coordData);
                System.out.println(gridDTO.getNx() + "," + gridDTO.getNy());
                return gridDTO;
            } else {
                System.out.println("GET 요청 실패");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
