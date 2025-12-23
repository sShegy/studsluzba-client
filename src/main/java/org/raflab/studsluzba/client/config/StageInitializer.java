package org.raflab.studsluzba.client.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.raflab.studsluzba.client.JavaFxApplication;
import org.raflab.studsluzba.client.core.NavigationManager;
import org.raflab.studsluzba.client.utils.SpringFXMLLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<JavaFxApplication.StageReadyEvent> {

    private final SpringFXMLLoader springFXMLLoader;
    private final NavigationManager navigationManager;

    public StageInitializer(SpringFXMLLoader springFXMLLoader, NavigationManager navigationManager) {
        this.springFXMLLoader = springFXMLLoader;
        this.navigationManager = navigationManager;
    }

    @Override
    public void onApplicationEvent(JavaFxApplication.StageReadyEvent event) {
        try {
            Stage stage = event.getStage();

            FXMLLoader loader = springFXMLLoader.load("/fxml/MainLayout.fxml");
            Parent root = loader.load();

            if (root instanceof BorderPane) {
                Scene scene = new Scene(root, 1280, 800); // Malo veća rezolucija

                navigationManager.initialize(scene, (BorderPane) root);

                navigationManager.navigateTo("/fxml/Welcome.fxml");

                stage.setScene(scene);
                stage.setTitle("Studentska Služba - Klijent");
                stage.show();
            } else {
                throw new IllegalStateException("MainLayout.fxml mora imati BorderPane kao koren!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri pokretanju aplikacije", e);
        }
    }
}