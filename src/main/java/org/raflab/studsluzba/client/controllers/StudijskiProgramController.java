package org.raflab.studsluzba.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.raflab.studsluzba.client.core.ClientCache;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.service.ApiClient;
import org.raflab.studsluzba.dto.kurikulum.response.StudijskiProgramResponseDTO;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class StudijskiProgramController {

    @FXML private Label lblStudijski;
    @FXML private TableView<StudijskiProgramResponseDTO> tabelaStudijskiProgram;
    @FXML private TableColumn<StudijskiProgramResponseDTO, String> colOznaka;
    @FXML private TableColumn<StudijskiProgramResponseDTO, String> colNaziv;
    @FXML private TableColumn<StudijskiProgramResponseDTO, Integer> colGodinaAkreditacije;
    @FXML private TableColumn<StudijskiProgramResponseDTO, String> colNazivZvanja;
    @FXML private TableColumn<StudijskiProgramResponseDTO, Integer> colTrajanjeSemestra;


    private final ApiClient apiClient;
    private final NavigationManager navigationManager;
    private final ClientCache clientCache;

    public StudijskiProgramController(ApiClient apiClient, NavigationManager navigationManager, ClientCache clientCache) {
        this.apiClient = apiClient;
        this.navigationManager = navigationManager;
        this.clientCache = clientCache;
    }

    @FXML private void initialize() {

        colOznaka.setCellValueFactory(new PropertyValueFactory<>("oznaka"));
        colNaziv.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        colGodinaAkreditacije.setCellValueFactory(new PropertyValueFactory<>("godinaAkreditacije"));
        colNazivZvanja.setCellValueFactory(new PropertyValueFactory<>("nazivZvanja"));
        colTrajanjeSemestra.setCellValueFactory(new PropertyValueFactory<>("trajanjeSemestra"));

        tabelaStudijskiProgram.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StudijskiProgramResponseDTO selected = tabelaStudijskiProgram.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openDetails(selected);
                }
            }
        });

        loadStudijskiProgram();
    }
    private void openDetails(StudijskiProgramResponseDTO program) {
        navigationManager.navigateTo("/fxml/StudijskiProgramDetails.fxml", controller -> {
            if (controller instanceof StudijskiProgramDetailsController) {
                ((StudijskiProgramDetailsController) controller).setStudijskiProgram(program);
            }
        });
    }
    private void loadStudijskiProgram() {

        if (clientCache.hasStudijskiProgrami()) {
            List<StudijskiProgramResponseDTO> programiIzKesa = clientCache.getStudijskiProgrami();
            tabelaStudijskiProgram.setItems(FXCollections.observableArrayList(programiIzKesa));

            return;
        }

        apiClient.getAllStudijskiProgrami().collectList().subscribe(programi -> {
            clientCache.setStudijskiProgrami(programi);
            System.out.println("Studijski programi uspešno keširani.");

            Platform.runLater(() -> {
                tabelaStudijskiProgram.setItems(FXCollections.observableArrayList(programi));
                System.out.println("Tabela je uspešno osvežena sa " + programi.size() + " programa.");
            });
        }, e -> {
            System.err.println("Greška pri inicijalizaciji keša: " + e.getMessage());
        });
    }
}
