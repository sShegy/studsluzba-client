package org.raflab.studsluzba.client.dto.ispit.response;

import lombok.Data;

@Data
public class IspitRezultatResponseDTO {
    private String studentImePrezime;
    private String brojIndeksa;
    private Double poeniPredispitne;
    private Double poeniIspit;
    private Double ukupnoPoena;
    private Integer ocena;
    private boolean polozio;
}