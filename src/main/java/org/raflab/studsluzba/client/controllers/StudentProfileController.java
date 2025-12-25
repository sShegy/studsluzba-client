package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.dto.PredmetDTO;
import org.raflab.studsluzba.client.dto.student.request.CreateUplataRequestDTO;
import org.raflab.studsluzba.client.dto.student.response.PolozenIspitResponseDTO;
import org.raflab.studsluzba.client.dto.student.response.UpisanaGodinaResponseDTO;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.client.service.ReportService;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class StudentProfileController {

    // --- LABELI (HEADER & INFO) ---
    @FXML private Label lblImePrezime;
    @FXML private Label lblIndeks;
    @FXML private Label lblJmbg;
    @FXML private Label lblEmail;
    @FXML private Label lblAdresa;
    @FXML private Label lblSrednjaSkola;

    // --- FINANSIJE ---
    @FXML private Label lblZaduzenje;
    @FXML private Label lblUplaceno;
    @FXML private Label lblStanje;

    // --- TABELA ISPITI (POLOŽENI) ---
    @FXML private TableView<PolozenIspitResponseDTO> ispitiTable;
    @FXML private TableColumn<PolozenIspitResponseDTO, String> colPredmet;
    @FXML private TableColumn<PolozenIspitResponseDTO, Integer> colEspb;
    @FXML private TableColumn<PolozenIspitResponseDTO, Integer> colOcena;
    @FXML private TableColumn<PolozenIspitResponseDTO, String> colDatum;

    // --- TABELA NEPOLOŽENI ---
    @FXML private TableView<PredmetDTO> nepolozeniTable;
    @FXML private TableColumn<PredmetDTO, String> colNepolozeniSifra;
    @FXML private TableColumn<PredmetDTO, String> colNepolozeniNaziv;
    @FXML private TableColumn<PredmetDTO, Integer> colNepolozeniEspb;
    @FXML private TableColumn<PredmetDTO, Integer> colNepolozeniSemestar;

    // --- TABELA TOK STUDIJA ---
    @FXML private TableView<UpisanaGodinaResponseDTO> tokStudijaTable;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, Integer> colGodinaUpisa;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colSkolskaGodina;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colDatumUpisa;
    @FXML private TableColumn<UpisanaGodinaResponseDTO, String> colTip;

    // --- PROSEK I ESPB ---
    @FXML private Label lblProsek;
    @FXML private Label lblEspb;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private final ReportService reportService;

    private Long currentIndeksId;

    public StudentProfileController(ApiClient apiClient, NavigationManager navigationManager, ReportService reportService) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
        this.reportService = reportService;
    }

    @FXML
    public void initialize() {
        // 1. Konfiguracija kolona za POLOŽENE ISPITE
        colPredmet.setCellValueFactory(new PropertyValueFactory<>("nazivPredmeta"));
        colEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colOcena.setCellValueFactory(new PropertyValueFactory<>("ocena"));
        colDatum.setCellValueFactory(new PropertyValueFactory<>("datumPolaganja"));

        // 2. Konfiguracija kolona za NEPOLOŽENE ISPITE
        colNepolozeniSifra.setCellValueFactory(new PropertyValueFactory<>("sifra"));
        colNepolozeniNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colNepolozeniEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colNepolozeniSemestar.setCellValueFactory(new PropertyValueFactory<>("semestar"));

        // 3. Konfiguracija kolona za TOK STUDIJA
        colGodinaUpisa.setCellValueFactory(new PropertyValueFactory<>("godinaKojaSeUpisuje"));
        colSkolskaGodina.setCellValueFactory(new PropertyValueFactory<>("skolskaGodina"));
        colDatumUpisa.setCellValueFactory(new PropertyValueFactory<>("datumUpisa"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
    }

    // Glavna metoda za učitavanje podataka
    public void loadStudentData(Long studentId) {
        apiClient.getStudentById(studentId).subscribe(student -> {
            Platform.runLater(() -> {
                // Popunjavanje osnovnih podataka
                lblImePrezime.setText(student.getIme() + " " + student.getPrezime());
                lblIndeks.setText(student.getAktivniIndeks() + " - " + student.getStudijskiProgram());
                lblJmbg.setText("JMBG: " + (student.getJmbg() != null ? student.getJmbg() : "Nema podataka"));
                lblEmail.setText("Email: " + student.getEmail());
                lblAdresa.setText("Adresa: " + (student.getAdresaStanovanja() != null ? student.getAdresaStanovanja() : "Nema podataka"));
                lblSrednjaSkola.setText("Mesto rođenja: " + (student.getMestoRodjenja() != null ? student.getMestoRodjenja() : "Nema podataka"));

                // Učitavanje podataka za tabove (ako postoji indeks)
                if (student.getIndeksId() != null) {
                    this.currentIndeksId = student.getIndeksId(); // Čuvamo ID za kasnije (uplata)

                    loadIspiti(student.getIndeksId());
                    loadNepolozeni(student.getIndeksId());
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

                            // --- FIX: Filtriramo samo ispite koji imaju unetu ocenu i ESPB ---
                            // Ovo sprečava NullPointerException ako je ocena null
                            var validniIspiti = ispiti.stream()
                                    .filter(i -> i.getOcena() != null && i.getEspb() != null)
                                    .toList();

                            if (!validniIspiti.isEmpty()) {
                                double zbirOcena = validniIspiti.stream().mapToInt(i -> i.getOcena()).sum();
                                int zbirEspb = validniIspiti.stream().mapToInt(i -> i.getEspb()).sum();
                                double prosek = zbirOcena / validniIspiti.size();

                                lblProsek.setText(String.format("Prosek: %.2f", prosek));
                                lblEspb.setText("Ukupno ESPB: " + zbirEspb);
                            } else {
                                lblProsek.setText("Prosek: 0.00");
                                lblEspb.setText("Ukupno ESPB: 0");
                            }
                        }
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška ispiti: " + error.getMessage())));
    }

    private void loadNepolozeni(Long indeksId) {
        apiClient.getNepolozeniIspiti(indeksId)
                .collectList()
                .subscribe(predmeti -> {
                    Platform.runLater(() -> {
                        if (nepolozeniTable != null) {
                            nepolozeniTable.getItems().setAll(predmeti);
                        }
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška nepoloženi: " + error.getMessage())));
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
    public void onNovaUplata() {
        if (this.currentIndeksId == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nova Uplata");
        dialog.setHeaderText("Unos uplate školarine");
        dialog.setContentText("Unesite iznos (RSD):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(iznosStr -> {
            try {
                BigDecimal iznos = new BigDecimal(iznosStr);
                CreateUplataRequestDTO request = new CreateUplataRequestDTO();
                request.setStudentIndeksId(this.currentIndeksId);
                request.setIznos(iznos);
                request.setDatumUplate(LocalDate.now());
                request.setSvrha("Uplata školarine (Klijent App)");

                apiClient.addUplata(request).subscribe(response -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Uspeh");
                        alert.setHeaderText(null);
                        alert.setContentText("Uplata uspešno evidentirana!");
                        alert.showAndWait();
                        loadFinansije(this.currentIndeksId);
                    });
                }, error -> Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Greška");
                    alert.setContentText("Neuspešna uplata: " + error.getMessage());
                    alert.showAndWait();
                }));

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Morate uneti validan broj!");
                alert.showAndWait();
            }
        });
    }

    @FXML
    public void onStampajUverenje() {
        try {
            String ime = lblImePrezime.getText();
            String indeks = lblIndeks.getText();
            reportService.generateUverenje(ime, indeks, "Osnovne akademske studije");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setHeaderText("Greška pri štampi");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onStampajPolozeno() {
        try {
            String ime = lblImePrezime.getText();
            String indeks = lblIndeks.getText();

            var listaIspita = new java.util.ArrayList<>(ispitiTable.getItems());

            if (listaIspita.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Student nema položenih ispita za štampu.");
                alert.showAndWait();
                return;
            }


            for (PolozenIspitResponseDTO ispit : listaIspita) {
                if (ispit.getGodinaStudija() == null) {
                    ispit.setGodinaStudija(1); // Stavljamo sve u prvu godinu
                }
            }

            listaIspita.sort((i1, i2) -> {
                int g1 = i1.getGodinaStudija() != null ? i1.getGodinaStudija() : 0;
                int g2 = i2.getGodinaStudija() != null ? i2.getGodinaStudija() : 0;
                return Integer.compare(g1, g2);
            });

            reportService.generateUverenjePolozeni(ime, indeks, listaIspita);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Greška pri štampi: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onBack() {
        navigationManager.goBack();
    }
}