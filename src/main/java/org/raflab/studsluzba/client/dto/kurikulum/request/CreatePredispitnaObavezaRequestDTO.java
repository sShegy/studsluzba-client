package org.raflab.studsluzba.client.dto.kurikulum.request;

import lombok.Data;

@Data
public class CreatePredispitnaObavezaRequestDTO {
    private Long predmetId;
    private Long skolskaGodinaId;
    private String vrsta; // npr. "kolokvijum", "test", "domaći"
    private Double maxPoena;
}