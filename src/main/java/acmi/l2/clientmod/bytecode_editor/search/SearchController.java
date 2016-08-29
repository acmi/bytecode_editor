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
package acmi.l2.clientmod.bytecode_editor.search;

import acmi.l2.clientmod.bytecode_editor.Function;
import acmi.l2.clientmod.io.UnrealPackage;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<SearchResult> searchResults;
    @FXML
    private TableColumn<SearchResult, Integer> searchResultRef;
    @FXML
    private TableColumn<SearchResult, String> searchResultType;
    @FXML
    private TableColumn<SearchResult, String> searchResultValue;

    private ObjectProperty<UnrealPackage> unrealPackage = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchResultRef.setPrefWidth(50);
        searchResultRef.setMinWidth(50);
        searchResultRef.setMaxWidth(50);
        searchResultRef.setResizable(false);

        searchResultType.setPrefWidth(60);
        searchResultType.setMinWidth(60);
        searchResultType.setMaxWidth(60);
        searchResultType.setResizable(false);

        searchResultValue.prefWidthProperty().bind(
                searchResults.widthProperty().subtract(
                        searchResultRef.widthProperty()).subtract(
                        searchResultType.widthProperty()).subtract(18)
        );

        searchResultRef.setCellValueFactory(new PropertyValueFactory<>("ref"));
        searchResultType.setCellValueFactory(new PropertyValueFactory<>("type"));
        searchResultValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        InvalidationListener listener = (observable) -> {
            searchResults.getItems().clear();

            String searchString = getSearchString();

            if (getUnrealPackage() == null)
                return;

            if (searchString == null || searchString.isEmpty())
                return;

            Stream<SearchResult> results = Stream.empty();

            try {
                int intValue = Integer.parseInt(searchString);


                try {
                    if (intValue > 0) {
                        results = Stream.concat(results, Stream.of(SearchResult.of(getUnrealPackage().getExportTable().get(intValue - 1))));
                    } else if (intValue < 0) {
                        results = Stream.concat(results, Stream.of(SearchResult.of(getUnrealPackage().getImportTable().get(-intValue - 1))));
                    }
                } catch (IndexOutOfBoundsException ignore) {
                }

                try {
                    results = Stream.concat(results, Stream.of(SearchResult.of(getUnrealPackage().getNameTable().get(intValue))));
                } catch (IndexOutOfBoundsException ignore) {
                }

                Optional<Function> function = Function.getNativeByIndex(intValue);
                if (function.isPresent())
                    results = Stream.concat(results, Stream.of(SearchResult.of(function.get())));
            } catch (NumberFormatException ignore) {
            }

            results = Stream.concat(results, Function.getNativeFunctions()
                    .stream()
                    .filter(function -> function.getNameWithParams().toLowerCase().contains(searchString.toLowerCase()))
                    .map(SearchResult::of));
            results = Stream.concat(results, getUnrealPackage().getExportTable()
                    .stream()
                    .filter(exportEntry -> exportEntry.getObjectInnerFullName().toLowerCase().contains(searchString.toLowerCase()))
                    .map(SearchResult::of));
            results = Stream.concat(results, getUnrealPackage().getImportTable()
                    .stream()
                    .filter(importEntry -> importEntry.getObjectFullName().toLowerCase().contains(searchString.toLowerCase()))
                    .map(SearchResult::of));
            results = Stream.concat(results, getUnrealPackage().getNameTable()
                    .stream()
                    .filter(nameEntry -> nameEntry.getName().toLowerCase().contains(searchString.toLowerCase()))
                    .map(SearchResult::of));


            searchResults.getItems().addAll(results.collect(Collectors.toList()));
        };

        unrealPackageProperty().addListener(listener);

        searchStringProperty().addListener(listener);
    }

    public UnrealPackage getUnrealPackage() {
        return unrealPackage.get();
    }

    public ObjectProperty<UnrealPackage> unrealPackageProperty() {
        return unrealPackage;
    }

    public void setUnrealPackage(UnrealPackage unrealPackage) {
        this.unrealPackage.set(unrealPackage);
    }

    public String getSearchString() {
        return searchField.getText();
    }

    public StringProperty searchStringProperty() {
        return searchField.textProperty();
    }

    public void setSearchString(String line) {
        searchField.setText(line);
    }

    public ObservableList<SearchResult> getSearchResults() {
        return searchResults.getItems();
    }
}
