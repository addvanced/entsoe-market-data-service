package dk.systemedz.entsoe.marketdataservice.service;

import dk.systemedz.entsoe.marketdataservice.infrastructure.EntsoeApiClient;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EntsoeService {
    private final EntsoeApiClient entsoeApiClient;

    public EntsoeService(EntsoeApiClient entsoeApiClient) {
        this.entsoeApiClient = entsoeApiClient;
    }

    public String getEntsoeData(Map<String,String> params) {
        if(!queryDataIsValid(params))
            throw new RuntimeException("URL Parameters are not valid.");

        return hasEventDateDefinition(params) ? getDataByEventDate(params) : getDataByPeriod(params);
    }

    public String getDataByEventDate(Map<String, String> params) {
        String eventDate = params.get("eventDate").trim().toLowerCase();

        if(Strings.isBlank(eventDate))
            throw new RuntimeException("No eventDate Data");

        return switch (eventDate) {
            case "lastweek" -> entsoeApiClient.getLastWeek(params);
            case "lastmonth" -> entsoeApiClient.getLastMonth(params);
            case "lastyear" -> entsoeApiClient.getLastYear(params);
            default -> throw new RuntimeException("Invalid eventDate. Options: lastWeek, lastMonth or lastYear");
        };
    }


    public String getDataByPeriod(Map<String, String> params) {
        return entsoeApiClient.getByPeriodDefinition(params);
    }

    private boolean queryDataIsValid(Map<String, String> params) {
        return params.containsKey("securityToken") && (hasEventDateDefinition(params) || hasPeriodDefinition(params));
    }

    private boolean hasEventDateDefinition(Map<String,String> queryData) {
        return queryData.containsKey("eventDate") && Strings.isNotBlank(queryData.get("eventDate"));
    }

    private boolean hasPeriodDefinition(Map<String,String> queryData) {
        var hasPeriodStart = queryData.containsKey("periodStart") && Strings.isNotBlank(queryData.get("periodStart"));
        var hasPeriodEnd = queryData.containsKey("periodEnd") && Strings.isNotBlank(queryData.get("periodEnd"));

        return hasPeriodStart && hasPeriodEnd;
    }
}
