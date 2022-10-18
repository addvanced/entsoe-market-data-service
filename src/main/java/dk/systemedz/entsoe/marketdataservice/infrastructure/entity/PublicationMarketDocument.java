package dk.systemedz.entsoe.marketdataservice.infrastructure.entity;

import dk.systemedz.entsoe.marketdataservice.domain.models.enums.AreaCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@XmlRootElement(name = "Publication_MarketDocument")
@Getter @Setter
@XmlType(propOrder = { "createdDateTime", "area", "areaCode", "timeInterval", "timeSeries"})
public class PublicationMarketDocument {

    @XmlElement(name = "createdDateTime")
    private String createdDateTime;
    @XmlElement(name = "area")
    private String area;

    @Setter(AccessLevel.NONE)
    @XmlElement(name = "areaCode")
    private String areaCode;

    @XmlElement(name = "period.timeInterval")
    private TimeInterval timeInterval;

    @XmlElement(name = "TimeSeries")
    private List<TimeSeries> timeSeries;

    public void setArea(String area) {
        AreaCode.getAreaCodeByValue(area)
                .ifPresent(areaCode -> {
                    this.area = "%s - %s".formatted(areaCode.name(), areaCode.getAreaName());
                    this.areaCode = areaCode.getAreaCode();
                });
    }

    public String getCreatedDateTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
