package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.PredmetDTO;
import org.springframework.stereotype.Controller;

import java.awt.print.PrinterJob;
import java.util.List;

@Controller
public class PredmetAvgController {

    @FXML private TextField txtGodinaOd, txtGodinaDo;
    @FXML private TableView<PredmetDTO> tabelaProsek;
    @FXML private TableColumn<PredmetDTO, String> colSifra, colNaziv;
    @FXML private TableColumn<PredmetDTO, Double> colProsek;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private List<PredmetDTO> sviPredmeti;

    private static PredmetDTO podatakZaPrikaz;

    public static void setPodatakZaPrikaz(PredmetDTO podatak) {
        podatakZaPrikaz = podatak;
    }

    public PredmetAvgController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        colSifra.setCellValueFactory(new PropertyValueFactory<>("sifra"));
        colNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colProsek.setCellValueFactory(new PropertyValueFactory<>("prosek"));

        if (podatakZaPrikaz != null) {
            prikaziProsekZaPredmet(podatakZaPrikaz);
            podatakZaPrikaz = null;
        }
    }

    private void prikaziProsekZaPredmet(PredmetDTO p) {
        tabelaProsek.getItems().clear();

        apiClient.getProsekZaPredmet(p.getId(), null, null).subscribe(prosek -> {
            Platform.runLater(() -> {
                p.setProsek(prosek != null ? prosek : 0.0);
                tabelaProsek.getItems().add(p);
            });
        }, error -> {
            System.err.println("Greška pri preuzimanju proseka: " + error.getMessage());
        });
    }

    public void setPodaci(List<PredmetDTO> predmeti) {
        this.sviPredmeti = predmeti;
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        Integer od = txtGodinaOd.getText().isEmpty() ? null : Integer.valueOf(txtGodinaOd.getText());
        Integer doG = txtGodinaDo.getText().isEmpty() ? null : Integer.valueOf(txtGodinaDo.getText());

        for (PredmetDTO p : tabelaProsek.getItems()) {
            apiClient.getProsekZaPredmet(p.getId(), od, doG).subscribe(prosek -> {
                Platform.runLater(() -> {
                    p.setProsek(prosek != null ? prosek : 0.0);
                    tabelaProsek.refresh();
                });
            });
        }
    }

    @FXML
    private void handlePrint() {
        System.out.println("Print");
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null && job.showPrintDialog(tabelaProsek.getScene().getWindow())) {
//            boolean success = job.printPage(tabelaProsek);
//            if (success) { job.endJob(); }
//        }
        // komentar proba
    }

    @FXML
    private void handleBack() {
        navigationManager.goBack();
    }
}
