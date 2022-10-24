package dk.systemedz.entsoe.marketdataservice.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@Service
public class EntsoeApiClient {

    @Value("${entsoe.api-url}")
    private String BASE_URL;
    @Value("${entsoe.document-types.prices}")
    private String PRICES_TYPE;

    private final RestTemplate restTemplate;

    public EntsoeApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "pricedata", key = "{#areaCode,#start,#end}")
    public String getByPeriodDefinition(String securityToken, String areaCode, String start, String end) {
        if(isAnyBlank(securityToken,areaCode,start,end))
            throw new RuntimeException("Something went wrong!");

        String apiUrl = "%s?documentType=%s&securityToken=%s&in_Domain=%s&out_Domain=%s&periodStart=%s&periodEnd=%s"
                .formatted(BASE_URL, PRICES_TYPE, securityToken, areaCode, areaCode, start, end);

        return restTemplate.getForObject(apiUrl, String.class);
    }
}
