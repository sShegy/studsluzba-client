package org.raflab.studsluzba.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    private final NavigationManager navigationManager;

    @FXML private Button btnBack;
    @FXML private Button btnForward;

    public MainController(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }

    @FXML
    public void initialize() {

        if (btnBack != null) {
            btnBack.disableProperty().bind(navigationManager.canGoBackProperty().not());
        }
        if (btnForward != null) {
            btnForward.disableProperty().bind(navigationManager.canGoForwardProperty().not());
        }
    }

    @FXML
    public void handleBack() {
        navigationManager.goBack();
    }

    @FXML
    public void handleForward() {
        navigationManager.goForward();
    }

    @FXML
    public void onPocetnaClick() {
        navigationManager.navigateTo("/fxml/Welcome.fxml");
    }

    @FXML
    public void onStudentiClick() {
        navigationManager.navigateTo("/fxml/StudentSearch.fxml");
    }

    @FXML
    public void onPregledClick() {
        navigationManager.navigateTo("/fxml/Subjects.fxml");
    }

    @FXML
    private void onStudijskiProgramClick(){
        navigationManager.navigateTo("/fxml/StudijskiProgram.fxml");
    }

    @FXML
    public void onIspitiClick() {
        navigationManager.navigateTo("/fxml/Ispiti.fxml");
    }
}