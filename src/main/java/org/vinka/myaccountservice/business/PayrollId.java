package org.vinka.myaccountservice.business;

import lombok.*;
import org.vinka.myaccountservice.YearMonthToDateConverter;

import javax.persistence.Convert;
import java.io.Serializable;
import java.time.YearMonth;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class PayrollId implements Serializable {
    private String employee;
    @Convert(converter = YearMonthToDateConverter.class)
    private YearMonth period;
}
