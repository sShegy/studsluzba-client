package org.raflab.studsluzba.client.controllers;

import javafx.fxml.FXML;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    private final NavigationManager navigationManager;

    public MainController(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
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
    public void onPredmetiClick() {
        System.out.println("Kliknuto na Predmeti");
    }

    @FXML
    public void onIspitiClick() {
        System.out.println("Kliknuto na Ispiti");
    }
}