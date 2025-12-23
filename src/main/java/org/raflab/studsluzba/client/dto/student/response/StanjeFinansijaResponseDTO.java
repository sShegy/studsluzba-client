package org.raflab.studsluzba.client.dto.student.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StanjeFinansijaResponseDTO {
    private BigDecimal ukupnoZaduzenje;
    private BigDecimal ukupnoUplaceno;
    private BigDecimal preostaloZaUplatu;
}