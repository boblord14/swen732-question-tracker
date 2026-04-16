package app;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.util.Duration;

/**
 * Small UI helpers for lightweight animations.
 */
public class UIUtils {

    private UIUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * Fade in the given root node over the specified milliseconds.
     */
    public static void fadeIn(Parent root, int millis) {
        if (root == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(millis), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    /**
     * Default fade-in (260ms).
     */
    public static void fadeIn(Parent root) {
        fadeIn(root, 260);
    }
}
