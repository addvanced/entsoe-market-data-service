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
    private static final DateTimeFormatter ENTSOE_QUERY_DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm"); // yyyyMMddHHMM = 202212312300

    private static final DateTimeFormatter ENTSOE_DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    public static LocalDateTime createLocalDateTimeNow() {
        return createLocalDateTimeFromString(null);
    }

    public static LocalDateTime createLocalDateTimeFromString(String localDateTime) {
        if(isBlank(localDateTime) || localDateTime.trim().length() < 8)
            return LocalDate.now().atTime(TIME_2300);

        return LocalDate.parse(localDateTime.trim().substring(0,8), DATE_PATTERN).atTime(TIME_2300);
    }

    public static LocalDateTime setHours(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().atTime(TIME_2300);
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

        return setHours(localDateTime);
    }

    public static String createEntsoeQueryDateTime(LocalDateTime localDateTime) {
        return setHours(localDateTime).format(ENTSOE_QUERY_DATE_TIME_PATTERN);
    }
}
