package org.raflab.studsluzba.client.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.raflab.studsluzba.client.utils.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Stack;
import java.util.function.Consumer;

@Component
public class NavigationManager {

    private final SpringFXMLLoader springFXMLLoader;
    private BorderPane mainLayout; // Glavni kontejner gde se menjaju ekrani

    // Stack za istoriju (Back i Forward)
    private final Stack<NavigationState> backStack = new Stack<>();
    private final Stack<NavigationState> forwardStack = new Stack<>();

    private NavigationState currentState;

    @Value("${app.history.max-depth:10}")
    private int maxHistoryDepth;

    public NavigationManager(SpringFXMLLoader springFXMLLoader) {
        this.springFXMLLoader = springFXMLLoader;
    }

    public void initialize(Scene scene, BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        setupGlobalEventHandlers(scene);
    }

    public void navigateTo(String fxmlPath) {
        navigateTo(fxmlPath, null);
    }

    public void navigateTo(String fxmlPath, Consumer<Object> controllerSetup) {
        if (currentState != null) {
            backStack.push(currentState);

            // Ograničenje istorije na osnovu konfiguracije
            if (backStack.size() > maxHistoryDepth) {
                backStack.remove(0); // Izbacujemo najstariji element
            }
        }
        forwardStack.clear(); // Nova grana, brišemo forward
        loadView(fxmlPath, controllerSetup);
    }

    public void goBack() {
        if (backStack.isEmpty()) return;

        forwardStack.push(currentState);
        NavigationState previous = backStack.pop();
        loadView(previous.fxmlPath, previous.controllerSetup);
    }

    public void goForward() {
        if (forwardStack.isEmpty()) return;

        backStack.push(currentState);
        NavigationState next = forwardStack.pop();
        loadView(next.fxmlPath, next.controllerSetup);
    }

    private void loadView(String fxmlPath, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = springFXMLLoader.load(fxmlPath);
            Parent view = loader.load();

            if (controllerSetup != null) {
                controllerSetup.accept(loader.getController());
            }

            mainLayout.setCenter(view);

            currentState = new NavigationState(fxmlPath, controllerSetup);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Greška pri učitavanju FXML-a: " + fxmlPath);
        }
    }

    private void setupGlobalEventHandlers(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.BACK) {
                goBack();
                event.consume();
            } else if (event.getButton() == MouseButton.FORWARD) {
                goForward();
                event.consume();
            }
        });

        // Tastatura (Ctrl + [ za nazad, Ctrl + ] za napred)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.OPEN_BRACKET) {
                    goBack();
                    event.consume();
                } else if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                    goForward();
                    event.consume();
                }
            }
        });
    }

    // Pomoćna klasa za čuvanje stanja
    private static class NavigationState {
        String fxmlPath;
        Consumer<Object> controllerSetup;

        public NavigationState(String fxmlPath, Consumer<Object> controllerSetup) {
            this.fxmlPath = fxmlPath;
            this.controllerSetup = controllerSetup;
        }
    }
}