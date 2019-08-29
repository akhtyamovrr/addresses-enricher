package org.test.enricher.model.dto;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.Address;

@Data
@Accessors(fluent = true, chain = true)
public class AddressDto {
    private Address address;
    private int houseNumber;

    public AddressDto(Address address) {
        this.address = address;
        houseNumber = parseHouseNumber(address.houseNumber());
    }

    private int parseHouseNumber(String houseNumber) {
        Preconditions.checkArgument(StringUtils.isNoneEmpty(houseNumber), "building number should not be empty");
        if (StringUtils.isNumeric(houseNumber)) {
            return Integer.parseInt(houseNumber);
        } else {
            final String[] split = StringUtils.split(houseNumber, "-;# ", 2);
            var number = split[0].trim();
            if (!StringUtils.isNumeric(number)) {
                number = number.replaceAll("[^\\d]", "");
            }
            return Integer.parseInt(number);
        }
    }
}
