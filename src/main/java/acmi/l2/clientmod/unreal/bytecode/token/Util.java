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

import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.unreal.UnrealRuntimeContext;
import acmi.l2.clientmod.unreal.core.*;
import acmi.l2.clientmod.unreal.core.Class;
import acmi.l2.clientmod.unreal.core.Field;
import acmi.l2.clientmod.unreal.core.Object;

import java.util.*;

public class Util {

    private static List<Runnable> tasks = new ArrayList<>();

//    public static void a(int offset, Token expression, UnrealPackage up) {
//        if (
//                expression instanceof ByteConst ||
//                        expression instanceof IntConstByte ||
//                        expression instanceof ConversionTable) {
//
//        } else if (expression instanceof VirtualFunction ||
//                expression instanceof FinalFunction ||
//                expression instanceof NativeFunctionCall) {
//
//        } else if (expression instanceof ArrayElement) {
//            ArrayElement ae = (ArrayElement) expression;
//            a(offset, ae.array, up);
//        } else if (expression instanceof DynArrayElement) {
//            DynArrayElement ae = (DynArrayElement) expression;
//            a(offset, ae.array, up);
//        } else if (expression instanceof LocalVariable) {
//            LocalVariable lv = (LocalVariable) expression;
//            UnrealPackage.Entry ref = up.objectReference(lv.objRef);
//            System.out.println(offset + "\t" + ref.getFullClassName() + "\t" + ref);
//        } else if (expression instanceof InstanceVariable) {
//            InstanceVariable iv = (InstanceVariable) expression;
//            UnrealPackage.Entry ref = up.objectReference(iv.objRef);
//            System.out.println(offset + "\t" + ref.getFullClassName() + "\t" + ref);
//        } else if (expression instanceof DefaultVariable) {
//            DefaultVariable iv = (DefaultVariable) expression;
//            UnrealPackage.Entry ref = up.objectReference(iv.objRef);
//            System.out.println(offset + "\t" + ref.getFullClassName() + "\t" + ref);
//        } else if (expression instanceof BoolVariable) {
//            BoolVariable lv = (BoolVariable) expression;
//            a(offset, lv.value, up);
//        } else if (expression instanceof StructMember) {
//            StructMember sm = (StructMember) expression;
//            UnrealPackage.Entry ref = up.objectReference(sm.objRef);
//            System.out.println(offset + "\t" + ref.getFullClassName() + "\t" + ref);
//        } else if (expression instanceof Context) {
//            Context cc = (Context) expression;
//            a(offset, cc.member, up);
//        } else if (expression instanceof ClassContext) {
//            ClassContext cc = (ClassContext) expression;
//            a(offset, cc.member, up);
//        } else {
//            throw new IllegalStateException(offset + "\t" + expression);
//        }
//    }

    public static void test(int offset, Token obj, Token expression, UnrealPackage.ExportEntry entry) {
        Collection<String> bp = new HashSet<>(Arrays.asList(
                "UIDATA_ITEM.GetInventoryType(item.Id.ClassID)",
                "rewardIDList[i]",
                "dScript.GetID()",
                "UICommonAPI.DialogGetID()"
        ));
        tasks.add(() -> {
            UnrealRuntimeContext context = new UnrealRuntimeContext(entry, Test.serializer);

            int size = getSize(expression, obj, false, context);


            String s = expression.toString(context);
            if (bp.contains(s)) {
                System.out.print("");
            }
            if (offset != size) {
                System.err.println(expression.toString(context) + "\t" + expression);
                System.err.println("\t" + offset + " != " + size);
            }
        });
    }

    public static void run() {
        tasks.forEach(Runnable::run);
    }

