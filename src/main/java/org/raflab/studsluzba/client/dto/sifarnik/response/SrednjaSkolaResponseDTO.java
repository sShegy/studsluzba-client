package org.raflab.studsluzba.client.dto.sifarnik.response;

import lombok.Data;

@Data
public class SrednjaSkolaResponseDTO {
    private Long id;
    private String naziv;
    private String mesto;
    private String vrsta;
}