package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.dto.PredmetDTO;
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
import java.util.List;
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
                    showError("Upozorenje", "Student nema aktivan indeks ID!");
                }
            });
        }, error -> showError("Greška", "Greška pri učitavanju studenta: " + error.getMessage()));
    }

    private void loadIspiti(Long indeksId) {
        apiClient.getPolozeniIspiti(indeksId)
                .collectList()
                .subscribe(ispiti -> {
                    Platform.runLater(() -> {
                        if (ispitiTable != null) {
                            ispitiTable.getItems().setAll(ispiti);

                            // --- FIX: Filtriramo samo ispite koji imaju unetu ocenu i ESPB ---
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
                }, error -> showError("Greška", "Greška pri učitavanju ispita: " + error.getMessage()));
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
                }, error -> showError("Greška", "Greška pri učitavanju nepoloženih ispita: " + error.getMessage()));
    }

    private void loadTokStudija(Long indeksId) {
        // 1. Prvo dohvatamo UPISANE godine
        apiClient.getUpisaneGodine(indeksId).collectList().subscribe(upisi -> {

            // 2. Odmah zatim dohvatamo OBNOVLJENE godine
            apiClient.getObnovljeneGodine(indeksId).collectList().subscribe(obnove -> {

                Platform.runLater(() -> {
                    // Pravimo novu listu
                    java.util.List<UpisanaGodinaResponseDTO> sveGodine = new java.util.ArrayList<>();

                    // Ubacujemo upise i ručno postavljamo TIP
                    for (UpisanaGodinaResponseDTO u : upisi) {
                        u.setTip("Upis"); // <--- OVO REŠAVA PRAZNU KOLONU
                        sveGodine.add(u);
                    }

                    // Ubacujemo obnove i ručno postavljamo TIP
                    for (UpisanaGodinaResponseDTO o : obnove) {
                        o.setTip("Obnova"); // <--- OVO REŠAVA PRAZNU KOLONU
                        sveGodine.add(o);
                    }

                    // Sortiramo (najnovije gore)
                    sveGodine.sort((g1, g2) -> {
                        if (g1.getDatumUpisa() == null || g2.getDatumUpisa() == null) return 0;
                        return g2.getDatumUpisa().compareTo(g1.getDatumUpisa());
                    });

                    // --- NASILNO AŽURIRANJE TABELE ---
                    tokStudijaTable.getItems().clear(); // 1. Obriši sve staro
                    tokStudijaTable.getItems().addAll(sveGodine); // 2. Ubaci novo
                    tokStudijaTable.refresh(); // 3. Osveži prikaz
                });

            }, error -> showError("Greška", "Neuspešno učitavanje obnova: " + error.getMessage()));

        }, error -> showError("Greška", "Neuspešno učitavanje upisa: " + error.getMessage()));
    }

    private void loadFinansije(Long indeksId) {
        apiClient.getStanjeFinansija(indeksId)
                .subscribe(stanje -> {
                    Platform.runLater(() -> {
                        lblZaduzenje.setText(stanje.getUkupnoZaduzenje() + " RSD");
                        lblUplaceno.setText(stanje.getUkupnoUplaceno() + " RSD");
                        lblStanje.setText(stanje.getPreostaloZaUplatu() + " RSD");
                    });
                }, error -> showError("Greška", "Greška pri učitavanju finansija: " + error.getMessage()));
    }
    @FXML
    public void onUpisGodine() {
        if (this.currentIndeksId == null) return;

        // 1. Određujemo NAREDNU godinu studija (Max iz tabele + 1)
        int maxGodina = 0;
        if (tokStudijaTable.getItems() != null) {
            maxGodina = tokStudijaTable.getItems().stream()
                    .mapToInt(UpisanaGodinaResponseDTO::getGodinaKojaSeUpisuje)
                    .max()
                    .orElse(0);
        }

        final int godinaZaUpis = maxGodina + 1;

        // PRAVILO: Osnovne studije traju 4 godine
        if (godinaZaUpis > 4) {
            showError("Kraj studija", "Student je već završio 4. godinu.\nNije moguć dalji upis, koristite opciju 'Obnova Godine'.");
            return;
        }

        // 2. Dohvatamo sve školske godine sa servera
        apiClient.getAllSkolskeGodine().collectList().subscribe(godine -> {
            Platform.runLater(() -> {

                // 3. Sakupljamo nazive godina koje je student VEĆ upisao/obnovio
                java.util.Set<String> zauzeteSkolskeGodine = new java.util.HashSet<>();
                if (tokStudijaTable.getItems() != null) {
                    tokStudijaTable.getItems().forEach(red -> zauzeteSkolskeGodine.add(red.getSkolskaGodina()));
                }

                // 4. Tražimo prvu godinu koja je AKTIVNA i NIJE ZAUZETA
                SkolskaGodinaResponseDTO predlozenaGodina = godine.stream()
                        .filter(SkolskaGodinaResponseDTO::isAktivna) // Mora biti aktivna u bazi
                        .filter(sg -> !zauzeteSkolskeGodine.contains(sg.getNaziv())) // Ne sme biti već u tabeli
                        .findFirst()
                        .orElse(null);

                if (predlozenaGodina == null) {
                    showError("Nema dostupnih godina", "Nema narednih aktivnih školskih godina u koje student može da se upiše.");
                    return;
                }

                // 5. Kreiramo dijalog sa tom jednom opcijom
                ChoiceDialog<SkolskaGodinaResponseDTO> dialog = new ChoiceDialog<>(predlozenaGodina, java.util.List.of(predlozenaGodina));
                dialog.setTitle("Upis Godine");
                dialog.setHeaderText("Upis u " + godinaZaUpis + ". godinu studija.");
                dialog.setContentText("Školska godina:");

                // Lep ispis objekta u meniju
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) return dialog.getSelectedItem();
                    return null;
                });

                Optional<SkolskaGodinaResponseDTO> result = dialog.showAndWait();

                // 6. Slanje zahteva na server
                result.ifPresent(selectedGodina -> {
                    UpisGodineRequestDTO request = new UpisGodineRequestDTO();
                    request.setStudentIndeksId(this.currentIndeksId);
                    request.setSkolskaGodinaId(selectedGodina.getId());
                    request.setGodinaKojaSeUpisuje(godinaZaUpis);
                    request.setPredmetiKojeUpisujeIds(new ArrayList<>()); // Server dodaje obavezne predmete

                    apiClient.upisGodine(request).subscribe(
                            success -> {
                                Platform.runLater(() -> {
                                    showSuccess("Uspešno upisana " + godinaZaUpis + ". godina (" + selectedGodina.getNaziv() + ")!");
                                    // KLJUČNO: Odmah osvežavamo tabelu da bi se video novi red
                                    loadTokStudija(this.currentIndeksId);
                                });
                            },
                            error -> showError("Greška pri upisu", error.getMessage())
                    );
                });
            });
        }, error -> showError("Greška", "Neuspešno učitavanje školskih godina: " + error.getMessage()));
    }
    @FXML
    public void onObnovaGodine() {
        if (this.currentIndeksId == null) return;

        int godinaZaObnovu = 1;
        if (tokStudijaTable.getItems() != null && !tokStudijaTable.getItems().isEmpty()) {
            godinaZaObnovu = tokStudijaTable.getItems().stream()
                    .mapToInt(UpisanaGodinaResponseDTO::getGodinaKojaSeUpisuje)
                    .max()
                    .orElse(1);
        }
        final int finalGodinaZaObnovu = godinaZaObnovu;

        apiClient.getAllSkolskeGodine().collectList().subscribe(godine -> {
            Platform.runLater(() -> {

                // --- PAMETNA LOGIKA ---
                java.util.Set<String> iskorisceneGodine = new java.util.HashSet<>();
                if (tokStudijaTable.getItems() != null) {
                    tokStudijaTable.getItems().forEach(red -> iskorisceneGodine.add(red.getSkolskaGodina()));
                }

                SkolskaGodinaResponseDTO sledecaSlobodnaGodina = godine.stream()
                        .filter(SkolskaGodinaResponseDTO::isAktivna)
                        .filter(sg -> !iskorisceneGodine.contains(sg.getNaziv()))
                        .findFirst()
                        .orElse(null);

                if (sledecaSlobodnaGodina == null) {
                    showError("Nema dostupnih godina", "Nema narednih aktivnih školskih godina za obnovu.");
                    return;
                }

                ChoiceDialog<SkolskaGodinaResponseDTO> dialog = new ChoiceDialog<>(sledecaSlobodnaGodina, java.util.List.of(sledecaSlobodnaGodina));
                dialog.setTitle("Obnova Godine");
                dialog.setHeaderText("Obnova " + finalGodinaZaObnovu + ". godine studija.");
                dialog.setContentText("Školska godina:");

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        return dialog.getSelectedItem();
                    }
                    return null;
                });

                Optional<SkolskaGodinaResponseDTO> result = dialog.showAndWait();

                result.ifPresent(selectedGodina -> {
                    ObnovaGodineRequestDTO request = new ObnovaGodineRequestDTO();
                    request.setStudentIndeksId(this.currentIndeksId);
                    request.setSkolskaGodinaId(selectedGodina.getId());
                    request.setGodinaKojaSeObnavlja(finalGodinaZaObnovu);
                    request.setPredmetiIzNaredneGodineIds(new ArrayList<>());

                    apiClient.obnovaGodine(request).subscribe(
                            success -> {
                                Platform.runLater(() -> {
                                    showSuccess("Uspešno obnovljena " + finalGodinaZaObnovu + ". godina (" + selectedGodina.getNaziv() + ")!");
                                    loadTokStudija(this.currentIndeksId);
                                });
                            },
                            error -> showError("Greška pri obnovi", error.getMessage())
                    );
                });
            });
        }, error -> showError("Greška", "Neuspešno učitavanje školskih godina: " + error.getMessage()));
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
            String ime = lblImePrezime.getText();
            String indeks = lblIndeks.getText();
            reportService.generateUverenje(ime, indeks, "Osnovne akademske studije");
        } catch (Exception e) {
            showError("Greška pri štampi", e.getMessage());
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
            showError("Greška pri štampi", e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        navigationManager.goBack();
    }

    // --- POMOĆNE METODE ZA ALERTE ---
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