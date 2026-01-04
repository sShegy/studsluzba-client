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
    private BorderPane mainLayout;

    // Stack za istoriju
    private final Stack<NavigationState> backStack = new Stack<>();
    private final Stack<NavigationState> forwardStack = new Stack<>();

    private NavigationState currentState;

    // Zastavica da sprečimo petlju (da listener ne reaguje kad mi programski menjamo tab)
    private boolean isNavigating = false;

    @Value("${app.history.max-depth:20}")
    private int maxHistoryDepth;

    public NavigationManager(SpringFXMLLoader springFXMLLoader) {
        this.springFXMLLoader = springFXMLLoader;
    }

    public void initialize(Scene scene, BorderPane mainLayout) {
        this.mainLayout = mainLayout;
        setupGlobalEventHandlers(scene);
    }

    public boolean isNavigating() {
        return isNavigating;
    }

    // Standardna navigacija (Menja celi ekran)
    public void navigateTo(String fxmlPath) {
        navigateTo(fxmlPath, null);
    }

    public void navigateTo(String fxmlPath, Consumer<Object> controllerSetup) {
        if (currentState != null) {
            backStack.push(currentState);
            if (backStack.size() > maxHistoryDepth) {
                backStack.remove(0);
            }
        }
        forwardStack.clear();
        loadView(fxmlPath, controllerSetup);
    }

    /**
     * KLJUČNA METODA ZA TABOVE:
     * Beleži promenu stanja (npr. promena taba) bez ponovnog učitavanja FXML-a.
     */
    public void recordStateChange(Consumer<Object> newStateSetup) {
        if (isNavigating) return; // Ne beleži ako se upravo vraćamo nazad/napred

        if (currentState != null) {
            // Gurnemo STARO stanje u istoriju
            backStack.push(currentState);
            if (backStack.size() > maxHistoryDepth) backStack.remove(0);
        }
        // Brišemo forward jer smo napravili novu granu
        forwardStack.clear();

        // Ažuriramo trenutno stanje da reflektuje NOVI tab
        // Koristimo isti FXML path, ali novi setup (koji selektuje novi tab)
        currentState = new NavigationState(currentState.fxmlPath, newStateSetup);
    }

    public void goBack() {
        if (backStack.isEmpty()) return;

        isNavigating = true; // Blokiramo beleženje novih stanja dok se vraćamo
        try {
            forwardStack.push(currentState);
            NavigationState previous = backStack.pop();
            loadView(previous.fxmlPath, previous.controllerSetup);
        } finally {
            isNavigating = false;
        }
    }

    public void goForward() {
        if (forwardStack.isEmpty()) return;

        isNavigating = true;
        try {
            backStack.push(currentState);
            NavigationState next = forwardStack.pop();
            loadView(next.fxmlPath, next.controllerSetup);
        } finally {
            isNavigating = false;
        }
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
        // Miš (Back/Forward dugmići)
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.BACK) {
                goBack();
                event.consume();
            } else if (event.getButton() == MouseButton.FORWARD) {
                goForward();
                event.consume();
            }
        });

        // Tastatura (Ctrl + [ i Ctrl + ])
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