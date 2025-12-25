package org.raflab.studsluzba.client.dto.student.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PolozenIspitResponseDTO {
    private String sifraPredmeta;
    private String nazivPredmeta;
    private Integer ocena;
    private Integer espb;
    private LocalDate datumPolaganja;

    private Integer godinaStudija;

}