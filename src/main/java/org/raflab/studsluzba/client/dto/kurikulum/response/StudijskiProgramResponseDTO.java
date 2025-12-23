package org.raflab.studsluzba.client.dto.kurikulum.response;

import lombok.Data;
import org.raflab.studsluzba.client.dto.PredmetDTO;

import java.util.List;

@Data
public class StudijskiProgramResponseDTO {
    private Long id;
    private String oznaka;
    private String naziv;
    private List<PredmetDTO> predmeti;
}