package dk.systemedz.entsoe.marketdataservice.domain.models;

import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MarketDocument {
    private LocalDateTime createdDateTime;
    private TimeInterval timeInterval;
    private AreaCode area;
    private List<IntervalDay> intervalDays;
}
