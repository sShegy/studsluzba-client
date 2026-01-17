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
    private final ClientCache clientCache;

    public StudentSearchController(ApiClient apiClient, NavigationManager navigationManager, ClientCache clientCache) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
        this.clientCache = clientCache;
    }

    @FXML
    public void initialize() {
        colIndeks.setCellValueFactory(new PropertyValueFactory<>("aktivniIndeks"));
        colIme.setCellValueFactory(new PropertyValueFactory<>("ime"));
        colPrezime.setCellValueFactory(new PropertyValueFactory<>("prezime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("studijskiProgram"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Validacija unosa
        indeksInput.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[0-9/]*")) {
                return change;
            }
            return null;
        }));

        imeInput.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[a-zA-ZšđčćžŠĐČĆŽ\\s\\-]*")) {
                return change;
            }
            return null;
        }));

        prezimeInput.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[a-zA-ZšđčćžŠĐČĆŽ\\s\\-]*")) {
                return change;
            }
            return null;
        }));

        studentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StudentProfileResponseDTO selected = studentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) openProfile(selected.getId());
            }
        });

        skolaCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SrednjaSkolaResponseDTO skola) {
                return skola != null ? skola.getNaziv() : null;
            }

            @Override
            public SrednjaSkolaResponseDTO fromString(String string) {
                return null;
            }
        });


        skolaCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SrednjaSkolaResponseDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Izaberi srednju školu"); // <-- FORSIRAMO TEKST
                } else {
                    setText(item.getNaziv());
                }
            }
        });

        skolaCombo.setVisibleRowCount(15);

        loadSkole();
        onSearch();
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
        String trazeniIndeks = (indeksInput.getText() != null) ? indeksInput.getText().trim() : "";
        String ime = (imeInput.getText() != null) ? imeInput.getText().trim() : "";
        String prezime = (prezimeInput.getText() != null) ? prezimeInput.getText().trim() : "";
        SrednjaSkolaResponseDTO izabranaSkola = skolaCombo.getValue();

        Flux<StudentProfileResponseDTO> searchFlux;

        if (izabranaSkola != null) {
            searchFlux = apiClient.getStudentiBySrednjaSkola(izabranaSkola.getId());
        } else {
            searchFlux = apiClient.searchStudents(trazeniIndeks, ime, prezime);
        }

        searchFlux.collectList().subscribe(studenti -> {
            Platform.runLater(() -> {
                var filtriranaLista = studenti;
                if (!trazeniIndeks.isEmpty() && izabranaSkola != null) {
                    filtriranaLista = studenti.stream()
                            .filter(s -> s.getAktivniIndeks() != null && s.getAktivniIndeks().contains(trazeniIndeks))
                            .toList();
                }
                studentsTable.getItems().setAll(filtriranaLista);
            });
        }, error -> Platform.runLater(() -> {
            System.err.println("Greška pretraga: " + error.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Greška: " + error.getMessage());
            alert.show();
        }));
    }

    @FXML
    public void onReset() {
        indeksInput.clear();
        imeInput.clear();
        prezimeInput.clear();

        // Resetovanje ComboBox-a
        skolaCombo.setValue(null);
        skolaCombo.getSelectionModel().clearSelection();

        onSearch();
    }

    public void restoreState(String indeks, String ime, String prezime, SrednjaSkolaResponseDTO skola) {
        this.indeksInput.setText(indeks);
        this.imeInput.setText(ime);
        this.prezimeInput.setText(prezime);
        this.skolaCombo.setValue(skola);

        onSearch();
    }

    private void openProfile(Long studentId) {
        final String savedIndeks = indeksInput.getText();
        final String savedIme = imeInput.getText();
        final String savedPrezime = prezimeInput.getText();
        final SrednjaSkolaResponseDTO savedSkola = skolaCombo.getValue();

        navigationManager.updateCurrentStateSetup(controller -> {
            if (controller instanceof StudentSearchController) {
                ((StudentSearchController) controller).restoreState(savedIndeks, savedIme, savedPrezime, savedSkola);
            }
        });

        navigationManager.navigateTo("/fxml/StudentProfile.fxml", controller -> {
            if (controller instanceof StudentProfileController) {
                ((StudentProfileController) controller).loadStudentData(studentId);
            }
        });
    }
}