package dk.systemedz.entsoe.marketdataservice.domain.models.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum AreaCode {
    FR("France", "10YFR-RTE------C"),
    DK1("Denmark - West", "10YDK-1--------W"),
    DK2("Denmark - East", "10YDK-2--------M"),
    DE("Germany", "10Y1001A1001A82H"),
    SE1("Sweden - 1", "10Y1001A1001A44P"),
    SE2("Sweden - 2", "10Y1001A1001A45N"),
    SE3("Sweden - 3", "10Y1001A1001A46L"),
    SE4("Sweden - 4", "10Y1001A1001A47J"),
    NO1("Norway - 1", "10YNO-1--------2"),
    NO2("Norway - 2", "10YNO-2--------T"),
    NO3("Norway - 3", "10YNO-3--------J"),
    NO4("Norway - 4", "10YNO-4--------9"),
    NO5("Norway - 5", "10Y1001A1001A48H"),
    FI("Finland", "10YFI-1--------U");

    private final String areaName;
    private final String areaCode;

    public static Optional<AreaCode> getAreaCodeByValue(String value) {
        return Arrays.stream(AreaCode.values())
                .filter(aCode -> aCode.areaName.equalsIgnoreCase(value) || aCode.areaCode.equalsIgnoreCase(value))
                .findFirst();
    }

    public static boolean existsByName(String name) {
        return Arrays.stream(AreaCode.values()).map(Enum::name)
                .anyMatch(ac -> ac.equals(name.trim().toUpperCase()));
    }
}
