package org.raflab.studsluzba.client.dto.nastavnik.response;

import lombok.Data;

@Data
public class ObrazovanjeResponseDTO {
    private String nazivUstanove;
    private String vrstaStudija;
    private String stecenoZvanje;
    private Integer godinaZavrsetka;
}