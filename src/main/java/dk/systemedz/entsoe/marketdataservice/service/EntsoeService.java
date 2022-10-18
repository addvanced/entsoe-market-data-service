package dk.systemedz.entsoe.marketdataservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import dk.systemedz.entsoe.marketdataservice.infrastructure.EntsoeApiClient;
import dk.systemedz.entsoe.marketdataservice.infrastructure.entity.PublicationMarketDocument;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EntsoeService {
    private final EntsoeApiClient entsoeApiClient;

    public EntsoeService(EntsoeApiClient entsoeApiClient) {
        this.entsoeApiClient = entsoeApiClient;
    }

    public PublicationMarketDocument getEntsoeData(Map<String,String> params) {
        if(!queryDataIsValid(params))
            throw new RuntimeException("URL Parameters are not valid.");

        try {


        String response = hasEventDateDefinition(params) ? getDataByEventDate(params) : getDataByPeriod(params);

        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper xmlMapper = new XmlMapper(module);
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.registerModule(new JaxbAnnotationModule());

        PublicationMarketDocument document = xmlMapper.readValue(response, PublicationMarketDocument.class);
        prepareDocument(document, params.get("in_Domain"));
        return document;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareDocument(PublicationMarketDocument document, String areaCode) {
        document.setArea(areaCode);
        document.getTimeSeries()
                .forEach(s -> s.getPeriod()
                        .setPricePointHours());
    }

    private String getDataByEventDate(Map<String, String> params) {
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


    private String getDataByPeriod(Map<String, String> params) {
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
