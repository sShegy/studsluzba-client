package org.raflab.studsluzba.client.dto.ispit.response;

import lombok.Data;

@Data
public class PrijavljeniStudentResponseDTO {
    private String ime;
    private String prezime;
    private String brojIndeksa;
    private String studijskiProgram;
}