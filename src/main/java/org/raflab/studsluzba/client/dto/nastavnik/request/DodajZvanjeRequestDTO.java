package org.raflab.studsluzba.client.dto.nastavnik.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DodajZvanjeRequestDTO {
    private Long nastavnikId;
    private String zvanje; // npr. "Docent", "Vanredni profesor"
    private LocalDate datumIzbora;
    private String naucnaOblast;
}