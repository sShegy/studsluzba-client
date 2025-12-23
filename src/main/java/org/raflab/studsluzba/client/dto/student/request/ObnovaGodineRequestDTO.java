package org.raflab.studsluzba.client.dto.student.request;

import lombok.Data;

import java.util.List;

@Data
public class ObnovaGodineRequestDTO {
    private Long studentIndeksId;
    private Long skolskaGodinaId;
    private List<Long> predmetiIzNaredneGodineIds;
    private Integer godinaKojaSeObnavlja;

}