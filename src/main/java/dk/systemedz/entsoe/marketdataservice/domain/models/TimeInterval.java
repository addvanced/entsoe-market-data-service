package dk.systemedz.entsoe.marketdataservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class TimeInterval {
    private LocalDateTime intervalStart;
    private LocalDateTime intervalEnd;
}
