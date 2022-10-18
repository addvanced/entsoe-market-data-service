package dk.systemedz.entsoe.marketdataservice.infrastructure.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@XmlRootElement(name = "Point")
@XmlType(propOrder = { "hour", "position", "amount" })
@Getter @Setter
public class PricePoint {

   @XmlElement
   private String hour;
   @XmlElement
   @Getter(AccessLevel.NONE)
   private int position;
   @XmlElement(name = "price.amount")
   private BigDecimal amount;

   public void setHour(LocalDateTime startTime, String pattern) {
      this.hour = startTime
              .plusHours(this.position-1)
              .format(DateTimeFormatter.ofPattern(pattern));
   }
}
