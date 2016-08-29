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

import acmi.l2.clientmod.bytecode_editor.search.SearchController;
import acmi.l2.clientmod.io.ObjectInput;
import acmi.l2.clientmod.io.ObjectInputStream;
import acmi.l2.clientmod.io.ObjectOutputStream;
import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.unreal.UnrealRuntimeContext;
import acmi.l2.clientmod.unreal.bytecode.BytecodeContext;
import acmi.l2.clientmod.unreal.bytecode.TokenSerializerFactory;
import acmi.l2.clientmod.unreal.bytecode.token.Token;
import groovy.lang.GroovyShell;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.IOGroovyMethods.readLines;

public class Controller implements Initializable {
    @FXML
    private ListView<ExportEntryView> entries;
    @FXML
    public TextField entriesSearchField;
    @FXML
    private TextArea offsets;
    @FXML
    private TextArea tokens;
    @FXML
    private TextArea text;

    @SuppressWarnings("unused")
    @FXML
    private SearchController searchController;

    @FXML
    private ProgressIndicator compileProgress;

    Main application;

    private ObjectProperty<File> initialDirectory = new SimpleObjectProperty<>();
    private ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    private ObjectProperty<UnrealPackage> unrealPackage = new SimpleObjectProperty<>();
    private TokenSerializerFactory serializerFactory = new TokenSerializerFactory();
    private ObservableList<ExportEntryView> entriesList = FXCollections.observableArrayList();

    private GroovyShell shell = new GroovyShell();

    public File getInitialDirectory() {
        return initialDirectory.get();
    }

    public ObjectProperty<File> initialDirectoryProperty() {
        return initialDirectory;
    }

    public void setInitialDirectory(File initialDirectory) {
        this.initialDirectory.set(initialDirectory);
    }

    public File getSelectedFile() {
        return selectedFile.get();
    }

