package org.raflab.studsluzba.client.dto.nastavnik.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateNastavnikRequestDTO {
    private String ime;
    private String prezime;
    private String srednjeIme;
    private String email;
    private LocalDate datumRodjenja;
}