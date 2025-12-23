package org.raflab.studsluzba.client.dto.student.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpisanaGodinaResponseDTO {
    private String skolskaGodina;
    private Integer godinaKojaSeUpisuje;
    private LocalDate datumUpisa;
    private String tip; // "Upis" ili "Obnova"
}