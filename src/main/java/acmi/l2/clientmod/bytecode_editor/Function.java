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
import acmi.l2.clientmod.io.ObjectInput;
import acmi.l2.clientmod.io.ObjectInputStream;
import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.unreal.bytecode.BytecodeContext;
import acmi.l2.clientmod.unreal.bytecode.TokenSerializerFactory;
import acmi.l2.clientmod.unreal.bytecode.token.NativeParam;
import acmi.l2.clientmod.unreal.bytecode.token.Token;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Function {
    private String name;
    private List<String> params;
    private String nameWithParams;
    private int nativeIndex;
    private int operatorPrecedence;
    private int flags;

    public Function(String name, List<String> params, int nativeIndex, int operatorPrecedence, int flags) {
        this.name = name;
        this.params = params;
        this.nativeIndex = nativeIndex;
        this.operatorPrecedence = operatorPrecedence;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public List<String> getParams() {
        return params;
    }

    public String getNameWithParams() {
        if (nameWithParams == null) {
            if (getFlags().contains(Function.Flag.PRE_OPERATOR)) {
                nameWithParams = getName() + getParams().get(0);
            } else if (getFlags().contains(Function.Flag.OPERATOR)) {
                if (getOperatorPrecedence() > 0) {
                    nameWithParams = getParams().get(0) + getName() + getParams().get(1);
                } else {
                    nameWithParams = getParams().get(0) + getName();
                }
            } else {
                nameWithParams = getName() + params.stream().collect(Collectors.joining(",", "(", ")"));
            }
        }
        return nameWithParams;
    }

    public int getNativeIndex() {
        return nativeIndex;
    }

    public int getOperatorPrecedence() {
        return operatorPrecedence;
    }

    public Collection<Flag> getFlags() {
        return Flag.getFlags(flags);
    }

    @Override
    public String toString() {
        return "put (new Function(" +
                '"' + name + '"' +
                ", " + params.stream().map(s -> '"' + s + '"').collect(Collectors.joining(", ", "Arrays.asList(", ")")) +
                ", " + nativeIndex +
                ", " + operatorPrecedence +
                ", " + flags +
                "));";
    }

    private static Map<Integer, Function> nativeFunctions = new HashMap<>();

    public static Optional<Function> getNativeByIndex(int index) {
        return Optional.ofNullable(nativeFunctions.get(index));
    }

    public static Collection<Function> getNativeFunctions() {
        return nativeFunctions.values();
    }

    static {
        put(new Function("$", Arrays.asList("Str", "Str"), 112, 40, 144385));
        put(new Function("GotoState", Arrays.asList("Name", "Name"), 113, 0, 132097));
        put(new Function("==", Arrays.asList("Core.Object", "Core.Object"), 114, 24, 144385));
        put(new Function("<", Arrays.asList("Str", "Str"), 115, 24, 144385));
        put(new Function(">", Arrays.asList("Str", "Str"), 116, 24, 144385));
        put(new Function("Enable", Collections.singletonList("Name"), 117, 0, 132097));
        put(new Function("Disable", Collections.singletonList("Name"), 118, 0, 132097));
        put(new Function("!=", Arrays.asList("Core.Object", "Core.Object"), 119, 26, 144385));
        put(new Function("<=", Arrays.asList("Str", "Str"), 120, 24, 144385));
        put(new Function(">=", Arrays.asList("Str", "Str"), 121, 24, 144385));
        put(new Function("==", Arrays.asList("Str", "Str"), 122, 24, 144385));
        put(new Function("!=", Arrays.asList("Str", "Str"), 123, 26, 144385));
        put(new Function("~=", Arrays.asList("Str", "Str"), 124, 24, 144385));
        put(new Function("Len", Collections.singletonList("Str"), 125, 0, 140289));
        put(new Function("InStr", Arrays.asList("Str", "Str"), 126, 0, 140289));
        put(new Function("Mid", Arrays.asList("Str", "Int", "Int"), 127, 0, 140289));
        put(new Function("Left", Arrays.asList("Str", "Int"), 128, 0, 140289));
        put(new Function("!", Collections.singletonList("Bool"), 129, 0, 144401));
        put(new Function("&&", Arrays.asList("Bool", "Bool"), 130, 30, 144385));
        put(new Function("^^", Arrays.asList("Bool", "Bool"), 131, 30, 144385));
        put(new Function("||", Arrays.asList("Bool", "Bool"), 132, 32, 144385));
        put(new Function("*=", Arrays.asList("Byte", "Byte"), 133, 34, 144385));
        put(new Function("/=", Arrays.asList("Byte", "Byte"), 134, 34, 144385));
        put(new Function("+=", Arrays.asList("Byte", "Byte"), 135, 34, 144385));
        put(new Function("-=", Arrays.asList("Byte", "Byte"), 136, 34, 144385));
        put(new Function("++", Collections.singletonList("Byte"), 137, 0, 144401));
        put(new Function("--", Collections.singletonList("Byte"), 138, 0, 144401));
        put(new Function("++", Collections.singletonList("Byte"), 139, 0, 144385));
        put(new Function("--", Collections.singletonList("Byte"), 140, 0, 144385));
        put(new Function("~", Collections.singletonList("Int"), 141, 0, 144401));
        put(new Function("==", Arrays.asList("Rotator", "Rotator"), 142, 24, 144385));
        put(new Function("-", Collections.singletonList("Int"), 143, 0, 144401));
        put(new Function("*", Arrays.asList("Int", "Int"), 144, 16, 144385));
        put(new Function("/", Arrays.asList("Int", "Int"), 145, 16, 144385));
        put(new Function("+", Arrays.asList("Int", "Int"), 146, 20, 144385));
        put(new Function("-", Arrays.asList("Int", "Int"), 147, 20, 144385));
        put(new Function("<<", Arrays.asList("Int", "Int"), 148, 22, 144385));
        put(new Function(">>", Arrays.asList("Int", "Int"), 149, 22, 144385));
        put(new Function("<", Arrays.asList("Int", "Int"), 150, 24, 144385));
        put(new Function(">", Arrays.asList("Int", "Int"), 151, 24, 144385));
        put(new Function("<=", Arrays.asList("Int", "Int"), 152, 24, 144385));
        put(new Function(">=", Arrays.asList("Int", "Int"), 153, 24, 144385));
        put(new Function("==", Arrays.asList("Int", "Int"), 154, 24, 144385));
        put(new Function("!=", Arrays.asList("Int", "Int"), 155, 26, 144385));
        put(new Function("&", Arrays.asList("Int", "Int"), 156, 28, 144385));
        put(new Function("^", Arrays.asList("Int", "Int"), 157, 28, 144385));
        put(new Function("|", Arrays.asList("Int", "Int"), 158, 28, 144385));
        put(new Function("*=", Arrays.asList("Int", "Float"), 159, 34, 144385));
        put(new Function("/=", Arrays.asList("Int", "Float"), 160, 34, 144385));
        put(new Function("+=", Arrays.asList("Int", "Int"), 161, 34, 144385));
        put(new Function("-=", Arrays.asList("Int", "Int"), 162, 34, 144385));
        put(new Function("++", Collections.singletonList("Int"), 163, 0, 144401));
        put(new Function("--", Collections.singletonList("Int"), 164, 0, 144401));
        put(new Function("++", Collections.singletonList("Int"), 165, 0, 144385));
        put(new Function("--", Collections.singletonList("Int"), 166, 0, 144385));
        put(new Function("Rand", Collections.singletonList("Int"), 167, 0, 140289));
        put(new Function("@", Arrays.asList("Str", "Str"), 168, 40, 144385));
        put(new Function("-", Collections.singletonList("Float"), 169, 0, 144401));
        put(new Function("**", Arrays.asList("Float", "Float"), 170, 12, 144385));
        put(new Function("*", Arrays.asList("Float", "Float"), 171, 16, 144385));
        put(new Function("/", Arrays.asList("Float", "Float"), 172, 16, 144385));
        put(new Function("%", Arrays.asList("Float", "Float"), 173, 18, 144385));
        put(new Function("+", Arrays.asList("Float", "Float"), 174, 20, 144385));
        put(new Function("-", Arrays.asList("Float", "Float"), 175, 20, 144385));
        put(new Function("<", Arrays.asList("Float", "Float"), 176, 24, 144385));
        put(new Function(">", Arrays.asList("Float", "Float"), 177, 24, 144385));
        put(new Function("<=", Arrays.asList("Float", "Float"), 178, 24, 144385));
        put(new Function(">=", Arrays.asList("Float", "Float"), 179, 24, 144385));
        put(new Function("==", Arrays.asList("Float", "Float"), 180, 24, 144385));
        put(new Function("!=", Arrays.asList("Float", "Float"), 181, 26, 144385));
        put(new Function("*=", Arrays.asList("Float", "Float"), 182, 34, 144385));
        put(new Function("/=", Arrays.asList("Float", "Float"), 183, 34, 144385));
        put(new Function("+=", Arrays.asList("Float", "Float"), 184, 34, 144385));
        put(new Function("-=", Arrays.asList("Float", "Float"), 185, 34, 144385));
        put(new Function("Abs", Collections.singletonList("Float"), 186, 0, 140289));
        put(new Function("Sin", Collections.singletonList("Float"), 187, 0, 140289));
        put(new Function("Cos", Collections.singletonList("Float"), 188, 0, 140289));
        put(new Function("Tan", Collections.singletonList("Float"), 189, 0, 140289));
        put(new Function("Atan", Arrays.asList("Float", "Float"), 190, 0, 140289));
        put(new Function("Exp", Collections.singletonList("Float"), 191, 0, 140289));
        put(new Function("Loge", Collections.singletonList("Float"), 192, 0, 140289));
        put(new Function("Sqrt", Collections.singletonList("Float"), 193, 0, 140289));
        put(new Function("Square", Collections.singletonList("Float"), 194, 0, 140289));
        put(new Function("FRand", Collections.emptyList(), 195, 0, 140289));
        put(new Function(">>>", Arrays.asList("Int", "Int"), 196, 22, 144385));
        put(new Function("*", Arrays.asList("Int", "Float"), 197, 16, 144385));
        put(new Function("!=", Arrays.asList("Rotator", "Rotator"), 203, 26, 144385));
        put(new Function("~=", Arrays.asList("Float", "Float"), 210, 24, 144385));
        put(new Function("-", Collections.singletonList("Vector"), 211, 0, 144401));
        put(new Function("*", Arrays.asList("Vector", "Float"), 212, 16, 144385));
        put(new Function("*", Arrays.asList("Float", "Vector"), 213, 16, 144385));
        put(new Function("/", Arrays.asList("Vector", "Float"), 214, 16, 144385));
        put(new Function("+", Arrays.asList("Vector", "Vector"), 215, 20, 144385));
        put(new Function("-", Arrays.asList("Vector", "Vector"), 216, 20, 144385));
        put(new Function("==", Arrays.asList("Vector", "Vector"), 217, 24, 144385));
        put(new Function("!=", Arrays.asList("Vector", "Vector"), 218, 26, 144385));
        put(new Function("Dot", Arrays.asList("Vector", "Vector"), 219, 16, 144385));
        put(new Function("Cross", Arrays.asList("Vector", "Vector"), 220, 16, 144385));
        put(new Function("*=", Arrays.asList("Vector", "Float"), 221, 34, 144385));
        put(new Function("/=", Arrays.asList("Vector", "Float"), 222, 34, 144385));
        put(new Function("+=", Arrays.asList("Vector", "Vector"), 223, 34, 144385));
        put(new Function("-=", Arrays.asList("Vector", "Vector"), 224, 34, 144385));
        put(new Function("VSize", Collections.singletonList("Vector"), 225, 0, 140289));
        put(new Function("Normal", Collections.singletonList("Vector"), 226, 0, 140289));
        put(new Function("Invert", Arrays.asList("Vector", "Vector", "Vector"), 227, 0, 140289));
        put(new Function("GetAxes", Arrays.asList("Rotator", "Vector", "Vector", "Vector"), 229, 0, 140289));
        put(new Function("GetUnAxes", Arrays.asList("Rotator", "Vector", "Vector", "Vector"), 230, 0, 140289));
        put(new Function("Log", Arrays.asList("Str", "Name"), 231, 0, 140289));
        put(new Function("Warn", Collections.singletonList("Str"), 232, 0, 140289));
        put(new Function("Error", Collections.singletonList("Str"), 233, 0, 132097));
        put(new Function("Right", Arrays.asList("Str", "Int"), 234, 0, 140289));
        put(new Function("Caps", Collections.singletonList("Str"), 235, 0, 140289));
        put(new Function("Chr", Collections.singletonList("Int"), 236, 0, 140289));
        put(new Function("Asc", Collections.singletonList("Str"), 237, 0, 140289));
        put(new Function("Substitute", Arrays.asList("Str", "Str", "Str", "Bool"), 238, 0, 140289));
        put(new Function("==", Arrays.asList("Bool", "Bool"), 242, 24, 144385));
        put(new Function("!=", Arrays.asList("Bool", "Bool"), 243, 26, 144385));
        put(new Function("FMin", Arrays.asList("Float", "Float"), 244, 0, 140289));
        put(new Function("FMax", Arrays.asList("Float", "Float"), 245, 0, 140289));
        put(new Function("FClamp", Arrays.asList("Float", "Float", "Float"), 246, 0, 140289));
        put(new Function("Lerp", Arrays.asList("Float", "Float", "Float"), 247, 0, 140289));
        put(new Function("Smerp", Arrays.asList("Float", "Float", "Float"), 248, 0, 140289));
        put(new Function("Min", Arrays.asList("Int", "Int"), 249, 0, 140289));
        put(new Function("Max", Arrays.asList("Int", "Int"), 250, 0, 140289));
        put(new Function("Clamp", Arrays.asList("Int", "Int", "Int"), 251, 0, 140289));
        put(new Function("VRand", Collections.emptyList(), 252, 0, 140289));
        put(new Function("==", Arrays.asList("Name", "Name"), 254, 24, 144385));
        put(new Function("!=", Arrays.asList("Name", "Name"), 255, 26, 144385));
        put(new Function("Sleep", Collections.singletonList("Float"), 256, 0, 132105));
        put(new Function("ClassIsChildOf", Arrays.asList("Core.Class<Core.Object>", "Core.Class<Core.Object>"), 258, 0, 140289));
        put(new Function("PlayAnim", Arrays.asList("Name", "Float", "Float", "Int"), 259, 0, 132097));
        put(new Function("LoopAnim", Arrays.asList("Name", "Float", "Float", "Int"), 260, 0, 132097));
        put(new Function("FinishAnim", Collections.singletonList("Int"), 261, 0, 132105));
        put(new Function("SetCollision", Arrays.asList("Bool", "Bool", "Bool"), 262, 0, 132097));
        put(new Function("HasAnim", Collections.singletonList("Name"), 263, 0, 132097));
        put(new Function("PlaySound", Arrays.asList("Engine.Sound", "Byte", "Float", "Bool", "Float", "Float", "Bool"), 264, 0, 132097));
        put(new Function("Move", Collections.singletonList("Vector"), 266, 0, 132097));
        put(new Function("SetLocation", Arrays.asList("Vector", "Bool"), 267, 0, 132097));
        put(new Function("SetOwner", Collections.singletonList("Engine.Actor"), 272, 0, 132097));
        put(new Function("<<", Arrays.asList("Vector", "Rotator"), 275, 22, 144385));
        put(new Function(">>", Arrays.asList("Vector", "Rotator"), 276, 22, 144385));
        put(new Function("Trace", Arrays.asList("Vector", "Vector", "Vector", "Vector", "Bool", "Vector", "Engine.Material"), 277, 0, 132097));
        put(new Function("Spawn", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Name", "Vector", "Rotator", "Bool", "Bool"), 278, 0, 132097));
        put(new Function("Destroy", Collections.emptyList(), 279, 0, 132097));
        put(new Function("SetTimer", Arrays.asList("Float", "Bool"), 280, 0, 132097));
        put(new Function("IsInState", Collections.singletonList("Name"), 281, 0, 132097));
        put(new Function("IsAnimating", Collections.singletonList("Int"), 282, 0, 132097));
        put(new Function("SetCollisionSize", Arrays.asList("Float", "Float"), 283, 0, 132097));
        put(new Function("GetStateName", Collections.emptyList(), 284, 0, 132097));
        put(new Function("*", Arrays.asList("Rotator", "Float"), 287, 16, 144385));
        put(new Function("*", Arrays.asList("Float", "Rotator"), 288, 16, 144385));
        put(new Function("/", Arrays.asList("Rotator", "Float"), 289, 16, 144385));
        put(new Function("*=", Arrays.asList("Rotator", "Float"), 290, 34, 144385));
        put(new Function("/=", Arrays.asList("Rotator", "Float"), 291, 34, 144385));
        put(new Function("TweenAnim", Arrays.asList("Name", "Float", "Int"), 294, 0, 132097));
        put(new Function("*", Arrays.asList("Vector", "Vector"), 296, 16, 144385));
        put(new Function("*=", Arrays.asList("Vector", "Vector"), 297, 34, 144385));
        put(new Function("SetBase", Arrays.asList("Engine.Actor", "Vector"), 298, 0, 132097));
        put(new Function("SetRotation", Collections.singletonList("Rotator"), 299, 0, 132097));
        put(new Function("MirrorVectorByNormal", Arrays.asList("Vector", "Vector"), 300, 0, 140289));
        put(new Function("FinishInterpolation", Collections.emptyList(), 301, 0, 132105));
        put(new Function("IsA", Collections.singletonList("Name"), 303, 0, 132097));
        put(new Function("AllActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Name"), 304, 0, 132101));
        put(new Function("ChildActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor"), 305, 0, 132101));
        put(new Function("BasedActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor"), 306, 0, 132101));
        put(new Function("TouchingActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor"), 307, 0, 132101));
        put(new Function("ZoneActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor"), 308, 0, 132101));
        put(new Function("TraceActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Vector", "Vector", "Vector", "Vector", "Vector"), 309, 0, 132101));
        put(new Function("RadiusActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Float", "Vector"), 310, 0, 132101));
        put(new Function("VisibleActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Float", "Vector"), 311, 0, 132101));
        put(new Function("VisibleCollidingActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Float", "Vector", "Bool"), 312, 0, 132101));
        put(new Function("DynamicActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Name"), 313, 0, 132101));
        put(new Function("Warp", Arrays.asList("Vector", "Vector", "Rotator"), 314, 0, 132097));
        put(new Function("UnWarp", Arrays.asList("Vector", "Vector", "Rotator"), 315, 0, 132097));
        put(new Function("+", Arrays.asList("Rotator", "Rotator"), 316, 20, 144385));
        put(new Function("-", Arrays.asList("Rotator", "Rotator"), 317, 20, 144385));
        put(new Function("+=", Arrays.asList("Rotator", "Rotator"), 318, 34, 144385));
        put(new Function("-=", Arrays.asList("Rotator", "Rotator"), 319, 34, 144385));
        put(new Function("RotRand", Collections.singletonList("Bool"), 320, 0, 140289));
        put(new Function("CollidingActors", Arrays.asList("Core.Class<Engine.Actor>", "Engine.Actor", "Float", "Vector"), 321, 0, 132101));
        put(new Function("-", Collections.singletonList("INT64"), 400, 0, 144401));
        put(new Function("*", Arrays.asList("INT64", "INT64"), 401, 16, 144385));
        put(new Function("*", Arrays.asList("INT64", "Float"), 402, 16, 144385));
        put(new Function("/", Arrays.asList("INT64", "INT64"), 403, 16, 144385));
        put(new Function("+", Arrays.asList("INT64", "INT64"), 404, 20, 144385));
        put(new Function("-", Arrays.asList("INT64", "INT64"), 405, 20, 144385));
        put(new Function("+=", Arrays.asList("INT64", "INT64"), 406, 34, 144385));
        put(new Function("-=", Arrays.asList("INT64", "INT64"), 407, 34, 144385));
        put(new Function("<", Arrays.asList("INT64", "INT64"), 408, 24, 144385));
        put(new Function(">", Arrays.asList("INT64", "INT64"), 409, 24, 144385));
        put(new Function("<=", Arrays.asList("INT64", "INT64"), 410, 24, 144385));
        put(new Function(">=", Arrays.asList("INT64", "INT64"), 411, 24, 144385));
        put(new Function("==", Arrays.asList("INT64", "INT64"), 412, 24, 144385));
        put(new Function("!=", Arrays.asList("INT64", "INT64"), 413, 26, 144385));
        put(new Function("StrLen", Arrays.asList("Str", "Float", "Float"), 464, 0, 132097));
        put(new Function("DrawText", Arrays.asList("Str", "Bool"), 465, 0, 132097));
        put(new Function("DrawTile", Arrays.asList("Engine.Material", "Float", "Float", "Float", "Float", "Float", "Float"), 466, 0, 132097));
        put(new Function("DrawActor", Arrays.asList("Engine.Actor", "Bool", "Bool", "Float"), 467, 0, 132097));
        put(new Function("DrawTileClipped", Arrays.asList("Engine.Material", "Float", "Float", "Float", "Float", "Float", "Float"), 468, 0, 132097));
        put(new Function("DrawTextClipped", Arrays.asList("Str", "Bool"), 469, 0, 132097));
        put(new Function("TextSize", Arrays.asList("Str", "Float", "Float"), 470, 0, 132097));
        put(new Function("DrawPortal", Arrays.asList("Int", "Int", "Int", "Int", "Engine.Actor", "Vector", "Rotator", "Int", "Bool"), 480, 0, 132097));
        put(new Function("MoveTo", Arrays.asList("Vector", "Engine.Actor", "Bool"), 500, 0, 132105));
        put(new Function("MoveToward", Arrays.asList("Engine.Actor", "Engine.Actor", "Float", "Bool", "Bool"), 502, 0, 132105));
        put(new Function("FinishRotation", Collections.emptyList(), 508, 0, 132105));
        put(new Function("WaitToSeeEnemy", Collections.emptyList(), 510, 0, 132105));
        put(new Function("MakeNoise", Collections.singletonList("Float"), 512, 0, 132097));
        put(new Function("LineOfSightTo", Collections.singletonList("Engine.Actor"), 514, 0, 132097));
        put(new Function("FindPathToward", Arrays.asList("Engine.Actor", "Bool"), 517, 0, 132097));
        put(new Function("FindPathTo", Collections.singletonList("Vector"), 518, 0, 132097));
        put(new Function("actorReachable", Collections.singletonList("Engine.Actor"), 520, 0, 132097));
        put(new Function("pointReachable", Collections.singletonList("Vector"), 521, 0, 132097));
        put(new Function("EAdjustJump", Arrays.asList("Float", "Float"), 523, 0, 132097));
        put(new Function("FindStairRotation", Collections.singletonList("Float"), 524, 0, 132097));
        put(new Function("FindRandomDest", Collections.emptyList(), 525, 0, 132097));
        put(new Function("PickWallAdjust", Collections.singletonList("Vector"), 526, 0, 132097));
        put(new Function("WaitForLanding", Collections.emptyList(), 527, 0, 132105));
        put(new Function("AddController", Collections.emptyList(), 529, 0, 132097));
        put(new Function("RemoveController", Collections.emptyList(), 530, 0, 132097));
        put(new Function("PickTarget", Arrays.asList("Float", "Float", "Vector", "Vector", "Float"), 531, 0, 132097));
        put(new Function("PlayerCanSeeMe", Collections.emptyList(), 532, 0, 132097));
        put(new Function("CanSee", Collections.singletonList("Engine.Pawn"), 533, 0, 132097));
        put(new Function("PickAnyTarget", Arrays.asList("Float", "Float", "Vector", "Vector"), 534, 0, 132097));
        put(new Function("SaveConfig", Collections.emptyList(), 536, 0, 132097));
        put(new Function("GetMapName", Arrays.asList("Str", "Str", "Int"), 539, 0, 132097));
        put(new Function("FindBestInventoryPath", Collections.singletonList("Float"), 540, 0, 132097));
        put(new Function("ResetKeyboard", Collections.emptyList(), 544, 0, 132097));
        put(new Function("GetNextSkin", Arrays.asList("Str", "Str", "Int", "Str", "Str"), 545, 0, 132097));
        put(new Function("UpdateURL", Arrays.asList("Str", "Str", "Bool"), 546, 0, 132097));
        put(new Function("GetURLMap", Collections.emptyList(), 547, 0, 132097));
        put(new Function("FastTrace", Arrays.asList("Vector", "Vector"), 548, 0, 132097));
        put(new Function("-", Arrays.asList("Color", "Color"), 549, 20, 144385));
        put(new Function("*", Arrays.asList("Float", "Color"), 550, 16, 144385));
        put(new Function("+", Arrays.asList("Color", "Color"), 551, 20, 144385));
        put(new Function("*", Arrays.asList("Color", "Float"), 552, 16, 144385));
        put(new Function("MoveSmooth", Collections.singletonList("Vector"), 3969, 0, 132097));
        put(new Function("SetPhysics", Collections.singletonList("Byte"), 3970, 0, 132097));
        put(new Function("AutonomousPhysics", Collections.singletonList("Float"), 3971, 0, 132097));
    }

    private static void put(Function function) {
        nativeFunctions.put(function.getNativeIndex(), function);
    }

    public static Function fromExportEntry(UnrealPackage.ExportEntry entry) {
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
        String name = up.nameReference(input.readCompactInt());
        input.readCompactInt();
        input.readInt();
        input.readInt();
        int size = input.readInt();
        int readSize = 0;
        List<String> params = new ArrayList<>();
        while (readSize < size) {
            Token token = input.readObject(Token.class);

            if (token instanceof NativeParam) {
                params.add(getType(up.objectReference(((NativeParam) token).objRef)));
            }

            readSize += token.getSize(input.getContext());
        }
        return new Function(name, params, input.readUnsignedShort(), input.readUnsignedByte(), input.readInt());
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
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.skip(8);
                    dis.readCompactInt();
                    return "array<" + getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + ">";
                }
                case "ClassProperty": {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), null);
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.skip(8);
                    dis.readCompactInt();
                    return getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + "<" + getType(entry.getUnrealPackage().objectReference(dis.readCompactInt())) + ">";
                }
                case "ObjectProperty":
                case "StructProperty": {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), null);
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.readCompactInt();
                    dis.skip(8);
                    dis.readCompactInt();
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

    public enum Flag {
        /**
         * Function is final (prebindable, non-overridable function).
         */
        FINAL,
        /**
         * Function has been defined (not just declared).
         */
        DEFINED,
        /**
         * Function is an iterator.
         */
        ITERATOR,
        /**
         * Function is a latent state function.
         */
        LATENT,
        /**
         * Unary operator is a prefix operator.
         */
        PRE_OPERATOR,
        /**
         * Function cannot be reentered.
         */
        SINGULAR,
        /**
         * Function is network-replicated.
         */
        NET,
        /**
         * Function should be sent reliably on the network.
         */
        NET_RELIABLE,
        /**
         * Function executed on the client side.
         */
        SIMULATED,
        /**
         * Executable from command line.
         */
        EXEC,
        /**
         * Native function.
         */
        NATIVE,
        /**
         * Event function.
         */
        EVENT,
        /**
         * Operator function.
         */
        OPERATOR,
        /**
         * Static function.
         */
        STATIC,
        /**
         * Don't export intrinsic function to C++.
         */
        NO_EXPORT,
        /**
         * Function doesn't modify this object.
         */
        CONST,
        /**
         * Return value is purely dependent on parameters; no state dependencies or internal state changes.
         */
        INVARIANT,
        PROTECTED,
        Flag18,
        Flag19,
        /**
         * Function is a delegate
         */
        DELEGATE;

        private int mask = 1 << ordinal();

        public int getMask() {
            return mask;
        }

        @Override
        public String toString() {
            return "FF_" + name();
        }

        public static Collection<Flag> getFlags(int flags) {
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(Flag... flags) {
            int v = 0;
            for (Flag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }
}
