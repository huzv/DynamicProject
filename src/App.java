import controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setMaximized(true);
        loadCustomFonts();

        MainController controller = new MainController(stage);
        Scene scene = controller.createScene();

        stage.setTitle("Task Scheduler V1.0");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();

        controller.playWelcomeAnimation();
    }

    private void loadCustomFonts() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/resources/fonts/Monocraft.ttf"), 14);
        } catch (Exception e) {
            // Do nothing
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}