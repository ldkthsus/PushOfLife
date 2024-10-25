package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.location.LocationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final KafkaProducerService kafkaProducerService;

    public void saveLocation(LocationDTO locationDTO) {
        redisTemplate.opsForGeo().add("user_location", new Point(locationDTO.getLatitude(), locationDTO.getLongitude()), locationDTO.getFcmToken());

    }

    public List<String> findUsersNearby(String fcmToken, int range) {

        GeoReference reference = GeoReference.fromMember(fcmToken);

        Distance radius = new Distance(range, RedisGeoCommands.DistanceUnit.METERS);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(13);

        GeoOperations<String, String> geoOperations = redisTemplate.opsForGeo();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOperations
                .search("user_location", reference, radius, args);

        List<Point> position = geoOperations.position("user_location", fcmToken);
        Point point = position.get(0);

        // fcm Token 리스트
        List<String> list = new ArrayList<>();

        if (results == null){
            return list;
        }

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : results){
            String token = geoResult.getContent().getName();
            if (!token.equals(fcmToken)){
                list.add(token);
            }
        }

        for (String tokens : list){
            kafkaProducerService.sendTopic(tokens, point.getX(),point.getY());
        }

        return list;

    }


}
