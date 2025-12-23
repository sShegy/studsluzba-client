package org.raflab.studsluzba.client.dto.nastavnik.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ZvanjeResponseDTO {
    private String zvanje;
    private LocalDate datumIzbora;
    private String naucnaOblast;
}