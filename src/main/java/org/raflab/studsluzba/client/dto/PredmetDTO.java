package org.raflab.studsluzba.client.dto;

import lombok.Data;

@Data
public class PredmetDTO {
    private Long id;
    private String sifra;
    private String naziv;
    private Integer espb;
    private Integer semestar;
}