package dk.systemedz.entsoe.marketdataservice.api.mappers;

import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalDayDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricePointDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.PricesResponseDto;
import dk.systemedz.entsoe.marketdataservice.api.dto.TimeIntervalDto;
import dk.systemedz.entsoe.marketdataservice.domain.models.IntervalDay;
import dk.systemedz.entsoe.marketdataservice.domain.models.MarketDocument;
import dk.systemedz.entsoe.marketdataservice.domain.models.PricePoint;
import dk.systemedz.entsoe.marketdataservice.domain.models.TimeInterval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DtoMapper {

    @Mappings({
            @Mapping(source = "area.areaName", target = "area"),
            @Mapping(source = "area.areaCode", target = "areaCode")
    })
    PricesResponseDto mapPricesResponse(MarketDocument marketDocument);

    @Mappings({
            @Mapping(source = "intervalStart", target = "start"),
            @Mapping(source = "intervalEnd", target = "end")
    })
    TimeIntervalDto mapTimeInterval(TimeInterval timeInterval);
    default String mapLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    IntervalDayDto mapIntervalDay(IntervalDay intervalDay);

    PricePointDto mapPricePoint(PricePoint pricePoint);
}
