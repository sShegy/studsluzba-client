package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.PredmetDTO;
import org.raflab.studsluzba.dto.kurikulum.response.StudijskiProgramResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
@Component
public class StudijskiProgramDetailsController {

    @FXML
    private Label lblNazivPrograma;
    @FXML
    private Label lblOznaka;
    @FXML
    private Label lblGodina;
    @FXML
    private Label lblZvanje;
    @FXML
    private Label lblTrajanje;

    @FXML
    private TableView<PredmetDTO> tabelaPredmeti;
    @FXML
    private TableColumn<PredmetDTO, String> colSifra;
    @FXML
    private TableColumn<PredmetDTO, String> colNazivPredmeta;
    @FXML
    private TableColumn<PredmetDTO, Integer> colEspb;
    @FXML
    private TableColumn<PredmetDTO, Integer> colSemestar;

    @FXML
    private TextField txtSifra;
    @FXML
    private TextField txtNaziv;
    @FXML
    private TextField txtEspb;
    @FXML
    private TextField txtSemestar;

    @FXML
    private Button btnBack;

    private StudijskiProgramResponseDTO trenutniProgram;
    private final NavigationManager navigationManager;
    private final ApiClient apiClient;

    public StudijskiProgramDetailsController(NavigationManager navigationManager, ApiClient apiClient) {
        this.navigationManager = navigationManager;
        this.apiClient = apiClient;
    }

    @FXML
    public void initialize() {

        colSifra.setCellValueFactory(new PropertyValueFactory<>("sifra"));
        colNazivPredmeta.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colSemestar.setCellValueFactory(new PropertyValueFactory<>("semestar"));

        tabelaPredmeti.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PredmetDTO selected = tabelaPredmeti.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleAvg(selected);
                }
            }
        });
    }

    public void setStudijskiProgram(StudijskiProgramResponseDTO program) {
        this.trenutniProgram = program;
        lblNazivPrograma.setText("Program: " + program.getNaziv());
        lblOznaka.setText(program.getOznaka());
        lblGodina.setText(String.valueOf(program.getGodinaAkreditacije()));
        lblZvanje.setText(program.getNazivZvanja());
        lblTrajanje.setText(String.valueOf(program.getTrajanjeSemestra()));

        if (program.getPredmeti() != null) {
            tabelaPredmeti.setItems(FXCollections.observableArrayList(program.getPredmeti()));
        }
    }

    @FXML
    private void handleDodajPredmet() {
        Long programId = trenutniProgram.getId();

        String sifra = txtSifra.getText().trim();
        String naziv = txtNaziv.getText().trim();
        String espbRaw = txtEspb.getText().trim();
        String semestarRaw = txtSemestar.getText().trim();

        if (sifra.isEmpty() || naziv.isEmpty() || espbRaw.isEmpty() || semestarRaw.isEmpty()) {
            displayError("Popunite pravilno polja!");
            return;
        }

        try {
            Integer espb = Integer.valueOf(espbRaw);
            Integer semestar = Integer.valueOf(semestarRaw);

            if (!sifra.matches("^P[1-4]\\d{2}$")) {
                displayError("Popunite pravilno polja!");
                return;
            }
            if (semestar < 1 || semestar > 8) {
                displayError("Popunite pravilno polja!");
                return;
            }
            if (espb < 2 || espb > 8) {
                displayError("Popunite pravilno polja!");
                return;
            }

            apiClient.addPredmet(programId, sifra, naziv, espb, semestar).subscribe(noviId -> {
                Platform.runLater(() -> {
                    refreshTable();

                    txtSifra.clear();
                    txtNaziv.clear();
                    txtEspb.clear();
                    txtSemestar.clear();

                    System.out.println("Predmet uspešno dodat sa ID: " + noviId);
                });
            }, error -> {
                        Platform.runLater(() -> {
                            if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException.Conflict) {
                                displayError("Šifra '" + sifra + "' već postoji!");
                            } else {
                                displayError("Popunite pravilno polja!");
                            }
                        });
                    }
            );
        }catch (NumberFormatException e) {
            displayError("Popunite pravilno polja!");
        }
    }

        private void displayError(String poruka) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška pri unosu");
            alert.setHeaderText(null);
            alert.setContentText(poruka);
            alert.showAndWait();
        }

        @FXML
        private void handleBack () {
            navigationManager.goBack();
        }

        @FXML
        private void handleAvg () {
            PredmetDTO selected = tabelaPredmeti.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleAvg(selected);
            }
        }

        private void handleAvg (PredmetDTO program){
            PredmetAvgController.setPodatakZaPrikaz(program);
            navigationManager.navigateTo("/fxml/PredmetAvg.fxml");
        }

        private void refreshTable() {
            if (trenutniProgram == null)
                return;

            apiClient.getPredmetiByStudijskiProgram(trenutniProgram.getId())
                    .map(details -> new PredmetDTO(
                            details.getId(),
                            details.getSifra(),
                            details.getNaziv(),
                            details.getEspb(),
                            details.getSemestar()
                    ))
                    .collectList()
                    .subscribe(lista -> {
                        Platform.runLater(() -> {
                            tabelaPredmeti.getItems().setAll(lista);
                        });
                    }, Throwable::printStackTrace);
        }
    }
