package dk.systemedz.entsoe.marketdataservice.infrastructure.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "TimeSeries")
@Getter @Setter
@XmlType(propOrder = {"currency","measureUnit","period"})
public class TimeSeries {

   @XmlElement(name = "currency_Unit.name")
   private String currency;
   @XmlElement(name = "price_Measure_Unit.name")
   private String measureUnit;

   @XmlElement(name = "Period")
   private PricePeriod period;
}
