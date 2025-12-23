package org.raflab.studsluzba.client.dto.sifarnik.request;

import lombok.Data;

@Data
public class CreateSrednjaSkolaRequestDTO {
    private String naziv;
    private String mesto;
    private String vrsta;
}