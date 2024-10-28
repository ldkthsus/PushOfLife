package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.location.LocationDTO;
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
public class LocationService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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


        List<String> list = new ArrayList<>();

        if (results == null){
            return list;
        }

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : results){
            String token = geoResult.getContent().getName();
            list.add(token);
        }
        return list;

    }


}
