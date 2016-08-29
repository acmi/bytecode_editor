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

public class SearchResult {
    private final int ref;
    private final String type;
    private final String value;

    public SearchResult(int ref, String type, String value) {
        this.ref = ref;
        this.type = type;
        this.value = value;
    }

    public static SearchResult of(UnrealPackage.NameEntry nameEntry) {
        return new SearchResult(nameEntry.getIndex(), "Name", nameEntry.getName());
    }

    public static SearchResult of(UnrealPackage.ImportEntry importEntry) {
        return new SearchResult(importEntry.getObjectReference(), "Import", importEntry.getObjectFullName() + "[" + importEntry.getFullClassName() + "]");
    }

    public static SearchResult of(UnrealPackage.ExportEntry exportEntry) {
        return new SearchResult(exportEntry.getObjectReference(), "Export", exportEntry.getObjectInnerFullName() + "[" + exportEntry.getFullClassName() + "]");
    }

    public static SearchResult of(Function natFunc) {
        return new SearchResult(natFunc.getNativeIndex(), "Func", natFunc.getNameWithParams());
    }

    public int getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
