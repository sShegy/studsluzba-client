package org.raflab.studsluzba.client.dto.kurikulum.response;

import lombok.Data;
import org.raflab.studsluzba.client.dto.NastavnikOsnovnoDTO;

import java.util.List;

@Data
public class PredmetDetaljiResponseDTO {
    private Long id;
    private String sifra;
    private String naziv;
    private String opis;
    private Integer espb;
    private Integer semestar;
    private List<NastavnikOsnovnoDTO> nastavnici;
}