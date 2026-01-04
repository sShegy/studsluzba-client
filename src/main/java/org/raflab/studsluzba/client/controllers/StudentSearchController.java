package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.dto.sifarnik.response.SrednjaSkolaResponseDTO;
import org.raflab.studsluzba.dto.student.response.StudentProfileResponseDTO;
import org.raflab.studsluzba.client.service.ApiClient;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

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

    private final ApiClient apiClient;
    private final NavigationManager navigationManager;

    public StudentSearchController(ApiClient apiClient, NavigationManager navigationManager) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {
        colIndeks.setCellValueFactory(new PropertyValueFactory<>("aktivniIndeks"));
        colIme.setCellValueFactory(new PropertyValueFactory<>("ime"));
        colPrezime.setCellValueFactory(new PropertyValueFactory<>("prezime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("studijskiProgram"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        studentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StudentProfileResponseDTO selected = studentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openProfile(selected.getId());
                }
            }
        });

        skolaCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SrednjaSkolaResponseDTO skola) {
                return skola != null ? skola.getNaziv() : "";
            }

            @Override
            public SrednjaSkolaResponseDTO fromString(String string) {
                return null;
            }
        });

        skolaCombo.setVisibleRowCount(15);

        loadSkole();
        onSearch();
    }

    private void loadSkole() {
        apiClient.getAllSrednjeSkole()
                .collectList()
                .subscribe(skole -> {
                    Platform.runLater(() -> {
                        // SADA JE OVO ČISTO:
                        // Samo uzimamo listu koju je server poslao (sa pravim imenima)
                        // i ubacujemo je u ComboBox.
                        skolaCombo.getItems().setAll(skole);
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pri učitavanju škola: " + error.getMessage())));
    }

    @FXML
    public void onSearch() {
        String trazeniIndeks = indeksInput.getText().trim();
        String ime = imeInput.getText();
        String prezime = prezimeInput.getText();
        SrednjaSkolaResponseDTO izabranaSkola = skolaCombo.getValue();

        Flux<StudentProfileResponseDTO> searchFlux;

        if (izabranaSkola != null) {
            searchFlux = apiClient.getStudentiBySrednjaSkola(izabranaSkola.getId());
        } else {
            // Šaljemo parametre serveru
            searchFlux = apiClient.searchStudents(trazeniIndeks, ime, prezime);
        }

        searchFlux.collectList()
                .subscribe(studenti -> {
                    Platform.runLater(() -> {
                        // Filtriranje na klijentu za indeks (ako server ne podržava filtriranje po indeksu)
                        var filtriranaLista = studenti;

                        if (!trazeniIndeks.isEmpty()) {
                            filtriranaLista = studenti.stream()
                                    .filter(s -> s.getAktivniIndeks() != null &&
                                            s.getAktivniIndeks().contains(trazeniIndeks))
                                    .toList();
                        }
                        studentsTable.getItems().setAll(filtriranaLista);
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška pretraga: " + error.getMessage())));
    }

    @FXML
    public void onReset() {
        indeksInput.clear();
        imeInput.clear();
        prezimeInput.clear();
        skolaCombo.getSelectionModel().clearSelection();
        onSearch();
    }

    private void openProfile(Long studentId) {
        navigationManager.navigateTo("/fxml/StudentProfile.fxml", controller -> {
            if (controller instanceof StudentProfileController) {
                ((StudentProfileController) controller).loadStudentData(studentId);
            }
        });
    }
}