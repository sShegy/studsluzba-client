package org.raflab.studsluzba.client.dto.ispit.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class IspitniRokResponseDTO {
    private Long id;
    private String naziv;
    private LocalDate datumPocetka;
    private LocalDate datumZavrsetka;
    private List<IspitResponseDTO> ispiti;
}