    public static int getSize(Token expression, Token obj, boolean array, UnrealRuntimeContext context) {
        UnrealPackage up = context.getUnrealPackage();

        if (expression instanceof ByteConst) {
            return 1;
        } else if (expression instanceof IntConstByte) {
            return 4;
        } else if (expression instanceof IntToByte) {
            return 1;
        } else if (expression instanceof ConversionTable) {
            ConversionTable ct = (ConversionTable) expression;
            return getSize(ct.inner, obj, array, context);
        } else if (expression instanceof ArrayElement) {
            ArrayElement ae = (ArrayElement) expression;
            return getSize(ae.array, obj, true, context);
        } else if (expression instanceof DynArrayElement) {
            DynArrayElement ae = (DynArrayElement) expression;
            return getSize(ae.array, obj, true, context);
        } else if (expression instanceof LocalVariable) {
            LocalVariable lv = (LocalVariable) expression;
            Property property = (Property) context.getSerializer().getOrCreateObject(up.objectReference(lv.objRef));
            return getSize(property, array, context);
        } else if (expression instanceof InstanceVariable) {
            InstanceVariable iv = (InstanceVariable) expression;
            Property property = (Property) context.getSerializer().getOrCreateObject(up.objectReference(iv.objRef));
            return getSize(property, array, context);
        } else if (expression instanceof DefaultVariable) {
            DefaultVariable iv = (DefaultVariable) expression;
            Property property = (Property) context.getSerializer().getOrCreateObject(up.objectReference(iv.objRef));
            return getSize(property, array, context);
        } else if (expression instanceof BoolVariable) {
            BoolVariable lv = (BoolVariable) expression;
            return getSize(lv.value, obj, array, context);
        } else if (expression instanceof StructMember) {
            StructMember sm = (StructMember) expression;
            Property property = (Property) context.getSerializer().getOrCreateObject(up.objectReference(sm.objRef));
            return getSize(property, array, context);
        } else if (expression instanceof Context) {
            Context cc = (Context) expression;
            return getSize(cc.member, obj, array, context);
        } else if (expression instanceof ClassContext) {
            ClassContext cc = (ClassContext) expression;
            return getSize(cc.member, obj, array, context);
        } else if (expression instanceof VirtualFunction) {
            VirtualFunction func = (VirtualFunction) expression;
            String name = context.getUnrealPackage().nameReference(func.nameRef);
            Class classObj = a(obj, context);
            Class classObjIter = classObj;
            while (classObjIter != null) {
                for (Field field : classObjIter) {
                    if (field instanceof Function && ((Function) field).entry.getObjectName().getName().equalsIgnoreCase(name)) {
                        return a((Function) field, false, context);
                    }
                }
                classObjIter = (Class) classObjIter.superField;
            }
            return 0;
//            throw new IllegalStateException("VirtualFunction '" + name + "' not found in " + classObj);
        } else if (expression instanceof FinalFunction) {
            FinalFunction func = (FinalFunction) expression;
            Function f = (Function) context.getSerializer().getOrCreateObject(context.getUnrealPackage().objectReference(func.funcRef));
            return a(f, array, context);
        } else if (expression instanceof NativeFunctionCall) {
            NativeFunctionCall func = (NativeFunctionCall) expression;

            Optional<Function> of = context.getSerializer().getNativeFunction(func.nativeIndex);
            if (of.isPresent()) {
                return a(of.get(), array, context);
            } else {
                throw new IllegalStateException("Native function not found: " + func.nativeIndex);
            }
        } else {
            throw new IllegalStateException("Size of " + expression);
        }
    }

    private static int a(Function f, boolean array, UnrealRuntimeContext context) {
        for (Field o : f) {
            if (o instanceof Property && o.entry.getObjectName().getName().equalsIgnoreCase("ReturnValue"))
                return getSize((Property) o, array, context);
        }

        return 0;
    }

    private static Class a(Token obj, UnrealRuntimeContext context) {
        if (obj instanceof ClassContext) {
            Token clazz = ((ClassContext) obj).clazz;
            if (clazz instanceof ObjectConst) {
                return (Class) context.getSerializer().getOrCreateObject(context.getUnrealPackage().objectReference(((ObjectConst) clazz).objRef));
            } else {
                throw new IllegalStateException("VirtualFunction of " + clazz.getClass());
            }
        } else if (obj instanceof Context) {
//            Token member = ((Context) obj).member;
//            return a(member, context);
            return a(((Context) obj).object, context);
        } else if (obj instanceof InstanceVariable) {
            return a(context.getSerializer().getOrCreateObject(context.getUnrealPackage().objectReference(((InstanceVariable) obj).objRef)));
        } else if (obj instanceof LocalVariable) {
            return a(context.getSerializer().getOrCreateObject(context.getUnrealPackage().objectReference(((LocalVariable) obj).objRef)));
        } else {
            return null;
//            throw new IllegalStateException("VirtualFunction of " + obj.getClass());
        }
    }

    private static Class a(Object objectObj) {
        if (objectObj instanceof ObjectProperty) {
            return ((ObjectProperty) objectObj).type;
        } else {
            throw new IllegalStateException("VirtualFunction of " + objectObj.getClass());
        }
    }

    public static int getSize(Property property, boolean arrayElement, UnrealRuntimeContext context) {
        int dim = arrayElement ? 1 : property.arrayDimension;
        if (property instanceof ByteProperty) {
            return dim;
        } else if (property instanceof IntProperty ||
                property instanceof BoolProperty ||
                property instanceof FloatProperty ||
                property instanceof ObjectProperty ||
                property instanceof NameProperty) {
            return dim * 4;
        } else if (property instanceof StrProperty) {
            return 0;
        } else if (property instanceof ArrayProperty) {
            return arrayElement ? getSize(((ArrayProperty) property).inner, true, context) : 0;
        } else if (property instanceof StructProperty) {
            StructProperty sp = (StructProperty) property;
            int size = 0;
            Struct struct = sp.struct;
            while (struct != null) {
                for (Field field : struct) {
                    if (field instanceof Property)
                        size += getSize((Property) field, false, context);
                }
                struct = (Struct) struct.superField;
            }
            while (size % 4 != 0)
                size++;
            return dim * size;
        }
        throw new IllegalStateException("Size of " + property.getClass().getSimpleName());
    }
}
