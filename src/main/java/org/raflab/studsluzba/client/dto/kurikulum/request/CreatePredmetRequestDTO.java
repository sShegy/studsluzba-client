package org.raflab.studsluzba.client.dto.kurikulum.request;

import lombok.Data;

@Data
public class CreatePredmetRequestDTO {
    private String sifra;
    private String naziv;
    private String opis;
    private Integer espb;
    private Integer semestar;
    private boolean obavezan;
    private Long studijskiProgramId;
}