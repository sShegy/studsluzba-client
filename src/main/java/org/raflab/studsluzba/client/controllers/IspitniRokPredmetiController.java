package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.DrziPredmetDTO;
import org.raflab.studsluzba.dto.ispit.request.CreateIspitRequestDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.PrijavljeniStudentResponseDTO;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class IspitniRokPredmetiController {
    @FXML private Label lblNaslov;
    @FXML private TableView<IspitResponseDTO> tabelaIspita;
    @FXML private TableColumn<IspitResponseDTO, String> colSifraPredmeta;
    @FXML private TableColumn<IspitResponseDTO, String> colNazivPredmeta;
    @FXML private TableColumn<IspitResponseDTO, String> colDatum;
    @FXML private TableColumn<IspitResponseDTO, String> colVreme;
    //@FXML private TableView<PrijavljeniStudentResponseDTO> tabelaPrijavljenih;
    @FXML private DatePicker dpDatum;
    @FXML private TextField txtVreme;
    @FXML private Label greskaLabel;
    @FXML private ComboBox<DrziPredmetDTO> comboDrziPredmet;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private static IspitniRokResponseDTO trenutniRok;

    public static void setTrenutniRok(IspitniRokResponseDTO rok) {
        trenutniRok = rok;
    }

    public IspitniRokPredmetiController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        if (trenutniRok != null) {
            lblNaslov.setText("Ispiti za rok: " + trenutniRok.getNaziv());

            colSifraPredmeta.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPredmet().getSifra()));

            colNazivPredmeta.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPredmet().getNaziv()));

            colDatum.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getDatumOdrzavanja().toString()));

            colVreme.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getVremePocetka().toString()));

            refreshTable();

            apiClient.getSveVezeNastavnikPredmet()
                    .collectList()
                    .subscribe(lista -> Platform.runLater(() -> {
                        comboDrziPredmet.getItems().setAll(lista);
                    }), error -> {
                        Platform.runLater(() -> {
                            displayError("Greška pri učitavanju: " + error.getMessage());
                            error.printStackTrace();
                        });
                    });

//            tabelaIspita.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//                if (newSelection != null) {
//                    loadPrijavljeni(newSelection.getId());
//                }
//            });

            comboDrziPredmet.setConverter(new StringConverter<DrziPredmetDTO>() {
                @Override
                public String toString(DrziPredmetDTO dto) {
                    return dto == null ? "" : dto.getNazivPredmeta() + " (" + dto.getImeNastavnika() + ")";
                }
                @Override
                public DrziPredmetDTO fromString(String string) { return null; }
            });
        }
    }

    private void refreshTable() {
        apiClient.getIspitiByRokId(trenutniRok.getId())
                .collectList()
                .subscribe(lista -> Platform.runLater(() -> tabelaIspita.getItems().setAll(lista)),
                        Throwable::printStackTrace);
    }

    @FXML private void handleBack() {
        navigationManager.goBack();
    }

//    private void loadPrijavljeni(Long ispitId) {
//        apiClient.getPrijavljeniStudenti(ispitId)
//                .collectList()
//                .subscribe(lista -> Platform.runLater(() -> {
//                    tabelaPrijavljenih.getItems().setAll(lista);
//                }));
//    }

    private void displayError(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    @FXML private void addNewIspit() {
        DrziPredmetDTO selektovan = comboDrziPredmet.getValue();
        LocalDate izabraniDatum = dpDatum.getValue();
        String vremePo = txtVreme.getText();

        if (selektovan == null || izabraniDatum == null || vremePo.isEmpty()) {
            displayError("Molimo popunite sva polja!");
            return;
        }

        LocalDate pocetakRoka = trenutniRok.getDatumPocetka();
        LocalDate krajRoka = trenutniRok.getDatumZavrsetka();

        if (izabraniDatum.isBefore(pocetakRoka) || izabraniDatum.isAfter(krajRoka)) {
            displayError("Datum ispita mora biti u okviru roka: " +
                    pocetakRoka + " do " + krajRoka);
            return;
        }

        LocalTime vreme;
        try {
            vreme = LocalTime.parse(vremePo);
        } catch (Exception e) {
            displayError("Vreme mora biti u formatu HH:mm (npr. 09:00)");
            return;
        }
        CreateIspitRequestDTO request = new CreateIspitRequestDTO();
        request.setDrziPredmetId(selektovan.getId());
        request.setIspitniRokId(trenutniRok.getId());
        request.setDatumOdrzavanja(dpDatum.getValue());
        request.setVremePocetka(LocalTime.parse(txtVreme.getText()));

        apiClient.zakaziIspit(request).subscribe(res -> {
            Platform.runLater(() -> {
                refreshTable();
                displayError("Ispit uspešno zakazan!");
                txtVreme.clear();
                dpDatum.setValue(null);
            });
        });
    }

    @FXML
    private void handleShowPrijavljeni() {
        IspitResponseDTO selektovani = tabelaIspita.getSelectionModel().getSelectedItem();

        if (selektovani == null) {
            displayError("Molimo odaberite ispit iz tabele!");
            return;
        }

        PrijavljeniStudentiController.setOdabraniIspit(selektovani);

        navigationManager.navigateTo("/fxml/PrijavljeniStudenti.fxml");
    }

    @FXML
    private void handlePrijava(){
        IspitResponseDTO selektovani = tabelaIspita.getSelectionModel().getSelectedItem();

        if (selektovani == null) {
            displayError("Molimo odaberite ispit iz tabele!");
            return;
        }
        PrijavljeniStudentiController.setOdabraniIspit(selektovani);

        navigationManager.navigateTo("/fxml/Prijava.fxml");
    }
}
