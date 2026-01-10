package org.raflab.studsluzba.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO;
import org.springframework.stereotype.Controller;

@Controller
public class IspitController {
    @FXML private TableView<IspitniRokResponseDTO> tabelaRokova;
    @FXML private TableColumn<IspitniRokResponseDTO, String> colNaziv;
    @FXML private TableColumn<IspitniRokResponseDTO, String> colPocetak;
    @FXML private TableColumn<IspitniRokResponseDTO, String> colKraj;

    @FXML private TextField txtNaziv, txtPocetak, txtKraj;

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
        colKraj.setCellValueFactory(new PropertyValueFactory<>("datumKraja"));

        //osveziTabelu();

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

//    private void osveziTabelu() {
//        apiClient.getAllIspitniRokovi()
//                .collectList()
//                .subscribe(lista -> Platform.runLater(() -> tabelaRokova.getItems().setAll(lista)),
//                        Throwable::printStackTrace);
//    }
//
//    @FXML
//    private void handleDodajRok() {
//        String naziv = txtNaziv.getText();
//        String pocetak = txtPocetak.getText();
//        String kraj = txtKraj.getText();
//
//        apiClient.dodajIspitniRok(naziv, pocetak, kraj).subscribe(res -> {
//            Platform.runLater(() -> {
//                osveziTabelu();
//                txtNaziv.clear(); txtPocetak.clear(); txtKraj.clear();
//            });
//        }, Throwable::printStackTrace);
//    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }
}
