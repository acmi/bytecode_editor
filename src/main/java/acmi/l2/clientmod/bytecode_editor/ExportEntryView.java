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

import acmi.l2.clientmod.io.UnrealPackage;

public class ExportEntryView {
    private UnrealPackage.ExportEntry entry;
    private int bytecodeStartOffset;
    private int bytecodeBytesSize;
    private int bytecodeSize;

    public ExportEntryView(UnrealPackage.ExportEntry entry) {
        this.entry = entry;
    }

    public UnrealPackage.ExportEntry getEntry() {
        return entry;
    }

    public void setEntry(UnrealPackage.ExportEntry entry) {
        this.entry = entry;
    }

    public int getBytecodeStartOffset() {
        return bytecodeStartOffset;
    }

    public void setBytecodeStartOffset(int bytecodeStartOffset) {
        this.bytecodeStartOffset = bytecodeStartOffset;
    }

    public int getBytecodeBytesSize() {
        return bytecodeBytesSize;
    }

    public void setBytecodeBytesSize(int bytecodeBytesSize) {
        this.bytecodeBytesSize = bytecodeBytesSize;
    }

    public int getBytecodeSize() {
        return bytecodeSize;
    }

    public void setBytecodeSize(int bytecodeSize) {
        this.bytecodeSize = bytecodeSize;
    }

    public String getType() {
        return entry.getObjectClass() == null ? "Class" : entry.getObjectClass().getObjectName().getName();
    }

    @Override
    public String toString() {
        return entry.getObjectInnerFullName() + "[" + getType() + "]";
    }
}
