package org.raflab.studsluzba.client.dto.student.request;

import lombok.Data;

import java.util.List;

@Data
public class UpisGodineRequestDTO {
    private Long studentIndeksId;
    private Long skolskaGodinaId;
    private List<Long> predmetiKojeUpisujeIds;
    private Integer godinaKojaSeUpisuje;
}