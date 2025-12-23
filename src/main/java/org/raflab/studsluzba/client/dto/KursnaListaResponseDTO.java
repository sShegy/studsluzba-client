package org.raflab.studsluzba.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KursnaListaResponseDTO {

    @JsonProperty("srednji_kurs")
    private BigDecimal srednjiKurs;

}