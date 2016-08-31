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
package acmi.l2.clientmod.bytecode_editor.calc;

import acmi.l2.clientmod.io.ObjectInputStream;
import acmi.l2.clientmod.io.ObjectOutputStream;
import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.unreal.bytecode.BytecodeContext;
import acmi.l2.clientmod.unreal.bytecode.TokenSerializerFactory;
import acmi.l2.clientmod.unreal.bytecode.token.Token;
import groovy.lang.GroovyShell;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CalcController implements Initializable {
    @FXML
    private TextField bytes;
    @FXML
    private TextField tokens;
    @FXML
    private Label size;

    private TokenSerializerFactory tokenSerializer = new TokenSerializerFactory();
    private GroovyShell shell = new GroovyShell();
    private ObjectProperty<UnrealPackage> unrealPackage = new SimpleObjectProperty<>(this, "unrealPackage");

    public UnrealPackage getUnrealPackage() {
        return unrealPackage.get();
    }

    public ObjectProperty<UnrealPackage> unrealPackageProperty() {
        return unrealPackage;
    }

    public void setUnrealPackage(UnrealPackage unrealPackage) {
        this.unrealPackage.set(unrealPackage);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding disable = unrealPackageProperty().isNull();
        bytes.disableProperty().bind(disable);
        tokens.disableProperty().bind(disable);

        bytes.textProperty().bindBidirectional(tokens.textProperty(), new StringConverter<String>() {
            @Override
            public String fromString(String string) {
                string = string.replaceAll("\\s", "");

                if (string.length() % 2 == 1)
                    return "";

                try {
                    BytecodeContext context = new BytecodeContext(getUnrealPackage());
                    ObjectInputStream<BytecodeContext> ois = new ObjectInputStream<>(
                            new ByteArrayInputStream(DatatypeConverter.parseHexBinary(string)),
                            getUnrealPackage().getFile().getCharset(),
                            tokenSerializer,
                            context);
                    Token token = ois.readObject(Token.class);
                    size.setText(String.valueOf(token.getSize(context)));
                    return token.toString();
                } catch (UncheckedIOException ignore) {
                } catch (IllegalArgumentException e) {
                    return e.getMessage();
                }
                return "";
            }

            @Override
            public String toString(String object) {
                try {
                    Token token = (Token) shell.evaluate("def methodMissing(String name, args) {\n" +
                            "Class.forName(\"acmi.l2.clientmod.unreal.bytecode.token.$name\").newInstance(args)\n" +
                            "}\n" + object);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BytecodeContext context = new BytecodeContext(getUnrealPackage());
                    size.setText(String.valueOf(token.getSize(context)));
                    ObjectOutputStream<BytecodeContext> oos = new ObjectOutputStream<>(
                            baos,
                            getUnrealPackage().getFile().getCharset(),
                            tokenSerializer,
                            context
                    );
                    oos.write(token);
                    return DatatypeConverter.printHexBinary(baos.toByteArray());
                } catch (Exception ignore){
                    return "";
                }
            }
        });
    }
}
