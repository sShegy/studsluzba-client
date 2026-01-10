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

    @FXML private Label lblNazivPrograma;
    @FXML private Label lblOznaka;
    @FXML private Label lblGodina;
    @FXML private Label lblZvanje;
    @FXML private Label lblTrajanje;

    @FXML private TableView<PredmetDTO> tabelaPredmeti;
    @FXML private TableColumn<PredmetDTO, String> colSifra;
    @FXML private TableColumn<PredmetDTO, String> colNazivPredmeta;
    @FXML private TableColumn<PredmetDTO, Integer> colEspb;
    @FXML private TableColumn<PredmetDTO, Integer> colSemestar;

    @FXML private TextField txtSifra;
    @FXML private TextField txtNaziv;
    @FXML private TextField txtEspb;
    @FXML private TextField txtSemestar;

    @FXML private Button btnBack;

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
        String sifra = txtSifra.getText();
        String naziv = txtNaziv.getText();
        Integer espb = Integer.valueOf(txtEspb.getText());
        Integer semestar = Integer.valueOf(txtSemestar.getText());
        Long programId = trenutniProgram.getId();

        apiClient.addPredmet(programId, sifra, naziv, espb, semestar).subscribe(noviId -> {
            Platform.runLater(() -> {
                PredmetDTO zaTabelu = new PredmetDTO(noviId, sifra, naziv, espb, semestar);
                tabelaPredmeti.getItems().add(zaTabelu);

                osveziTabelu();

                txtSifra.clear();
                txtNaziv.clear();
                txtEspb.clear();
                txtSemestar.clear();
            });
        }, Throwable::printStackTrace);
    }

    @FXML
    private void handleBack() {
        navigationManager.goBack();
    }

    @FXML
    private void handleAvg() {
        PredmetDTO selected = tabelaPredmeti.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleAvg(selected);
        }
    }

    private void handleAvg(PredmetDTO program) {
        PredmetAvgController.setPodatakZaPrikaz(program);
        navigationManager.navigateTo("/fxml/PredmetAvg.fxml");
    }

    private void osveziTabelu() {
        if (trenutniProgram == null)
            return;

        apiClient.getPredmetiByStudijskiProgram(trenutniProgram.getId())
                .map(details -> new PredmetDTO(details.getId(), details.getSifra(), details.getNaziv(), details.getEspb(), details.getSemestar()))
                .collectList()
                .subscribe(lista -> {
                    Platform.runLater(() -> {
                        tabelaPredmeti.getItems().setAll(lista);
                    });
                }, Throwable::printStackTrace);
    }
}
