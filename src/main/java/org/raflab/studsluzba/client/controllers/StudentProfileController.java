package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.dto.student.response.PolozenIspitResponseDTO;
import org.raflab.studsluzba.client.dto.student.response.UpisanaGodinaResponseDTO;
import org.raflab.studsluzba.client.service.ApiClient;
import org.springframework.stereotype.Controller;

@Controller
public class StudentProfileController {

    // --- LABELI (HEADER & INFO) ---
    @FXML private Label lblImePrezime;
    @FXML private Label lblIndeks;
    @FXML private Label lblJmbg;
    @FXML private Label lblEmail;
    @FXML private Label lblAdresa;
    @FXML private Label lblSrednjaSkola;

    // --- FINANSIJE (NOVO) ---
    @FXML private Label lblZaduzenje;
    @FXML private Label lblUplaceno;
    @FXML private Label lblStanje;

    // --- TABELA ISPITI ---
    @FXML private TableView<PolozenIspitResponseDTO> ispitiTable;
    @FXML private TableColumn<PolozenIspitResponseDTO, String> colPredmet;
    @FXML private TableColumn<PolozenIspitResponseDTO, Integer> colEspb;
    @FXML private TableColumn<PolozenIspitResponseDTO, Integer> colOcena;
    @FXML private TableColumn<PolozenIspitResponseDTO, String> colDatum;

    // --- TABELA TOK STUDIJA ---
    @FXML private TableView<UpisanaGodinaResponseDTO> tokStudijaTable;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, Integer> colGodinaUpisa;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colSkolskaGodina;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colDatumUpisa;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colTip;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;

    public StudentProfileController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        colPredmet.setCellValueFactory(new PropertyValueFactory<>("nazivPredmeta"));
        colEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colOcena.setCellValueFactory(new PropertyValueFactory<>("ocena"));
        colDatum.setCellValueFactory(new PropertyValueFactory<>("datumPolaganja"));

        colGodinaUpisa.setCellValueFactory(new PropertyValueFactory<>("godinaKojaSeUpisuje"));
        colSkolskaGodina.setCellValueFactory(new PropertyValueFactory<>("skolskaGodina"));
        colDatumUpisa.setCellValueFactory(new PropertyValueFactory<>("datumUpisa"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
    }

    public void loadStudentData(Long studentId) {
        apiClient.getStudentById(studentId).subscribe(student -> {
            Platform.runLater(() -> {
                lblImePrezime.setText(student.getIme() + " " + student.getPrezime());
                lblIndeks.setText(student.getAktivniIndeks() + " - " + student.getStudijskiProgram());
                lblJmbg.setText("JMBG: " + (student.getJmbg() != null ? student.getJmbg() : "Nema podataka"));
                lblEmail.setText("Email: " + student.getEmail());
                lblAdresa.setText("Adresa: " + (student.getAdresaStanovanja() != null ? student.getAdresaStanovanja() : "Nema podataka"));
                lblSrednjaSkola.setText("Mesto rođenja: " + (student.getMestoRodjenja() != null ? student.getMestoRodjenja() : "Nema podataka"));

                if (student.getIndeksId() != null) {
                    loadIspiti(student.getIndeksId());
                    loadTokStudija(student.getIndeksId());
                    loadFinansije(student.getIndeksId());
                } else {
                    System.err.println("UPOZORENJE: Student nema aktivan indeks ID!");
                }
            });
        }, error -> {
            Platform.runLater(() -> System.err.println("Greška pri učitavanju studenta: " + error.getMessage()));
        });
    }

    private void loadIspiti(Long indeksId) {
        apiClient.getPolozeniIspiti(indeksId)
                .collectList()
                .subscribe(ispiti -> {
                    Platform.runLater(() -> {
                        if (ispitiTable != null) {
                            ispitiTable.getItems().setAll(ispiti);
                        }
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pri učitavanju ispita: " + error.getMessage())));
    }

    private void loadTokStudija(Long indeksId) {
        apiClient.getUpisaneGodine(indeksId)
                .collectList()
                .subscribe(godine -> {
                    Platform.runLater(() -> {
                        if (tokStudijaTable != null) {
                            tokStudijaTable.getItems().setAll(godine);
                        }
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pri učitavanju toka studija: " + error.getMessage())));
    }

    // NOVA METODA ZA FINANSIJE
    private void loadFinansije(Long indeksId) {
        apiClient.getStanjeFinansija(indeksId)
                .subscribe(stanje -> {
                    Platform.runLater(() -> {
                        lblZaduzenje.setText(stanje.getUkupnoZaduzenje() + " RSD");
                        lblUplaceno.setText(stanje.getUkupnoUplaceno() + " RSD");
                        lblStanje.setText(stanje.getPreostaloZaUplatu() + " RSD");
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pri učitavanju finansija: " + error.getMessage())));
    }

    @FXML
    public void onBack() {
        navigationManager.goBack();
    }
}