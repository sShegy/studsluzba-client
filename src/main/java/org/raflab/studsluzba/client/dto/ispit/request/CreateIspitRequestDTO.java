package org.raflab.studsluzba.client.dto.ispit.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateIspitRequestDTO {
    private Long drziPredmetId;
    private Long ispitniRokId;
    private LocalDate datumOdrzavanja;
    private LocalTime vremePocetka;
}