    public ObjectProperty<File> selectedFileProperty() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile.set(selectedFile);
    }

    public UnrealPackage getUnrealPackage() {
        return unrealPackage.get();
    }

    public ReadOnlyObjectProperty<UnrealPackage> unrealPackageProperty() {
        return unrealPackage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File id = new File(Main.getPrefs().get("initialDirectory", System.getProperty("user.dir")));
        if (!id.exists())
            id = new File(System.getProperty("user.dir"));
        setInitialDirectory(id);

        initialDirectoryProperty().addListener((observable, oldValue, newValue) -> {
            Main.getPrefs().put("initialDirectory", newValue.getAbsolutePath());
        });

        unrealPackage.bind(Bindings.createObjectBinding(() -> {
            if (getSelectedFile() == null)
                return null;

            try (UnrealPackage up = new UnrealPackage(getSelectedFile(), true)) {
                return up;
            } catch (UncheckedIOException e) {
                return null;
            }
        }, selectedFileProperty()));

        unrealPackageProperty().addListener(observable -> {
            buildTree();
        });

        offsets.scrollTopProperty().bindBidirectional(tokens.scrollTopProperty());
        offsets.scrollTopProperty().bindBidirectional(text.scrollTopProperty());

        entries.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            offsets.clear();
            tokens.clear();
            text.clear();

            if (newValue == null)
                return;

            loadEntry(newValue, line -> {
                offsets.appendText(String.format("0x%04x", line.getOffset()) + "\n");
                tokens.appendText(line.getToken() + "\n");
                text.appendText(line.getText() + "\n");
            });
        });

        searchController.unrealPackageProperty().bind(unrealPackageProperty());

        entries.setItems(entriesList);
        entriesSearchField.textProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                entries.setItems(entriesList);
                return;
            }

            String value = newVal.toLowerCase();
            List<ExportEntryView> list = oldVal != null && newVal.length() > oldVal.length() ?
                    entries.getItems() : entriesList;
            entries.setItems(list
                    .stream()
                    .filter(entry -> entry.getEntry().getObjectInnerFullName().toLowerCase().contains(value))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        });

        compileProgress.setVisible(false);
    }

    public void openPackage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open unreal package");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Unreal package (*.u)", "*.u"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        if (getInitialDirectory() != null &&
                getInitialDirectory().exists() &&
                getInitialDirectory().isDirectory())
            fileChooser.setInitialDirectory(getInitialDirectory());

        File selected = fileChooser.showOpenDialog(application.getStage());
        if (selected == null)
            return;

        setSelectedFile(selected);
        setInitialDirectory(selected.getParentFile());
    }

    protected void buildTree() {
        entriesList.clear();

        if (getUnrealPackage() != null) {
            entriesList.addAll(getUnrealPackage().getExportTable()
                    .stream()
                    .filter(exportEntry -> exportEntry.getFullClassName().equalsIgnoreCase("Core.Struct") ||
                            exportEntry.getFullClassName().equalsIgnoreCase("Core.State") ||
                            (exportEntry.getFullClassName().equalsIgnoreCase("Core.Function") && !Function.fromExportEntry(exportEntry).getFlags().contains(Function.Flag.NATIVE)))

                    .sorted((o1, o2) -> o1.getObjectInnerFullName().compareToIgnoreCase(o2.getObjectInnerFullName()))
                    .map(ExportEntryView::new)
                    .collect(Collectors.toList()));
        }
    }

    protected void loadEntry(ExportEntryView entryView, Consumer<Line> output) {
        UnrealPackage.ExportEntry entry = entryView.getEntry();
        UnrealPackage up = entry.getUnrealPackage();
        byte[] entryBytes = entry.getObjectRawDataExternally();
        BytecodeContext context = new BytecodeContext(up);
        TokenSerializerFactory tokenSerializerFactory = new TokenSerializerFactory();
        ObjectInput<BytecodeContext> input = new ObjectInputStream<>(
                new ByteArrayInputStream(entryBytes),
                up.getFile().getCharset(),
                0,
                tokenSerializerFactory,
                context
        );
        if (!entry.getFullClassName().equalsIgnoreCase("Core.Class"))
            input.readCompactInt();
        input.readCompactInt();
        input.readCompactInt();
        input.readCompactInt();
        input.readCompactInt();
        input.readCompactInt();
        input.readCompactInt();
        input.readInt();
        input.readInt();
        int pos = input.getPosition();
        entryView.setBytecodeStartOffset(pos);
        int size = input.readInt();
        entryView.setBytecodeSize(size);
        int readSize = 0;
        while (readSize < size) {
            Token token = input.readObject(Token.class);

            String code = "ERROR";
            try {
                code = token.toString(new UnrealRuntimeContext(entry, null));
            } catch (Exception ignore) {
            }
            output.accept(new Line(readSize, token.toString(), code));

            readSize += token.getSize(input.getContext());
        }
        entryView.setBytecodeBytesSize(input.getPosition() - pos);
    }

    public void compile() {
        new Thread() {
            @Override
            public void run() {
                Platform.runLater(() -> compileProgress.setVisible(true));

                ExportEntryView entryView;
                String text;

                if (getUnrealPackage() == null ||
                        (entryView = entries.getSelectionModel().getSelectedItem()) == null ||
                        (text = tokens.getText()).isEmpty())
                    return;

                int lineNum = 0;
                try {
                    List<Token> tokens = new ArrayList<>();

                    for (String line : readLines(new StringReader(text))) {
                        if (line.isEmpty())
                            continue;

                        tokens.add((Token) shell.evaluate("def methodMissing(String name, args) {\n" +
                                "Class.forName(\"acmi.l2.clientmod.unreal.bytecode.token.$name\").newInstance(args)\n" +
                                "}\n" + line));
                        lineNum++;
                    }

                    ByteArrayOutputStream newEntry = new ByteArrayOutputStream();

                    try (UnrealPackage up = new UnrealPackage(getUnrealPackage().getFile().openNewSession(false))) {
                        UnrealPackage.ExportEntry entry = up.getExportTable().get(entryView.getEntry().getIndex());

                        byte[] bytes = entry.getObjectRawData();

                        BytecodeContext context = new BytecodeContext(getUnrealPackage());
                        ObjectOutputStream<BytecodeContext> oos = new ObjectOutputStream<>(newEntry, getUnrealPackage().getFile().getCharset(), serializerFactory, context);
                        oos.writeBytes(bytes, 0, entryView.getBytecodeStartOffset());
                        oos.writeInt(tokens.stream().mapToInt(t -> t.getSize(context)).sum());
                        tokens.forEach(oos::write);
                        oos.writeBytes(bytes, entryView.getBytecodeStartOffset() + entryView.getBytecodeBytesSize(), entry.getSize() - (entryView.getBytecodeStartOffset() + entryView.getBytecodeBytesSize()));

                        byte[] newBytes = newEntry.toByteArray();

                        entry.setObjectRawData(newBytes);

                        entryView.setEntry(entry);
                    }

                    Platform.runLater(() -> {
                        int index = entries.getSelectionModel().getSelectedIndex();
                        entries.getSelectionModel().clearSelection();
                        entries.getSelectionModel().select(index);
                    });
                } catch (Exception e) {
                    System.err.println("Exception at line " + lineNum);
                    e.printStackTrace();
                } finally {
                    Platform.runLater(() -> compileProgress.setVisible(false));
                }
            }
        }.start();
    }
}
