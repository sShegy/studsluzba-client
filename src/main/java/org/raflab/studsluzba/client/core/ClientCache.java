package org.raflab.studsluzba.client.core;

import org.raflab.studsluzba.dto.sifarnik.response.SrednjaSkolaResponseDTO;
import org.raflab.studsluzba.dto.skolskagodina.response.SkolskaGodinaResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientCache {

    private List<SrednjaSkolaResponseDTO> srednjeSkole;
    private List<SkolskaGodinaResponseDTO> skolskeGodine;

    // --- SREDNJE ŠKOLE ---
    public List<SrednjaSkolaResponseDTO> getSrednjeSkole() {
        return srednjeSkole;
    }

    public void setSrednjeSkole(List<SrednjaSkolaResponseDTO> srednjeSkole) {
        this.srednjeSkole = srednjeSkole;
    }

    public boolean hasSrednjeSkole() {
        return srednjeSkole != null && !srednjeSkole.isEmpty();
    }

    // --- ŠKOLSKE GODINE ---
    public List<SkolskaGodinaResponseDTO> getSkolskeGodine() {
        return skolskeGodine;
    }

    public void setSkolskeGodine(List<SkolskaGodinaResponseDTO> skolskeGodine) {
        this.skolskeGodine = skolskeGodine;
    }

    public boolean hasSkolskeGodine() {
        return skolskeGodine != null && !skolskeGodine.isEmpty();
    }

    // Metoda za brisanje keša (ako zatreba)
    public void clear() {
        this.srednjeSkole = null;
        this.skolskeGodine = null;
    }
}
