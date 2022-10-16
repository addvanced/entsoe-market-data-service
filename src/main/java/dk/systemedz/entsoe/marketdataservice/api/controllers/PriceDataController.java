package dk.systemedz.entsoe.marketdataservice.api.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.systemedz.entsoe.xsd.iec62325.PublicationMarketDocument;
import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceDataControllerApiDelegate;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.service.EntsoeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Component
@Slf4j
public class PriceDataController implements PriceDataControllerApiDelegate {

    private final EntsoeService service;

    public PriceDataController(EntsoeService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<PricesResponseDto> getMarketDocumentFromInterval(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) throws Exception {
        validateInput(areaCode, entsoeSecurityToken, from, to, year, month, week, intervalType, interval);
        Map<String, String> params = new HashMap<>();
        params.put("securityToken", entsoeSecurityToken.trim());
        params.put("documentType", "A44");
        params.put("in_Domain", AreaCode.FR.getAreaCode());
        params.put("out_Domain", AreaCode.FR.getAreaCode());
        params.put("eventDate", "lastWeek");

        String apiResponse = service.getEntsoeData(params);

        try {
            ObjectMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            xmlMapper.registerModule(new JaxbAnnotationModule());

            PublicationMarketDocument value = xmlMapper.readValue(apiResponse, PublicationMarketDocument.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String apiResponseJson = objectMapper.writeValueAsString(value);
            System.out.println(apiResponseJson);
            return ok(PricesResponseDto.builder().build());
        } catch (Exception e) {
            throw new RestCallException("Something went wrong", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateInput(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) {


    }
}
