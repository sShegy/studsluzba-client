package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.dto.kurikulum.response.PredmetDetaljiResponseDTO;
import org.raflab.studsluzba.client.dto.kurikulum.response.StudijskiProgramResponseDTO;
import org.raflab.studsluzba.client.service.ApiClient;
import org.springframework.stereotype.Controller;

@Controller
public class SubjectsController {

    @FXML private ListView<StudijskiProgramResponseDTO> programiList;

    @FXML private TableView<PredmetDetaljiResponseDTO> predmetiTable;
    @FXML private TableColumn<PredmetDetaljiResponseDTO, String> colSifra;
    @FXML private TableColumn<PredmetDetaljiResponseDTO, String> colNaziv;
    @FXML private TableColumn<PredmetDetaljiResponseDTO, Integer> colEspb;
    @FXML private TableColumn<PredmetDetaljiResponseDTO, Integer> colSemestar;

    private final ApiClient apiClient;

    public SubjectsController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @FXML
    public void initialize() {
        // Konfiguracija kolona tabele
        colSifra.setCellValueFactory(new PropertyValueFactory<>("sifra"));
        colNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colEspb.setCellValueFactory(new PropertyValueFactory<>("espb"));
        colSemestar.setCellValueFactory(new PropertyValueFactory<>("semestar"));

        // Konfiguracija prikaza u listi (da piše naziv programa, a ne objekat)
        programiList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(StudijskiProgramResponseDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNaziv() + " (" + item.getOznaka() + ")");
                }
            }
        });

        // Listener za klik na program
        programiList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPredmeti(newVal.getId());
            }
        });

        loadProgrami();
    }

    private void loadProgrami() {
        apiClient.getStudijskiProgrami()
                .collectList()
                .subscribe(programi -> {
                    Platform.runLater(() -> {
                        programiList.getItems().setAll(programi);
                        // Selektuj prvi program automatski ako postoji
                        if (!programi.isEmpty()) {
                            programiList.getSelectionModel().selectFirst();
                        }
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška programi: " + error.getMessage())));
    }

    private void loadPredmeti(Long programId) {
        apiClient.getPredmetiByProgram(programId)
                .collectList()
                .subscribe(predmeti -> {
                    Platform.runLater(() -> {
                        predmetiTable.getItems().setAll(predmeti);
                    });
                }, error -> Platform.runLater(() -> System.err.println("Greška predmeti: " + error.getMessage())));
    }
}