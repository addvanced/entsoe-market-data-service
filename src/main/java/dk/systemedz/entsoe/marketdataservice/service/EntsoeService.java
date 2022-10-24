package dk.systemedz.entsoe.marketdataservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import dk.systemedz.entsoe.marketdataservice.domain.models.MarketDocument;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.IntervalType;
import dk.systemedz.entsoe.marketdataservice.domain.models.enums.QueryType;
import dk.systemedz.entsoe.marketdataservice.exceptions.rest.RestCallException;
import dk.systemedz.entsoe.marketdataservice.infrastructure.EntsoeApiClient;
import dk.systemedz.entsoe.marketdataservice.infrastructure.entity.PublicationMarketDocument;
import dk.systemedz.entsoe.marketdataservice.service.mappers.EntityMapper;
import dk.systemedz.entsoe.marketdataservice.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@AllArgsConstructor
public class EntsoeService {

    private final EntsoeApiClient entsoeApiClient;
    private final EntityMapper mapper;

    public MarketDocument getPricesFromEntsoeApi(String securityToken, String areaCode, Map<String,String> params, QueryType queryType) throws RestCallException {

        try {
            Pair<String, String> dateInterval = switch (queryType) {
                case DATETIME -> getDateIntervalByDateRange(params.get("from"), params.get("to"));
                case INTERVAL -> getDateIntervalByInterval(params.get("intervalType"), params.get("interval"));
                case FIXED -> getDateIntervalByFixedInterval(params.get("year"), params.get("month"), params.get("week"));
            };

            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);

            XmlMapper xmlMapper = new XmlMapper(module);
            xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            xmlMapper.registerModule(new JaxbAnnotationModule());

            String apiResponse = getDataByPeriod(securityToken,areaCode,dateInterval.getLeft(),dateInterval.getRight());
            PublicationMarketDocument document = xmlMapper
                    .readValue(apiResponse, PublicationMarketDocument.class);

            prepareDocument(document, areaCode);

            MarketDocument marketDocument = mapper.mapMarketDocument(document);

            if(isNull(marketDocument.getIntervalDays()) || marketDocument.getIntervalDays().isEmpty())
                throw new RestCallException("No data provided by ENTSO-E. Try another interval.", HttpStatus.NO_CONTENT);

            return marketDocument;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, String> getDateIntervalByFixedInterval(String year, String month, String week) {
        Integer y = isNotBlank(year) ? Integer.parseInt(year.trim()) : Calendar.getInstance().get(Calendar.YEAR);
        Integer m = isNotBlank(month) ? Integer.parseInt(month.trim()) : null;
        Integer w = isNotBlank(week) ? Integer.parseInt(week.trim()) : null;
        return getDateIntervalByFixedInterval(y,m,w);
    }
    private Pair<String, String> getDateIntervalByFixedInterval(Integer year, Integer month, Integer week) {
        year = nonNull(year) ? year : Calendar.getInstance().get(Calendar.YEAR);
        month = nonNull(month) ? month : 0;
        week = nonNull(week) ? week : 0;

        LocalDateTime fromDate = LocalDateTime.now().withYear(year);
        LocalDateTime toDate;

        if(month > 0 || week > 0) {
            if(month > 0) {
                fromDate = fromDate.withMonth(Math.min(month, 12)).withDayOfMonth(1).minusDays(1);
                toDate = fromDate.plusMonths(1);
            } else {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                fromDate = LocalDateTime.now()
                        .withYear(year)
                        .with(weekFields.weekOfYear(), week-1)
                        .with(weekFields.dayOfWeek(), 7);
                toDate = fromDate.plusWeeks(1);
            }
        } else {
            fromDate = fromDate.withMonth(1).withDayOfMonth(1).minusDays(1);
            toDate = fromDate.plusYears(1);
        }

        return Pair.of(DateTimeUtils.createEntsoeQueryDateTime(fromDate), DateTimeUtils.createEntsoeQueryDateTime(toDate));
    }
    private Pair<String, String> getDateIntervalByDateRange(String from, String to) {
        to = isNotBlank(to) ? to.trim().substring(0,8) : DateTimeUtils.createLocalDateTimeNow()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        LocalDateTime fromDateTime =
                DateTimeUtils.createLocalDateTimeFromString(from.trim().substring(0,8));

        LocalDateTime toDateTime =
                DateTimeUtils.createLocalDateTimeFromString(to);


        return (fromDateTime.toLocalDate().isBefore(toDateTime.toLocalDate()) || fromDateTime.toLocalDate().isEqual(toDateTime.toLocalDate())) ?
                Pair.of(DateTimeUtils.createEntsoeQueryDateTime(fromDateTime), DateTimeUtils.createEntsoeQueryDateTime(toDateTime)) :
                Pair.of(DateTimeUtils.createEntsoeQueryDateTime(toDateTime), DateTimeUtils.createEntsoeQueryDateTime(fromDateTime));
    }
    private Pair<String, String> getDateIntervalByInterval(String intervalType, String intervalStr) {
        IntervalType type = IntervalType.valueOf(intervalType);
        int interval = Integer.parseInt(intervalStr);

        LocalDateTime today = DateTimeUtils.createLocalDateTimeNow();

        LocalDateTime intervalDate = switch (type) {
            case YEAR -> today.minusYears(1);
            case MONTH -> today.minusMonths(Math.min(interval, 12));
            case WEEK -> today.minusWeeks(Math.min(interval, 53));
            case DAY -> interval == -1 ? today.plusDays(1) : today.minusDays(Math.min(interval, 365));
        };

        return interval != -1 ?
                Pair.of(DateTimeUtils.createEntsoeQueryDateTime(intervalDate), DateTimeUtils.createEntsoeQueryDateTime(today)) :
                Pair.of(DateTimeUtils.createEntsoeQueryDateTime(today), DateTimeUtils.createEntsoeQueryDateTime(intervalDate));
    }

    private void prepareDocument(PublicationMarketDocument document, String areaCode) {
        document.setArea(areaCode);
        if(nonNull(document.getTimeSeries()) && !document.getTimeSeries().isEmpty())
            document.getTimeSeries().forEach(s -> s.getPeriod().setPricePointHours());
    }


    @Cacheable(value = "pastyear", key = "{#areaCode,#start,#end}")
    public String getDataByPeriod(String securityToken, String areaCode, String start, String end) {
        return entsoeApiClient.getByPeriodDefinition(securityToken,areaCode,start,end);
    }
}
