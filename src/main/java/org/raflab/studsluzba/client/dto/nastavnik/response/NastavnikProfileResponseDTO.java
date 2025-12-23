package org.raflab.studsluzba.client.dto.nastavnik.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class NastavnikProfileResponseDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String email;
    private LocalDate datumRodjenja;
    private ZvanjeResponseDTO aktivnoZvanje;
    private List<ZvanjeResponseDTO> istorijaZvanja;
    private List<ObrazovanjeResponseDTO> obrazovanja;
}