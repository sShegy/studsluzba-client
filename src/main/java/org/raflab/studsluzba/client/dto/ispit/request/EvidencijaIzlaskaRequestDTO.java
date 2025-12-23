package org.raflab.studsluzba.client.dto.ispit.request;

import lombok.Data;

@Data
public class EvidencijaIzlaskaRequestDTO {
    private Long studentIndeksId;
    private Long ispitId;
    private Double poeniSaIspita;
    private String napomena;
}