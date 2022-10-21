package dk.systemedz.entsoe.marketdataservice.config.converts;

import dk.systemedz.entsoe.marketdataservice.api.dto.IntervalTypeDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;


@Component
public class IntervalDtoEnumCaseInsensitiveConverter implements Converter<String, IntervalTypeDto> {

    @Override
    public IntervalTypeDto convert(String intervalType) {
        if (intervalType.isBlank())
            return null;

        return IntervalTypeDto.valueOf(intervalType.trim().toUpperCase(Locale.ROOT));
    }
}
