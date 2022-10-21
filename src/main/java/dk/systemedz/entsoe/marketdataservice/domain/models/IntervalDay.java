package dk.systemedz.entsoe.marketdataservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IntervalDay {
    private String currency;
    private String measureUnit;
    private TimeInterval currentDateInterval;
    private List<PricePoint> hourPrices;
}
