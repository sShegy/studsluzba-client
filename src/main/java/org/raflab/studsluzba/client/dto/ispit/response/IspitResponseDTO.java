package org.raflab.studsluzba.client.dto.ispit.response;

import lombok.Data;
import org.raflab.studsluzba.client.dto.NastavnikOsnovnoDTO;
import org.raflab.studsluzba.client.dto.PredmetDTO;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class IspitResponseDTO {
    private Long id;
    private PredmetDTO predmet;
    private NastavnikOsnovnoDTO nastavnik;
    private LocalDate datumOdrzavanja;
    private LocalTime vremePocetka;
    private boolean zakljucen;
}