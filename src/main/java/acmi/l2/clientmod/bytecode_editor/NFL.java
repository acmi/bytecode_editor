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

import acmi.l2.clientmod.io.DataInputStream;
import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.unreal.Environment;
import acmi.l2.clientmod.unreal.UnrealSerializerFactory;
import acmi.l2.clientmod.unreal.bytecode.token.NativeParam;
import acmi.l2.clientmod.unreal.core.Function;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class NFL {
    public static void main(String[] args) {
        File l2system = new File(args[0]);

        Environment environment = Environment.fromIni(new File(l2system, "L2.ini"));
        UnrealSerializerFactory serializerFactory = new UnrealSerializerFactory(environment);

        File[] files = l2system
                .listFiles((dir, name) -> name.endsWith(".u") && !name.startsWith("Lineage"));
        for (File file : files) {
            try (UnrealPackage up = new UnrealPackage(file, true)) {
                up.getExportTable()
                        .stream()
                        .filter(e -> e.getFullClassName().equalsIgnoreCase("Core.Class"))
                        .map(serializerFactory::getOrCreateObject)
                        .map(object -> object.entry.getObjectFullName())
                        .forEach(System.out::println);
            }
        }
        try {
            Field field = UnrealSerializerFactory.class.getDeclaredField("nativeFunctions");
            field.setAccessible(true);
            Map<Integer, Function> map = (Map) field.get(serializerFactory);
            map.values().stream()
                    .sorted((f1, f2) -> Integer.compare(f1.nativeIndex, f2.nativeIndex))
                    .map(f -> String.format("put(new Function(\"%s\", %s, %d, %d, %d));",
                            f.friendlyName,
                            Arrays.stream(f.bytecode)
                                    .filter(token -> token instanceof NativeParam)
                                    .map(token -> getType(f.entry.getUnrealPackage().objectReference(((NativeParam) token).objRef)))
                                    .map(s -> '"' + s + '"')
                                    .collect(Collectors.joining(", ", "Arrays.asList(", ")")),
                            f.nativeIndex,
                            f.operatorPrecedence,
                            f.functionFlags))
                    .forEach(System.out::println);
        } catch (ReflectiveOperationException e) {
            System.err.println("Couldn't access UnrealSerializerFactory.nativeFunctions");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static String getType(UnrealPackage.Entry e) {
        if (e instanceof UnrealPackage.ImportEntry) {
            return e.getObjectFullName();
        } else {
            UnrealPackage.ExportEntry entry = (UnrealPackage.ExportEntry) e;
            String typeStr = entry.getObjectClass() == null ? "Class" : entry.getObjectClass().getObjectName().getName();
            switch (typeStr) {
                case "FloatProperty":
                case "ByteProperty":
                case "BoolProperty":
                case "IntProperty":
                case "NameProperty":
                case "StrProperty":
                    return typeStr.replace("Property", "");
                case "ArrayProperty": {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), null);
                    skipPropertyFields(dis);
                    return "array<" + getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + ">";
                }
                case "ClassProperty": {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), null);
                    skipPropertyFields(dis);
                    return getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + "<" + getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + ">";
                }
                case "ObjectProperty":
                case "StructProperty": {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), null);
                    skipPropertyFields(dis);
                    return getType(entry.getUnrealPackage().objectReference(dis.readCompactInt()));
                }
                case "Class":
                case "Struct":
                    return entry.getObjectFullName();
                default:
                    throw new RuntimeException(typeStr);
            }
        }
    }

    private static void skipPropertyFields(DataInputStream dis) {
        dis.readCompactInt();
        dis.readCompactInt();
        dis.readCompactInt();
        dis.skip(8);
        dis.readCompactInt();
    }
}
