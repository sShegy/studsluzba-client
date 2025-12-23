package org.raflab.studsluzba.client.dto.student.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentProfileResponseDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String jmbg;
    private LocalDate datumRodjenja;
    private String email;
    private String aktivniIndeks; // npr. "123/2025"
    private String studijskiProgram;
    private String mestoRodjenja;
    private String adresaStanovanja;

    private Long indeksId;
}
