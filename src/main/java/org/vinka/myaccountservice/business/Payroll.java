package org.vinka.myaccountservice.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vinka.myaccountservice.YearMonthToDateConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import java.time.YearMonth;

@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(PayrollId.class)
public class Payroll {
    @Id
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String employee;

    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-yyyy")
    @Convert(converter = YearMonthToDateConverter.class)
    private YearMonth period;

    @PositiveOrZero
    private Long salary;
}
