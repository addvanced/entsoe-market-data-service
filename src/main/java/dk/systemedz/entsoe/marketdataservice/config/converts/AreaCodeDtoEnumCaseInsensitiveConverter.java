package dk.systemedz.entsoe.marketdataservice.config.converts;

import dk.systemedz.entsoe.marketdataservice.api.dto.AreaCodeDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;


@Component
public class AreaCodeDtoEnumCaseInsensitiveConverter implements Converter<String, AreaCodeDto> {

    @Override
    public AreaCodeDto convert(String areaCode) {
        if (areaCode.isBlank())
            return null;

        return AreaCodeDto.valueOf(areaCode.trim().toUpperCase(Locale.ROOT));
    }
}
