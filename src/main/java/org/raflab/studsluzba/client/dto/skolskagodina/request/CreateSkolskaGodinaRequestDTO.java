package org.raflab.studsluzba.client.dto.skolskagodina.request;

import lombok.Data;

@Data
public class CreateSkolskaGodinaRequestDTO {
    private String naziv; // npr. "2025/2026"
    private boolean aktivna;
}