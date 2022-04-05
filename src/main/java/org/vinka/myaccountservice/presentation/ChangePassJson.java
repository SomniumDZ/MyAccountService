package org.vinka.myaccountservice.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChangePassJson {
    @JsonProperty(value = "new_password", access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    private String newPassword;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String email;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String status;
}
