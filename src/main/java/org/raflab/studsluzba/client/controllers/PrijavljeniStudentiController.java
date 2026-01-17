package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.PrijavljeniStudentResponseDTO;
import org.springframework.stereotype.Controller;

@Controller
public class PrijavljeniStudentiController {
    @FXML public Button btnNazad;
    @FXML private TableView<PrijavljeniStudentResponseDTO> tabelaPrijavljenih;
    @FXML private TableColumn<PrijavljeniStudentResponseDTO, String> colIndeks;
    @FXML private TableColumn<PrijavljeniStudentResponseDTO, String> colIme;
    @FXML private TableColumn<PrijavljeniStudentResponseDTO, String> colPrezime;
    @FXML private Label lblNaslov;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private static IspitResponseDTO odabraniIspit;

    public static void setOdabraniIspit(IspitResponseDTO ispit) {
        odabraniIspit = ispit;
    }

    public PrijavljeniStudentiController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        if (odabraniIspit != null) {
            lblNaslov.setText("Prijavljeni studenti za: " + odabraniIspit.getPredmet().getNaziv());

            colIndeks.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrojIndeksa()));

            colIme.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIme()));

            colPrezime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrezime()));
            ucitajStudente();
        }
    }

    private void ucitajStudente() {
        apiClient.getPrijavljeniStudenti(odabraniIspit.getId())
                .collectList()
                .subscribe(lista -> Platform.runLater(() ->
                        tabelaPrijavljenih.getItems().setAll(lista)));
    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }
}
