package org.raflab.studsluzba.client.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.raflab.studsluzba.client.dto.student.response.PolozenIspitResponseDTO;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    public void generateUverenjePolozeni(String imePrezime, String indeks, List<PolozenIspitResponseDTO> ispiti) {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/reports/UverenjePolozeni.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Fajl izveštaja (UverenjePolozeni.jrxml) nije pronađen!");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Parametri (Zaglavlje)
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("studentImePrezime", imePrezime);
            parameters.put("brojIndeksa", indeks);
            parameters.put("datum", java.time.LocalDate.now().toString());

            // Podaci (Tabela) - Pretvaramo listu DTO-ova u DataSource za Jasper
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ispiti);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri generisanju izveštaja o ispitima: " + e.getMessage());
        }
    }


    public void generateUverenje(String imePrezime, String indeks, String program) {
        try {
            // 1. Učitaj dizajn izveštaja
            InputStream reportStream = getClass().getResourceAsStream("/reports/Uverenje.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Fajl izveštaja nije pronađen!");
            }

            // 2. Kompajliraj izveštaj
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Popuni parametre
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("studentImePrezime", imePrezime);
            parameters.put("brojIndeksa", indeks);
            parameters.put("studijskiProgram", program);
            parameters.put("datum", java.time.LocalDate.now().toString());

            // 4. Napuni izveštaj podacima (koristimo JREmptyDataSource jer nemamo listu, samo jedan papir)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // 5. Prikaži izveštaj (false znači da se NE gasi cela aplikacija kad zatvoriš izveštaj)
            JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri generisanju izveštaja: " + e.getMessage());
        }
    }
}