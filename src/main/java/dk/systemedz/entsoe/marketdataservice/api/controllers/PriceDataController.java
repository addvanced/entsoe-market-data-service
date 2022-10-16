package dk.systemedz.entsoe.marketdataservice.api.controllers;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.rest.PriceDataControllerApiDelegate;
import org.springframework.http.ResponseEntity;

public class PriceDataController implements PriceDataControllerApiDelegate {
    @Override
    public ResponseEntity<PricesResponseDto> getMarketDocumentFromInterval(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) throws Exception {
        validateInput(areaCode, entsoeSecurityToken, from, to, year, month, week, intervalType, interval);

        return PriceDataControllerApiDelegate.super.getMarketDocumentFromInterval(areaCode, entsoeSecurityToken, from, to, year, month, week, intervalType, interval);
    }

    private void validateInput(AreaCodeDto areaCode, String entsoeSecurityToken, String from, String to, Integer year, Integer month, Integer week, IntervalTypeDto intervalType, Integer interval) {


    }
}
