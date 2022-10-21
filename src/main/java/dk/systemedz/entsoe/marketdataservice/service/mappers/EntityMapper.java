package dk.systemedz.entsoe.marketdataservice.service.mappers;

import dk.systemedz.entsoe.marketdataservice.domain.models.IntervalDay;
import dk.systemedz.entsoe.marketdataservice.domain.models.MarketDocument;
import dk.systemedz.entsoe.marketdataservice.domain.models.PricePoint;
import dk.systemedz.entsoe.marketdataservice.domain.models.TimeInterval;
import dk.systemedz.entsoe.marketdataservice.infrastructure.entity.PublicationMarketDocument;
import dk.systemedz.entsoe.marketdataservice.infrastructure.entity.TimeSeries;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface EntityMapper {

    @Mappings({
            @Mapping(source = "createdDateTime", target = "createdDateTime"),
            @Mapping(source = "areaCode", target = "area"),
            @Mapping(source = "timeSeries", target = "intervalDays"),
    })
    MarketDocument mapMarketDocument(PublicationMarketDocument publicationMarketDocument);

    @Mappings({
            @Mapping(source = "period.timeInterval", target = "currentDateInterval"),
            @Mapping(source = "period.pricePoints", target = "hourPrices")
    })
    IntervalDay mapTimeSeriesToIntervalDay(TimeSeries timeSeries);

    @Mappings({
            @Mapping(source = "start", target="intervalStart"),
            @Mapping(source = "end", target="intervalEnd")
    })
    TimeInterval mapTimeInterval(dk.systemedz.entsoe.marketdataservice.infrastructure.entity.TimeInterval timeIntervalEntity);

    @Mappings({
            @Mapping(source = "hour", target = "hour"),
            @Mapping(source = "amount", target = "price")
    })
    PricePoint mapPricePoint(dk.systemedz.entsoe.marketdataservice.infrastructure.entity.PricePoint pricePointEntity);

    default LocalDateTime mapCreatedDateTimeString(String localDateTimeString) {
        try {
            return LocalDateTime.parse(localDateTimeString.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch(DateTimeParseException e) {
            return LocalDateTime.parse(localDateTimeString.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
        }
    }
}
