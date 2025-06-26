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

import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SelectedWeatherService {
    // 날씨 API
    public List<SelectedWeatherDTO> getWeatherApi(GridDTO dto, LocalDate date) {
        // API url 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String apiUrl = "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst?pageNo=1&numOfRows=1000&dataType=XML";
        String base_date = date.minusDays(2).format(formatter);
        String base_time = "1700";

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

    public List<SelectedWeatherDTO> weatherDataParsing(String weatherData) {
        List<SelectedWeatherDTO> weatherList = new ArrayList<>();

        try {
            // XML 형식의 데이터 파싱(parsing) - item 태그의 부분(날씨 데이터) 가져오기
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(weatherData.getBytes(StandardCharsets.UTF_8)));
            NodeList items = document.getElementsByTagName("item");

            // 데이터를 임시로 담아둘 리스트 설정
            List<String> tmpList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            List<String> timeList = new ArrayList<>();

            // 예보자료 카테고리, 예보일, 예보시간, 예보 정보 추출
            for (int i = 0; i < items.getLength(); i++) {
                LocalTime parseTime = LocalTime.parse(items.item(0).getChildNodes().item(4).getTextContent(),
                        DateTimeFormatter.ofPattern("HHmm"));
                if (parseTime.plusHours(1) == LocalTime.now()) {
                    i = 1;
                } else if (parseTime.plusHours(2) == LocalTime.now()) {
                    i = 2;
                }

                String category = items.item(i).getChildNodes().item(2).getTextContent();

                String fcstDate = items.item(i).getChildNodes().item(3).getTextContent();
                String fcstTime = items.item(i).getChildNodes().item(4).getTextContent();

                String value = items.item(i).getChildNodes().item(5).getTextContent();

                // 예보 정보 코드값 변경 후 리스트에 모으기
                if (category.equals("TMP")) {
                    tmpList.add(value);
                    dateList.add(fcstDate);
                    timeList.add(fcstTime);
                } else
                    continue;
            }

            // 모은 리스트의 값을 순서대로 DTO에 저장
            for (int i = 0; i < 24; i++) {
                SelectedWeatherDTO selectedweatherDTO = new SelectedWeatherDTO();

                selectedweatherDTO.setTmp(tmpList.get(i));

                // 예보 날짜 및 시간 파싱
                LocalDate parseDate = LocalDate.parse(dateList.get(i), DateTimeFormatter.BASIC_ISO_DATE);
                LocalTime parseTime = LocalTime.parse(timeList.get(i), DateTimeFormatter.ofPattern("HHmm"));
                selectedweatherDTO.setFcstDate(parseDate);
                selectedweatherDTO.setFcstTime(parseTime);

                // 리스트에 DTO 값 넣기
                weatherList.add(selectedweatherDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(weatherList.toString());
        return weatherList;
    }
}
