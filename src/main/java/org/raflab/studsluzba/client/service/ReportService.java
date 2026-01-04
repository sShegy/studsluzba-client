package org.raflab.studsluzba.client.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.raflab.studsluzba.dto.student.response.PolozenIspitResponseDTO;
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

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("studentImePrezime", imePrezime);
            parameters.put("brojIndeksa", indeks);
            parameters.put("datum", java.time.LocalDate.now().toString());

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
            InputStream reportStream = getClass().getResourceAsStream("/reports/Uverenje.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Fajl izveštaja nije pronađen!");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("studentImePrezime", imePrezime);
            parameters.put("brojIndeksa", indeks);
            parameters.put("studijskiProgram", program);
            parameters.put("datum", java.time.LocalDate.now().toString());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri generisanju izveštaja: " + e.getMessage());
        }
    }
}