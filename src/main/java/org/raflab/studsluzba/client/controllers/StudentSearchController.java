package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.raflab.studsluzba.client.core.ClientCache;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.sifarnik.response.SrednjaSkolaResponseDTO;
import org.raflab.studsluzba.dto.student.response.StudentProfileResponseDTO;
import org.springframework.stereotype.Controller;

@Controller
public class StudentSearchController {

    @FXML private TextField indeksInput;
    @FXML private TextField imeInput;
    @FXML private TextField prezimeInput;
    @FXML private ComboBox<SrednjaSkolaResponseDTO> skolaCombo;

    @FXML private TableView<StudentProfileResponseDTO> studentsTable;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colIndeks;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colIme;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colPrezime;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colProgram;
    @FXML private TableColumn<StudentProfileResponseDTO, String> colEmail;

    // --- PAGINACIJA KONTROLE ---
    @FXML private Label lblStranica;
    @FXML private Button btnPrethodna;
    @FXML private Button btnSledeca;

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private final ClientCache clientCache;

    // Stanje paginacije
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalPages = 0;

    public StudentSearchController(ApiClient apiClient, NavigationManager navigationManager, ClientCache clientCache) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
        this.clientCache = clientCache;
    }

    @FXML
    public void initialize() {
        // Konfiguracija kolona
        colIndeks.setCellValueFactory(new PropertyValueFactory<>("aktivniIndeks"));
        colIme.setCellValueFactory(new PropertyValueFactory<>("ime"));
        colPrezime.setCellValueFactory(new PropertyValueFactory<>("prezime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("studijskiProgram"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Dvoklik za otvaranje profila
        studentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StudentProfileResponseDTO selected = studentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) openProfile(selected.getId());
            }
        });

        // Konfiguracija ComboBox-a
        skolaCombo.setConverter(new StringConverter<>() {
            @Override public String toString(SrednjaSkolaResponseDTO skola) { return skola != null ? skola.getNaziv() : ""; }
            @Override public SrednjaSkolaResponseDTO fromString(String string) { return null; }
        });
        skolaCombo.setVisibleRowCount(15);

        loadSkole();

        // Inicijalna pretraga (ako nije restaurirano stanje)
        if (studentsTable.getItems().isEmpty()) {
            onSearch();
        }
    }

    private void loadSkole() {
        if (clientCache.hasSrednjeSkole()) {
            Platform.runLater(() -> skolaCombo.getItems().setAll(clientCache.getSrednjeSkole()));
            return;
        }

        apiClient.getAllSrednjeSkole().collectList().subscribe(skole -> {
            clientCache.setSrednjeSkole(skole);
            Platform.runLater(() -> skolaCombo.getItems().setAll(skole));
        }, error -> Platform.runLater(() -> System.err.println("Greška pri učitavanju škola: " + error.getMessage())));
    }

    @FXML
    public void onSearch() {
        currentPage = 0;
        loadPage();
    }

    private void loadPage() {
        String trazeniIndeks = indeksInput.getText().trim();
        String ime = imeInput.getText();
        String prezime = prezimeInput.getText();

        if (btnPrethodna != null) btnPrethodna.setDisable(true);
        if (btnSledeca != null) btnSledeca.setDisable(true);
        if (lblStranica != null) lblStranica.setText("Svi rezultati");

        apiClient.searchStudents(trazeniIndeks, ime, prezime)
                .collectList()
                .subscribe(studenti -> {
                    Platform.runLater(() -> {
                        studentsTable.getItems().setAll(studenti);
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pretraga: " + error.getMessage())));
    }

    private void updatePaginationControls() {
        if (lblStranica != null) {
            lblStranica.setText("Strana " + (currentPage + 1) + " od " + (totalPages == 0 ? 1 : totalPages));
        }
        if (btnPrethodna != null) btnPrethodna.setDisable(currentPage == 0);
        if (btnSledeca != null) btnSledeca.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    public void onPrethodna() {
        if (currentPage > 0) {
            currentPage--;
            loadPage();
        }
    }

    @FXML
    public void onSledeca() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadPage();
        }
    }

    @FXML
    public void onReset() {
        indeksInput.clear();
        imeInput.clear();
        prezimeInput.clear();
        skolaCombo.getSelectionModel().clearSelection();
        onSearch();
    }

    // --- BACKTRACK LOGIKA ---

    /**
     * Ovu metodu poziva NavigationManager kada se vratimo na ovaj ekran (Back dugme).
     * Ona vraća sve vrednosti u polja i učitava tačnu stranicu na kojoj smo bili.
     */
    public void restoreState(String indeks, String ime, String prezime, SrednjaSkolaResponseDTO skola, int page) {
        this.indeksInput.setText(indeks);
        this.imeInput.setText(ime);
        this.prezimeInput.setText(prezime);
        this.skolaCombo.setValue(skola);
        this.currentPage = page;

        loadPage(); // Učitaj podatke za zapamćenu stranicu
    }

    private void openProfile(Long studentId) {
        // 1. ČUVANJE STANJA
        // Pre nego što odemo, kažemo NavigationManager-u: "Zapamti ovo stanje!"
        final String savedIndeks = indeksInput.getText();
        final String savedIme = imeInput.getText();
        final String savedPrezime = prezimeInput.getText();
        final SrednjaSkolaResponseDTO savedSkola = skolaCombo.getValue();
        final int savedPage = currentPage;

        navigationManager.updateCurrentStateSetup(controller -> {
            if (controller instanceof StudentSearchController) {
                ((StudentSearchController) controller).restoreState(savedIndeks, savedIme, savedPrezime, savedSkola, savedPage);
            }
        });

        // 2. NAVIGACIJA
        navigationManager.navigateTo("/fxml/StudentProfile.fxml", controller -> {
            if (controller instanceof StudentProfileController) {
                ((StudentProfileController) controller).loadStudentData(studentId);
            }
        });
    }
}