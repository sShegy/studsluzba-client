package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class IspitController {
    @FXML private TableView<IspitniRokResponseDTO> tabelaRokova;
    @FXML private TableColumn<IspitniRokResponseDTO, String> colNaziv;
    @FXML private TableColumn<IspitniRokResponseDTO, LocalDate> colPocetak;
    @FXML private TableColumn<IspitniRokResponseDTO, LocalDate> colKraj;

    @FXML private TextField txtNaziv;
    @FXML private DatePicker datumPocetka, datumKraja;
    @FXML private Button btnNazad;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;

    public IspitController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        colNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colPocetak.setCellValueFactory(new PropertyValueFactory<>("datumPocetka"));
        colKraj.setCellValueFactory(new PropertyValueFactory<>("datumZavrsetka"));

        osveziTabelu();

        tabelaRokova.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                IspitniRokResponseDTO selektovani = tabelaRokova.getSelectionModel().getSelectedItem();
                if (selektovani != null) {
                    IspitniRokPredmetiController.setTrenutniRok(selektovani);
                    navigationManager.navigateTo("/fxml/IspitniRokPredmeti.fxml");
                }
            }
        });
    }

    private void osveziTabelu() {
        apiClient.getAllIspitniRokovi()
                .collectList()
                .subscribe(lista -> Platform.runLater(() -> tabelaRokova.getItems().setAll(lista)),
                        Throwable::printStackTrace);
    }

    @FXML
    private void handleAddNewIspitniRok() {
        String naziv = txtNaziv.getText();
        LocalDate pocetak = datumPocetka.getValue();
        LocalDate kraj = datumKraja.getValue();

        if (naziv == null || naziv.trim().isEmpty() || pocetak == null || kraj == null) {
            displayError("Popunite sva polja!");
            return;
        }

        if (kraj.isBefore(pocetak)) {
            displayError("Datum kraja ne može biti pre datuma početka!");
            return;
        }
        long trajanje = java.time.temporal.ChronoUnit.DAYS.between(pocetak, kraj);
        if (trajanje < 10 || trajanje > 30) {
            displayError("Ispitni rok mora trajati minimum 10 dana, a najvise 30 dana!");
            return;
        }

        apiClient.dodajIspitniRok(naziv, pocetak, kraj).subscribe(res -> {
            Platform.runLater(() -> {
                osveziTabelu();
                txtNaziv.clear(); datumPocetka.setValue(null); datumKraja.setValue(null);
            });
        }, Throwable::printStackTrace);
    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }

    private void displayError(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
