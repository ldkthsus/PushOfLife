package com.example.PushOfLife.service;

import com.example.PushOfLife.entity.AedAvailableEntity;
import com.example.PushOfLife.entity.AedEntity;
import com.example.PushOfLife.repository.AedAvailableRepository;
import com.example.PushOfLife.repository.AedRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CsvToDataBaseService {

    private final AedRepository aedRepository;  // AedEntity용 JPA 리포지토리
    private final AedAvailableRepository aedAvailableRepository;  // AedAvailableEntity용 JPA 리포지토리

    public void processCsv(String csvFilePath) {
        try (Reader reader = new FileReader(csvFilePath)) {
            // CSV 데이터를 파싱, 첫 번째 줄을 헤더로 처리
//            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().withQuote('"').parse(reader);


            for (CSVRecord record : records) {
                // CSVRecord를 통해 각 필드를 가져옴, 필드가 없는 경우 기본값 처리
                String aedAddress = record.get("buildAddress"); // 설치 기관 주소
                String aedLocation = record.get("buildPlace");  // 설치 위치
//                String aedDetail = Optional.ofNullable(record.get("detail")).orElse(null);  // detail 필드, 없으면 null
                Double longitude = parseDoubleSafe(record.get("wgs84Lon")); // 경도
                Double latitude = parseDoubleSafe(record.get("wgs84Lat"));  // 위도
                String aedNumber = record.get("clerkTel");   // 설치 기관 전화번호

                // AedEntity에 데이터를 매핑
                AedEntity aedEntity = AedEntity.builder()
                        .aedAddress(aedAddress)
                        .aedLocation(aedLocation)
//                        .aedDetail(aedDetail)  // detail 필드, 없으면 null
                        .aedLongitude(longitude)
                        .aedLatitude(latitude)
                        .aedNumber(aedNumber)
                        .build();

                // AedEntity를 데이터베이스에 저장
                aedEntity = aedRepository.save(aedEntity);
                System.out.println("AedEntity 저장됨");

                System.out.println("1 : " + record.get("buildAddress"));
                System.out.println("2 : " + record.get("buildPlace"));
//                System.out.println("3 : " + record.get("detail"));
                System.out.println("4 : " + record.get("wgs84Lon"));
                System.out.println("5 : " + record.get("wgs84Lat"));
                System.out.println("6 : " + record.get("clerkTel"));
                System.out.println("7 : " + record.get("monSttTme"));
                System.out.println("8 : " + record.get("monEndTme"));
                System.out.println("9 : " + record.get("tueSttTme"));
                System.out.println("10 : " + record.get("tueEndTme"));
                System.out.println("11 : " + record.get("wedSttTme"));
                System.out.println("12 : " + record.get("wedEndTme"));
                System.out.println("13 : " + record.get("thuSttTme"));
                System.out.println("14 : " + record.get("thuEndTme"));


                // AedAvailableEntity에 데이터를 매핑
                AedAvailableEntity aedAvailableEntity = AedAvailableEntity.builder()
                        .aedMonStTime(safeParseTime(record.get("monSttTme")))   // 월요일 시작 시간
                        .aedMonEndTime(safeParseTime(record.get("monEndTme")))  // 월요일 종료 시간
                        .aedTueStTime(safeParseTime(record.get("tueSttTme")))   // 화요일 시작 시간
                        .aedTueEndTime(safeParseTime(record.get("tueEndTme")))  // 화요일 종료 시간
                        .aedWedStTime(safeParseTime(record.get("wedSttTme")))   // 수요일 시작 시간
                        .aedWedEndTime(safeParseTime(record.get("wedEndTme")))  // 수요일 종료 시간
                        .aedThuStTime(safeParseTime(record.get("thuSttTme")))   // 목요일 시작 시간
                        .aedThuEndTime(safeParseTime(record.get("thuEndTme")))  // 목요일 종료 시간
                        .aedFriStTime(safeParseTime(record.get("friSttTme")))   // 금요일 시작 시간
                        .aedFriEndTime(safeParseTime(record.get("friEndTme")))  // 금요일 종료 시간
                        .aedSatStTime(safeParseTime(record.get("satSttTme")))   // 토요일 시작 시간
                        .aedSatEndTime(safeParseTime(record.get("satEndTme")))  // 토요일 종료 시간
                        .aedSunStTime(safeParseTime(record.get("sunSttTme")))   // 일요일 시작 시간
                        .aedSunEndTime(safeParseTime(record.get("sunEndTme")))  // 일요일 종료 시간
                        .aedHolStTime(safeParseTime(record.get("holSttTme")))   // 공휴일 시작 시간
                        .aedHolEndTime(safeParseTime(record.get("holEndTme")))  // 공휴일 종료 시간
                        .aedFirSun(parsePossibleValue(record.get("sunFrtYon")))  // 일요일 첫 번째 가용 여부
                        .aedSecSun(parsePossibleValue(record.get("sunScdYon")))  // 일요일 두 번째 가용 여부
                        .aedThiSun(parsePossibleValue(record.get("sunThiYon")))  // 일요일 세 번째 가용 여부
                        .aedFouSun(parsePossibleValue(record.get("sunFurYon")))  // 일요일 네 번째 가용 여부
                        .aedEntity(aedEntity)
                        .build();

                // AedAvailableEntity를 데이터베이스에 저장
                aedAvailableRepository.save(aedAvailableEntity);
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    private LocalTime safeParseTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.isEmpty()) {
                return null;
            }

            // 2400은 00:00으로 변환
            if (timeStr.equals("2400")) {
                timeStr = "23:59";
            } else {
                // 앞 두 자리 시간, 뒤 두 자리 분으로 처리
                int hour = Integer.parseInt(timeStr.substring(0, 2));
                int minute = Integer.parseInt(timeStr.substring(2, 4));

                // 시간이 24 이상일 경우 24를 빼서 처리
                if (hour >= 24) {
                    hour = hour - 24;
                }

                // 형식에 맞게 HH:mm으로 변환
                timeStr = String.format("%02d:%02d", hour, minute);
            }

            return LocalTime.parse(timeStr); // HH:mm 형식으로 변환
        } catch (Exception e) {
            System.err.println("시간 파싱 오류: " + timeStr);
            return null; // 파싱 오류 시 null 반환
        }
    }


    // 문자열을 안전하게 Double로 변환하는 유틸리티 메서드
    private Double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Double 변환 오류: " + value);
            return 0.0;
        }
    }

    private AedAvailableEntity.Possible parsePossibleValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return AedAvailableEntity.Possible.NULL;  // null이거나 빈 문자열일 때 Possible.NULL 반환
        }
        return AedAvailableEntity.Possible.valueOf(value);  // 그 외의 경우는 enum 값 반환
    }

}
