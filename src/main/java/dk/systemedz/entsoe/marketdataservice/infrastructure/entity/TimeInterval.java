package dk.systemedz.entsoe.marketdataservice.infrastructure.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlType;

@Getter @Setter
@XmlType(propOrder = {"start","end"})
public class TimeInterval {
   private String start;
   private String end;
}
