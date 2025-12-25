package org.raflab.studsluzba.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Pokrećemo Spring kontekst
        context = new SpringApplicationBuilder(StudsluzbaClientApp.class)
                .headless(false) // <--- OVO JE KLJUČNO! DOZVOLJAVA OTVARANJE IZVEŠTAJA
                .run();
    }

    @Override
    public void start(Stage stage) {
        // Obaveštavamo Spring da je Stage (prozor) spreman
        context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        // Gašenje Spring konteksta kad se zatvori prozor
        context.close();
        Platform.exit();
    }

    // Event koji nosi Stage objekat
    public static class StageReadyEvent extends ApplicationEvent {
        public Stage getStage() {
            return (Stage) getSource();
        }

        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}