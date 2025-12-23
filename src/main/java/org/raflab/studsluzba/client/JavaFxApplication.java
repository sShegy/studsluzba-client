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
        context = new SpringApplicationBuilder(StudsluzbaClientApp.class).run();
    }

    @Override
    public void start(Stage stage) {
        context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }

    public static class StageReadyEvent extends ApplicationEvent {
        public Stage getStage() {
            return (Stage) getSource();
        }

        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}