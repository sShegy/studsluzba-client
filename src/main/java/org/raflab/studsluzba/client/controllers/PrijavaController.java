package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.student.response.StudentProfileResponseDTO;
import org.springframework.stereotype.Controller;

@Controller
public class PrijavaController {

    @FXML public Button btnNazad;
    @FXML private TableView<StudentProfileResponseDTO> tabelaStudenata;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colIndeks;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colIme;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colPrezime;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private static IspitResponseDTO odabraniIspit;

    public static void setOdabraniIspit(IspitResponseDTO ispit) {
        odabraniIspit = ispit;
    }

    public PrijavaController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        colIndeks.setCellValueFactory(new PropertyValueFactory<>("aktivniIndeks"));
        colIme.setCellValueFactory(new PropertyValueFactory<>("ime"));
        colPrezime.setCellValueFactory(new PropertyValueFactory<>("prezime"));

        if (odabraniIspit != null) {
            loadStudenti();
        }
    }

    private void loadStudenti() {
        apiClient.getStudentiZaPrijavu(odabraniIspit.getId())
                .collectList()
                .subscribe(lista -> Platform.runLater(() -> tabelaStudenata.getItems().setAll(lista)));
    }

    @FXML
    private void handlePrijavi() {
        StudentProfileResponseDTO selektovani = tabelaStudenata.getSelectionModel().getSelectedItem();
        if (selektovani != null) {
            apiClient.prijaviStudenta(odabraniIspit.getId(), selektovani.getIndeksId())
                    .subscribe(v -> Platform.runLater(() -> {
                        loadStudenti();
                        System.out.println("Student uspešno prijavljen.");
                    }));
        }
    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }
}
