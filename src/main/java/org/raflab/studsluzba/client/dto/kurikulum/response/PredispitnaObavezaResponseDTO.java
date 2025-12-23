package org.raflab.studsluzba.client.dto.kurikulum.response;

import lombok.Data;

@Data
public class PredispitnaObavezaResponseDTO {
    private Long id;
    private String vrsta;
    private Double maxPoena;
    private String skolskaGodina;
    private Double osvojeniPoeni;

}