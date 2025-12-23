package org.raflab.studsluzba.client.dto.skolskagodina.response;

import lombok.Data;

@Data
public class SkolskaGodinaResponseDTO {
    private Long id;
    private String naziv;
    private boolean aktivna;
}