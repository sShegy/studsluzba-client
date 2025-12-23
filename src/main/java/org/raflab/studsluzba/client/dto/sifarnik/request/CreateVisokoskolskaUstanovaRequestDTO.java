package org.raflab.studsluzba.client.dto.sifarnik.request;

import lombok.Data;

@Data
public class CreateVisokoskolskaUstanovaRequestDTO {
    private String naziv;
    private String mesto;
    private String skracenaOznaka;
}