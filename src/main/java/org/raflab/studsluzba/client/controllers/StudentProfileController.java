package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.raflab.studsluzba.client.core.ClientCache;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.dto.PredmetDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.skolskagodina.response.SkolskaGodinaResponseDTO;
import org.raflab.studsluzba.dto.student.request.CreateUplataRequestDTO;
import org.raflab.studsluzba.dto.student.request.ObnovaGodineRequestDTO;
import org.raflab.studsluzba.dto.student.request.UpisGodineRequestDTO;
import org.raflab.studsluzba.dto.student.response.PolozenIspitResponseDTO;
import org.raflab.studsluzba.dto.student.response.UpisanaGodinaResponseDTO;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.client.service.ReportService;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public class StudentProfileController {

    // --- LABELI ---
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

    // --- TABELA ISPITI ---
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

    // --- TABELA PRIJAVA ISPITA (NOVO) ---
    @FXML private TableView<IspitResponseDTO> tblPrijavaIspita;
    @FXML private TableColumn<IspitResponseDTO, String> colPrijavaPredmet;
    @FXML private TableColumn<IspitResponseDTO, String> colPrijavaDatum;
    @FXML private TableColumn<IspitResponseDTO, String> colPrijavaVreme;
    @FXML private TableColumn<IspitResponseDTO, Void> colPrijavaAkcija;

    // --- PROSEK I ESPB ---
    @FXML private Label lblProsek;
    @FXML private Label lblEspb;

    // --- TAB PANE (ZA NAVIGACIJU) ---
    @FXML private TabPane mainTabPane;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private final ReportService reportService;
    private final ClientCache clientCache; // <--- KEŠIRANJE

    private Long currentIndeksId;
    private Long currentStudentId; // Čuvamo i ID studenta za reload

    public StudentProfileController(ApiClient apiClient, NavigationManager navigationManager, ReportService reportService, ClientCache clientCache) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
        this.reportService = reportService;
        this.clientCache = clientCache;
    }

    @FXML
    public void initialize() {
        // Konfiguracija kolona
        colPredmet.setCellValueFactory(new PropertyValueFactory<>("nazivPredmeta"));
        colEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colOcena.setCellValueFactory(new PropertyValueFactory<>("ocena"));
        colDatum.setCellValueFactory(new PropertyValueFactory<>("datumPolaganja"));

        colNepolozeniSifra.setCellValueFactory(new PropertyValueFactory<>("sifra"));
        colNepolozeniNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colNepolozeniEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colNepolozeniSemestar.setCellValueFactory(new PropertyValueFactory<>("semestar"));

        colGodinaUpisa.setCellValueFactory(new PropertyValueFactory<>("godinaKojaSeUpisuje"));
        colSkolskaGodina.setCellValueFactory(new PropertyValueFactory<>("skolskaGodina"));
        colDatumUpisa.setCellValueFactory(new PropertyValueFactory<>("datumUpisa"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));

        // --- KONFIGURACIJA TABELE ZA PRIJAVU ISPITA (NOVO) ---
        if (colPrijavaPredmet != null) {
            colPrijavaPredmet.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPredmet().getNaziv()));

            colPrijavaDatum.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDatumOdrzavanja().toString()));

            colPrijavaVreme.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getVremePocetka().toString()));

            // --- DUGME "PRIJAVI" U TABELI ---
            colPrijavaAkcija.setCellFactory(new Callback<>() {
                @Override
                public TableCell<IspitResponseDTO, Void> call(final TableColumn<IspitResponseDTO, Void> param) {
                    return new TableCell<>() {
                        private final Button btn = new Button("Prijavi");
                        {
                            btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                            btn.setOnAction(event -> {
                                IspitResponseDTO ispit = getTableView().getItems().get(getIndex());
                                handlePrijavaIspita(ispit);
                            });
                        }
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    };
                }
            });
        }

        // --- LISTENER ZA TABOVE (NAVIGACIJA) ---
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                // Ako je promena programska (zbog Back dugmeta), ignoriši
                if (navigationManager.isNavigating()) return;

                int newIndex = mainTabPane.getSelectionModel().getSelectedIndex();

                // Beležimo promenu stanja u istoriju
                navigationManager.recordStateChange(controller -> {
                    if (controller instanceof StudentProfileController) {
                        StudentProfileController c = (StudentProfileController) controller;
                        // Kad se vratimo na ovo stanje, učitaj podatke I selektuj tab
                        c.loadStudentData(this.currentStudentId);
                        c.selectTab(newIndex);
                    }
                });
            });
        }
    }

    // Metoda koju NavigationManager poziva da selektuje tab
    public void selectTab(int index) {
        if (mainTabPane != null && index >= 0 && index < mainTabPane.getTabs().size()) {
            mainTabPane.getSelectionModel().select(index);
        }
    }

    public void loadStudentData(Long studentId) {
        this.currentStudentId = studentId;
        apiClient.getStudentById(studentId).subscribe(student -> {
            Platform.runLater(() -> {
                lblImePrezime.setText(student.getIme() + " " + student.getPrezime());
                lblIndeks.setText(student.getAktivniIndeks() + " - " + student.getStudijskiProgram());
                lblJmbg.setText("JMBG: " + (student.getJmbg() != null ? student.getJmbg() : "Nema podataka"));
                lblEmail.setText("Email: " + student.getEmail());
                lblAdresa.setText("Adresa: " + (student.getAdresaStanovanja() != null ? student.getAdresaStanovanja() : "Nema podataka"));
                lblSrednjaSkola.setText("Mesto rođenja: " + (student.getMestoRodjenja() != null ? student.getMestoRodjenja() : "Nema podataka"));

                if (student.getIndeksId() != null) {
                    this.currentIndeksId = student.getIndeksId();
                    loadIspiti(student.getIndeksId());
                    loadNepolozeni(student.getIndeksId());
                    loadTokStudija(student.getIndeksId());
                    loadFinansije(student.getIndeksId());

                    // --- UČITAJ DOSTUPNE ISPITE ZA PRIJAVU (NOVO) ---
                    loadDostupniIspiti(this.currentStudentId);
                } else {
                    showError("Upozorenje", "Student nema aktivan indeks ID!");
                }
            });
        }, error -> showError("Greška", "Greška pri učitavanju studenta: " + error.getMessage()));
    }

    private void loadIspiti(Long indeksId) {
        apiClient.getPolozeniIspiti(indeksId).collectList().subscribe(ispiti -> {
            Platform.runLater(() -> {
                if (ispitiTable != null) {
                    ispitiTable.getItems().setAll(ispiti);
                    var validniIspiti = ispiti.stream().filter(i -> i.getOcena() != null && i.getEspb() != null).toList();
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
        }, error -> showError("Greška", "Greška pri učitavanju ispita: " + error.getMessage()));
    }

    private void loadNepolozeni(Long indeksId) {
        apiClient.getNepolozeniIspiti(indeksId).collectList().subscribe(predmeti -> {
            Platform.runLater(() -> {
                if (nepolozeniTable != null) nepolozeniTable.getItems().setAll(predmeti);
            });
        }, error -> showError("Greška", "Greška pri učitavanju nepoloženih ispita: " + error.getMessage()));
    }

    private void loadTokStudija(Long indeksId) {
        apiClient.getUpisaneGodine(indeksId).collectList().subscribe(upisi -> {
            apiClient.getObnovljeneGodine(indeksId).collectList().subscribe(obnove -> {
                Platform.runLater(() -> {
                    java.util.List<UpisanaGodinaResponseDTO> sveGodine = new java.util.ArrayList<>();
                    for (UpisanaGodinaResponseDTO u : upisi) { u.setTip("Upis"); sveGodine.add(u); }
                    for (UpisanaGodinaResponseDTO o : obnove) { o.setTip("Obnova"); sveGodine.add(o); }
                    sveGodine.sort((g1, g2) -> {
                        if (g1.getDatumUpisa() == null || g2.getDatumUpisa() == null) return 0;
                        return g2.getDatumUpisa().compareTo(g1.getDatumUpisa());
                    });
                    tokStudijaTable.getItems().clear();
                    tokStudijaTable.getItems().addAll(sveGodine);
                    tokStudijaTable.refresh();
                });
            }, error -> showError("Greška", "Neuspešno učitavanje obnova: " + error.getMessage()));
        }, error -> showError("Greška", "Neuspešno učitavanje upisa: " + error.getMessage()));
    }

    private void loadFinansije(Long indeksId) {
        apiClient.getStanjeFinansija(indeksId).subscribe(stanje -> {
            Platform.runLater(() -> {
                lblZaduzenje.setText(stanje.getUkupnoZaduzenje() + " RSD");
                lblUplaceno.setText(stanje.getUkupnoUplaceno() + " RSD");
                lblStanje.setText(stanje.getPreostaloZaUplatu() + " RSD");
            });
        }, error -> showError("Greška", "Greška pri učitavanju finansija: " + error.getMessage()));
    }


    private void loadDostupniIspiti(Long studentId) {
        apiClient.getDostupniIspitiZaStudenta(studentId).collectList().subscribe(ispiti -> {
            Platform.runLater(() -> {
                if (tblPrijavaIspita != null) {
                    tblPrijavaIspita.getItems().setAll(ispiti);
                }
            });
        }, error -> {
            // Samo logujemo grešku, ne iskačemo korisniku ako nema ispita ili endpoint još ne postoji
            System.out.println("Info: Nema dostupnih ispita ili greška: " + error.getMessage());
        });
    }

    private void handlePrijavaIspita(IspitResponseDTO ispit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda prijave");
        confirm.setHeaderText("Prijavljujete ispit: " + ispit.getPredmet().getNaziv());
        confirm.setContentText("Da li ste sigurni da želite da prijavite ovaj ispit?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.prijaviIspit(this.currentStudentId, ispit.getId()).subscribe(success -> {
                    Platform.runLater(() -> {
                        showSuccess("Uspešno ste prijavili ispit iz predmeta: " + ispit.getPredmet().getNaziv());
                        // Osveži listu (ispit bi trebalo da nestane ili da se označi)
                        loadDostupniIspiti(this.currentStudentId);
                        // Osveži finansije jer prijava možda košta
                        loadFinansije(this.currentIndeksId);
                    });
                }, error -> showError("Greška pri prijavi", "Neuspešna prijava: " + error.getMessage()));
            }
        });
    }

    @FXML
    public void onUpisGodine() {
        if (this.currentIndeksId == null) return;
        int maxGodina = 0;
        if (tokStudijaTable.getItems() != null) {
            maxGodina = tokStudijaTable.getItems().stream().mapToInt(UpisanaGodinaResponseDTO::getGodinaKojaSeUpisuje).max().orElse(0);
        }
        final int godinaZaUpis = maxGodina + 1;
        if (godinaZaUpis > 4) {
            showError("Kraj studija", "Student je već završio 4. godinu.\nNije moguć dalji upis, koristite opciju 'Obnova Godine'.");
            return;
        }

        // --- KEŠIRANJE ŠKOLSKIH GODINA ---
        if (clientCache.hasSkolskeGodine()) {
            prikaziDialogZaUpis(clientCache.getSkolskeGodine(), godinaZaUpis);
        } else {
            apiClient.getAllSkolskeGodine().collectList().subscribe(godine -> {
                clientCache.setSkolskeGodine(godine); // Sačuvaj u keš
                Platform.runLater(() -> prikaziDialogZaUpis(godine, godinaZaUpis));
            }, error -> showError("Greška", "Neuspešno učitavanje školskih godina: " + error.getMessage()));
        }
    }

    private void prikaziDialogZaUpis(java.util.List<SkolskaGodinaResponseDTO> godine, int godinaZaUpis) {
        java.util.Set<String> zauzeteSkolskeGodine = new java.util.HashSet<>();
        if (tokStudijaTable.getItems() != null) {
            tokStudijaTable.getItems().forEach(red -> zauzeteSkolskeGodine.add(red.getSkolskaGodina()));
        }
        SkolskaGodinaResponseDTO predlozenaGodina = godine.stream()
                .filter(SkolskaGodinaResponseDTO::isAktivna)
                .filter(sg -> !zauzeteSkolskeGodine.contains(sg.getNaziv()))
                .findFirst().orElse(null);

        if (predlozenaGodina == null) {
            showError("Nema dostupnih godina", "Nema narednih aktivnih školskih godina.");
            return;
        }

        ChoiceDialog<SkolskaGodinaResponseDTO> dialog = new ChoiceDialog<>(predlozenaGodina, java.util.List.of(predlozenaGodina));
        dialog.setTitle("Upis Godine");
        dialog.setHeaderText("Upis u " + godinaZaUpis + ". godinu studija.");
        dialog.setContentText("Školska godina:");
        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK ? dialog.getSelectedItem() : null);

        dialog.showAndWait().ifPresent(selectedGodina -> {
            UpisGodineRequestDTO request = new UpisGodineRequestDTO();
            request.setStudentIndeksId(this.currentIndeksId);
            request.setSkolskaGodinaId(selectedGodina.getId());
            request.setGodinaKojaSeUpisuje(godinaZaUpis);
            request.setPredmetiKojeUpisujeIds(new ArrayList<>());

            apiClient.upisGodine(request).subscribe(success -> {
                Platform.runLater(() -> {
                    showSuccess("Uspešno upisana " + godinaZaUpis + ". godina!");
                    loadTokStudija(this.currentIndeksId);
                });
            }, error -> showError("Greška pri upisu", error.getMessage()));
        });
    }

    @FXML
    public void onObnovaGodine() {
        if (this.currentIndeksId == null) return;
        int godinaZaObnovu = 1;
        if (tokStudijaTable.getItems() != null && !tokStudijaTable.getItems().isEmpty()) {
            godinaZaObnovu = tokStudijaTable.getItems().stream().mapToInt(UpisanaGodinaResponseDTO::getGodinaKojaSeUpisuje).max().orElse(1);
        }
        final int finalGodinaZaObnovu = godinaZaObnovu;

        // --- KEŠIRANJE ŠKOLSKIH GODINA ---
        if (clientCache.hasSkolskeGodine()) {
            prikaziDialogZaObnovu(clientCache.getSkolskeGodine(), finalGodinaZaObnovu);
        } else {
            apiClient.getAllSkolskeGodine().collectList().subscribe(godine -> {
                clientCache.setSkolskeGodine(godine);
                Platform.runLater(() -> prikaziDialogZaObnovu(godine, finalGodinaZaObnovu));
            }, error -> showError("Greška", "Neuspešno učitavanje školskih godina: " + error.getMessage()));
        }
    }

    private void prikaziDialogZaObnovu(java.util.List<SkolskaGodinaResponseDTO> godine, int godinaZaObnovu) {
        java.util.Set<String> iskorisceneGodine = new java.util.HashSet<>();
        if (tokStudijaTable.getItems() != null) {
            tokStudijaTable.getItems().forEach(red -> iskorisceneGodine.add(red.getSkolskaGodina()));
        }
        SkolskaGodinaResponseDTO sledecaSlobodnaGodina = godine.stream()
                .filter(SkolskaGodinaResponseDTO::isAktivna)
                .filter(sg -> !iskorisceneGodine.contains(sg.getNaziv()))
                .findFirst().orElse(null);

        if (sledecaSlobodnaGodina == null) {
            showError("Nema dostupnih godina", "Nema narednih aktivnih školskih godina za obnovu.");
            return;
        }

        ChoiceDialog<SkolskaGodinaResponseDTO> dialog = new ChoiceDialog<>(sledecaSlobodnaGodina, java.util.List.of(sledecaSlobodnaGodina));
        dialog.setTitle("Obnova Godine");
        dialog.setHeaderText("Obnova " + godinaZaObnovu + ". godine studija.");
        dialog.setContentText("Školska godina:");
        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK ? dialog.getSelectedItem() : null);

        dialog.showAndWait().ifPresent(selectedGodina -> {
            ObnovaGodineRequestDTO request = new ObnovaGodineRequestDTO();
            request.setStudentIndeksId(this.currentIndeksId);
            request.setSkolskaGodinaId(selectedGodina.getId());
            request.setGodinaKojaSeObnavlja(godinaZaObnovu);
            request.setPredmetiIzNaredneGodineIds(new ArrayList<>());

            apiClient.obnovaGodine(request).subscribe(success -> {
                Platform.runLater(() -> {
                    showSuccess("Uspešno obnovljena " + godinaZaObnovu + ". godina!");
                    loadTokStudija(this.currentIndeksId);
                });
            }, error -> showError("Greška pri obnovi", error.getMessage()));
        });
    }

    @FXML
    public void onNovaUplata() {
        if (this.currentIndeksId == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nova Uplata");
        dialog.setHeaderText("Unos uplate školarine");
        dialog.setContentText("Unesite iznos (RSD):");

        dialog.showAndWait().ifPresent(iznosStr -> {
            try {
                BigDecimal iznos = new BigDecimal(iznosStr);
                // --- VALIDACIJA ---
                if (iznos.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Neispravan unos", "Iznos uplate mora biti veći od nule!");
                    return;
                }
                CreateUplataRequestDTO request = new CreateUplataRequestDTO();
                request.setStudentIndeksId(this.currentIndeksId);
                request.setIznos(iznos);
                request.setDatumUplate(LocalDate.now());
                request.setSvrha("Uplata školarine (Klijent App)");

                apiClient.addUplata(request).subscribe(response -> {
                    Platform.runLater(() -> {
                        showSuccess("Uplata uspešno evidentirana!");
                        loadFinansije(this.currentIndeksId);
                    });
                }, error -> showError("Greška", "Neuspešna uplata: " + error.getMessage()));
            } catch (NumberFormatException e) {
                showError("Greška", "Morate uneti validan broj!");
            }
        });
    }

    @FXML
    public void onStampajUverenje() {
        try {
            reportService.generateUverenje(lblImePrezime.getText(), lblIndeks.getText(), "Osnovne akademske studije");
        } catch (Exception e) { showError("Greška pri štampi", e.getMessage()); }
    }

    @FXML
    public void onStampajPolozeno() {
        try {
            var listaIspita = new java.util.ArrayList<>(ispitiTable.getItems());
            if (listaIspita.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Student nema položenih ispita za štampu.");
                alert.showAndWait();
                return;
            }
            for (PolozenIspitResponseDTO ispit : listaIspita) {
                if (ispit.getGodinaStudija() == null) ispit.setGodinaStudija(1);
            }
            listaIspita.sort((i1, i2) -> Integer.compare(i1.getGodinaStudija(), i2.getGodinaStudija()));
            reportService.generateUverenjePolozeni(lblImePrezime.getText(), lblIndeks.getText(), listaIspita);
        } catch (Exception e) { showError("Greška pri štampi", e.getMessage()); }
    }

    @FXML
    public void onBack() {
        navigationManager.goBack();
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Uspeh");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}