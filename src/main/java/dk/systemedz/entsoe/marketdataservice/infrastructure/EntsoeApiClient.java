package dk.systemedz.entsoe.marketdataservice.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

@Service
public class EntsoeApiClient {

    @Value("${entsoe.baseurl}")
    private String BASE_URL;

    private final RestTemplate restTemplate;

    public EntsoeApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getByPeriodDefinition(Map<String,String> params) {
        return getResultByInterval(params);
    }

    public String getLastWeek(Map<String,String> params) {
        var periodEnd = LocalDateTime.now(ZoneId.of("Europe/Copenhagen")).withHour(23).withMinute(0).withSecond(0).withNano(0);
        var periodStart = periodEnd.minusWeeks(1);
        addPeriodFromEventDateToParams(params, periodStart, periodEnd);

        return getResultByInterval(params);
    }

    public String getLastMonth(Map<String,String> params) {
        var periodEnd = LocalDateTime.now(ZoneId.of("Europe/Copenhagen"));
        var periodStart = periodEnd.minusMonths(1);
        addPeriodFromEventDateToParams(params, periodStart, periodEnd);

        return getResultByInterval(params);
    }

    public String getLastYear(Map<String,String> params) {
        var periodEnd = LocalDateTime.now(ZoneId.of("Europe/Copenhagen"));
        var periodStart = periodEnd.minusYears(1);
        addPeriodFromEventDateToParams(params, periodStart, periodEnd);

        return getResultByInterval(params);
    }

    private void addPeriodFromEventDateToParams(Map<String,String> params, LocalDateTime periodStart, LocalDateTime periodEnd) {
        params.put("periodStart", formatEntsoeDateToString(periodStart));
        params.put("periodEnd", formatEntsoeDateToString(periodEnd));
        params.remove("eventDate");
    }

    private String formatEntsoeDateToString(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dateTime.format(formatter)+"2300";
    }

    private String getResultByInterval(Map<String,String> urlParams) {
        var apiUrl = addUrlParams(urlParams);
        return restTemplate.getForObject(apiUrl, String.class);
    }

    private String addUrlParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder(BASE_URL).append("?");
        Iterator<String> keySet = params.keySet().iterator();
        while (keySet.hasNext()) {
            String key = keySet.next();

            result.append(key).append("=").append(params.get(key));
            if (keySet.hasNext()) {
                result.append("&");
            }
        }
        return result.toString();
    }
}
