/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.bytecode_editor;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Main extends Application {
    private Stage stage;

    public Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Bytecode editor");
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();

        Controller controller = loader.getController();
        controller.application = this;

        stage.setX(Double.parseDouble(getPrefs().get("window.x", String.valueOf(stage.getX()))));
        stage.setY(Double.parseDouble(getPrefs().get("window.y", String.valueOf(stage.getY()))));
        stage.setWidth(Double.parseDouble(getPrefs().get("window.width", String.valueOf(stage.getWidth()))));
        stage.setHeight(Double.parseDouble(getPrefs().get("window.height", String.valueOf(stage.getHeight()))));

        InvalidationListener listener = observable -> {
            getPrefs().put("window.x", String.valueOf(Math.round(stage.getX())));
            getPrefs().put("window.y", String.valueOf(Math.round(stage.getY())));
            getPrefs().put("window.width", String.valueOf(Math.round(stage.getWidth())));
            getPrefs().put("window.height", String.valueOf(Math.round(stage.getHeight())));
        };
        stage.xProperty().addListener(listener);
        stage.yProperty().addListener(listener);
        stage.widthProperty().addListener(listener);
        stage.heightProperty().addListener(listener);
    }

    public static Preferences getPrefs() {
        return Preferences.userRoot().node("bytecode_editor");
    }

    public static void main(String[] args) {
        launch(Main.class, args);
    }
}
