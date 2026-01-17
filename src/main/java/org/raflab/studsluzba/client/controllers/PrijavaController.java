package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.student.response.StudentProfileResponseDTO;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

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

//    private void loadStudenti() {
//        apiClient.getStudentiZaPrijavu(odabraniIspit.getId())
//                .collectList()
//                .subscribe(lista -> Platform.runLater(() -> tabelaStudenata.getItems().setAll(lista)));
//    }
    private void loadStudenti() {
        System.out.println("DEBUG: Pozivam API za Ispit ID = " + odabraniIspit.getId());
        apiClient.getStudentiZaPrijavu(odabraniIspit.getId())
                .collectList()
                .subscribe(lista -> {
                    // OVO MORAŠ DA VIDIŠ U KONZOLI
                    System.out.println("STIGLO SA SERVERA: " + lista.size());
                    if(!lista.isEmpty()) {
                        System.out.println("PRVI STUDENT: " + lista.get(0).getIme());
                    }

                    Platform.runLater(() -> {
                        tabelaStudenata.getItems().setAll(lista);
                    });
                }, error -> {
                    System.err.println("GREŠKA U MREŽI: " + error.getMessage());
                    error.printStackTrace();
                });
}

    @FXML
    private void handlePrijavi() {
        StudentProfileResponseDTO selektovani = tabelaStudenata.getSelectionModel().getSelectedItem();

        if (selektovani == null) {
            displayError("Molimo odaberite studenta!");
            return;
        }

//        LocalDateTime sada = LocalDateTime.now();
//        LocalDateTime datumIspita = odabraniIspit.getDatumIspita(); // Pretpostavka da DTO ima LocalDateTime
//
//        if (datumIspita.isBefore(sada)) {
//            displayError("Greška: Ispit je već održan!");
//            return;
//        }
//
//        // Provera za 2 dana (48 sati)
//        if (sada.isAfter(datumIspita.minusDays(2))) {
//            displayError("Rok za prijavu je istekao! Prijave se zatvaraju 2 dana pre ispita.");
//            return;
//        }
        ///TODO ovo odradi sutra
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

    private void displayError(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
