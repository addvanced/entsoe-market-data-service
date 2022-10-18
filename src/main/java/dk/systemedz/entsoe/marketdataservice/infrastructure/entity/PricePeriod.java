package dk.systemedz.entsoe.marketdataservice.infrastructure.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@XmlRootElement(name = "Period")
@Getter @Setter
public class PricePeriod {

   @XmlElement(name = "timeInterval")
   private TimeInterval timeInterval;

   @XmlElement(name = "Point")
   private List<PricePoint> pricePoints;

   public void setPricePointHours() {
      String timestampPattern = "yyyy-MM-dd'T'HH:mm'Z'";

      LocalDateTime startTime = LocalDateTime
              .parse(timeInterval.getStart(),
                      DateTimeFormatter.ofPattern(timestampPattern));

      pricePoints.forEach(point -> point.setHour(startTime, timestampPattern));
   }
}
