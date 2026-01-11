package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO;
import org.springframework.stereotype.Controller;

@Controller
public class IspitniRokPredmetiController {
    @FXML private Label lblNaslov;
    @FXML private TableView<IspitResponseDTO> tabelaIspita;
    @FXML private TableColumn<IspitResponseDTO, String> colSifraPredmeta;
    @FXML private TableColumn<IspitResponseDTO, String> colNazivPredmeta;
    @FXML private TableColumn<IspitResponseDTO, String> colDatum;
    @FXML private TableColumn<IspitResponseDTO, String> colVreme;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;

    private static IspitniRokResponseDTO trenutniRok;

    public static void setTrenutniRok(IspitniRokResponseDTO rok) { trenutniRok = rok; }

    public IspitniRokPredmetiController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        if (trenutniRok != null) {
            lblNaslov.setText("Ispiti za rok: " + trenutniRok.getNaziv());

//            colSifra.setCellValueFactory(new PropertyValueFactory<>("sifraPredmeta"));
//            colNaziv.setCellValueFactory(new PropertyValueFactory<>("nazivPredmeta"));
//            colDatum.setCellValueFactory(new PropertyValueFactory<>("vremeOdrzavanja"));

            colSifraPredmeta.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPredmet().getSifra()));

            colNazivPredmeta.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPredmet().getNaziv()));

            colDatum.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getDatumOdrzavanja().toString()));

            colVreme.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getVremePocetka().toString()));

            osveziTabeluIspita();
        }
    }

    private void osveziTabeluIspita() {
        apiClient.getIspitiByRokId(trenutniRok.getId())
                .collectList()
                .subscribe(lista -> Platform.runLater(() -> tabelaIspita.getItems().setAll(lista)),
                        Throwable::printStackTrace);
    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }
}
