package dk.systemedz.entsoe.marketdataservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class CachingService {

    @Scheduled(cron = "10 13,23 * * * ?", zone = "Europe/Copenhagen")
    @CacheEvict(value="pricedata", allEntries=true)
    public void evictDayAhead() {}
}
