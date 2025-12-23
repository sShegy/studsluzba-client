package org.raflab.studsluzba.client.dto.student.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateStudentRequestDTO {
    private String ime;
    private String prezime;
    private String srednjeIme;
    private String jmbg;
    private LocalDate datumRodjenja;
    private String mestoRodjenja;
    private String adresaStanovanja;
    private String email;
    private String emailPrivatni;
    private Double uspehSrednjaSkola;
    private Double uspehPrijemni;
    private Long idSrednjeSkole;
    // ... dodati ostala polja iz StudentPodaci po potrebi
}