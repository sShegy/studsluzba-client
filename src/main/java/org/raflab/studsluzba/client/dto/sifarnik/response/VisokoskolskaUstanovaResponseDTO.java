package org.raflab.studsluzba.client.dto.sifarnik.response;

import lombok.Data;

@Data
public class VisokoskolskaUstanovaResponseDTO {
    private Long id;
    private String naziv;
    private String mesto;
    private String skracenaOznaka;
}