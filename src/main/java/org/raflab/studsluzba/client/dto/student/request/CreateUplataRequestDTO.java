package org.raflab.studsluzba.client.dto.student.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateUplataRequestDTO {
    private Long studentIndeksId;
    private BigDecimal iznos;
    private LocalDate datumUplate;
    private String svrha;
}