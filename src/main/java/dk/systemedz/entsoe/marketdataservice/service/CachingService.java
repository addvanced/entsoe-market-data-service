package dk.systemedz.entsoe.marketdataservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class CachingService {

    private CacheManager cacheManager;

    public void evictAllValuesFromCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName.trim())).clear();
    }

    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .parallelStream()
                .forEach(cache -> Objects.requireNonNull(cacheManager.getCache(cache)).clear());
    }

    @Scheduled(cron = "0 23 * * * ?", zone = "Europe/Copenhagen")
    private void evictPastYear() {
        evictAllValuesFromCache("pastyear");
    }

    @Scheduled(cron = "0 13 5 * * ?", zone = "Europe/Copenhagen")
    private void evictDayAhead() {
        evictAllValuesFromCache("dayahead");
    }

}
