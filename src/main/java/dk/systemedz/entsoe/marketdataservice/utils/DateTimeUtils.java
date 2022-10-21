package dk.systemedz.entsoe.marketdataservice.utils;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DateTimeUtils {

    private static final LocalTime TIME_2300 = LocalTime.of(23,0,0,0);
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd = 20221231
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter ENTSOE_DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    public static LocalDateTime createLocalDateTimeNow() {
        return createLocalDateTimeFromString(null);
    }

    public static LocalDateTime createLocalDateTimeFromString(String localDateTime) {
        if(isBlank(localDateTime))
            return LocalDateTime.of(LocalDate.now(), TIME_2300);

        localDateTime = localDateTime.trim();
        if(localDateTime.length() > 8)
            localDateTime = localDateTime.substring(0,8);

        return LocalDateTime.of(LocalDate.parse(localDateTime, DATE_PATTERN), TIME_2300);
    }

    /**
     * ENTSO-E DateTime timestamp parser to LocalDateTime
     * @param entsoeDateTime DateTime of format yyyy-MM-dd'T'HH:mm'Z' - e.g. 2022-06-24T22:00Z
     * @return
     */
    public static LocalDateTime parseEntsoeTimestamp(@NotNull String entsoeDateTime) {
        LocalDateTime localDateTime = LocalDateTime
                .parse(entsoeDateTime, ENTSOE_DATE_TIME_PATTERN)
                .atZone(ZoneOffset.UTC.normalized())
                .toLocalDateTime();

        return localDateTime;
    }
}
