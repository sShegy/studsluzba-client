package org.raflab.studsluzba.client.dto.nastavnik.request;

import lombok.Data;

@Data
public class DodajObrazovanjeRequestDTO {
    private Long nastavnikId;
    private Long visokoskolskaUstanovaId;
    private String vrstaStudija;
    private String stecenoZvanje;
    private Integer godinaZavrsetka;
}