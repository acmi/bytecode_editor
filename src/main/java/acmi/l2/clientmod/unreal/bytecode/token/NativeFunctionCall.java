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
package acmi.l2.clientmod.unreal.bytecode.token;

import acmi.l2.clientmod.bytecode_editor.Function;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.unreal.UnrealRuntimeContext;
import acmi.l2.clientmod.unreal.bytecode.token.annotation.FunctionParams;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NativeFunctionCall extends Token {
    public transient int nativeIndex;
    @FunctionParams
    public Token[] params;

    public NativeFunctionCall(int nativeIndex, Token... params) {
        this.nativeIndex = nativeIndex;
        this.params = params;
    }

    public NativeFunctionCall() {
    }

    protected int getOpcode() {
        return this.nativeIndex;
    }

    protected void writeOpcode(DataOutput output, int opcode) throws UncheckedIOException {
        if (opcode > 255) {
            output.writeByte(96 + (opcode >> 8 & 15));
            output.writeByte(opcode & 255);
        } else {
            output.writeByte(opcode);
        }

    }

    protected Sizer getSizer() {
        return (token, context) -> {
            NativeFunctionCall nativeFunctionCall = (NativeFunctionCall) token;
            return (nativeFunctionCall.nativeIndex > 255 ? 2 : 1) + Stream.concat(Arrays.stream(nativeFunctionCall.params), Stream.of(new EndFunctionParams())).mapToInt((t) -> t.getSize(context)).sum();
        };
    }

    public String toString() {
        return "NativeFunctionCall(" + this.nativeIndex + (this.params != null && this.params.length != 0 ? ", " + (String) Arrays.stream(this.params).map(Objects::toString).collect(Collectors.joining(", ")) : "") + ')';
    }

    public String toString(UnrealRuntimeContext context) {
        Optional<Function> function = Function.getNativeByIndex(nativeIndex);
        if (function.isPresent()) {
            Function f = function.get();
            if (f.getFlags().contains(Function.Flag.PRE_OPERATOR)) {
                return f.getName() + params[0].toString(context);
            } else if (f.getFlags().contains(Function.Flag.OPERATOR)) {
                if (f.getOperatorPrecedence() > 0) {
                    return params[0].toString(context) + " " + f.getName() + " " + params[params.length - 1].toString(context);
                } else {
                    return params[0].toString(context) + f.getName();
                }
            } else {
                return f.getName() + Arrays.stream(this.params).map((p) -> p.toString(context)).collect(Collectors.joining(", ", "(", ")"));
            }
        } else {
            return "native" + this.nativeIndex + Arrays.stream(this.params).map((p) -> p.toString(context)).collect(Collectors.joining(", ", "(", ")"));
        }
    }
}