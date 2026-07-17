package com.juzi.nhaddtingsjuzi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BogoSorterRegistrationTest {

    @Test
    public void modEntryPointDoesNotRegisterGlobalBogoSorterCompatibility() throws Exception {
        InputStream classBytes = NHAddTingsJuzi.class.getResourceAsStream(
                "/com/juzi/nhaddtingsjuzi/NHAddTingsJuzi.class");
        assertNotNull(classBytes);

        final boolean[] hasPostInit = new boolean[1];
        final boolean[] referencesBogoSorter = new boolean[1];
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("postInit".equals(name)) {
                    hasPostInit[0] = true;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName,
                                                String methodDescriptor, boolean isInterface) {
                        if (owner.contains("BogoSorterCompat")) {
                            referencesBogoSorter[0] = true;
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertFalse("the mod entry point must not register a global sorter compatibility", hasPostInit[0]);
        assertFalse("the mod entry point must not call BogoSorterCompat", referencesBogoSorter[0]);
    }

    @Test
    public void globalBogoSorterCompatibilityClassIsRemoved() throws Exception {
        try {
            Class.forName(
                    "com.juzi.nhaddtingsjuzi.client.BogoSorterCompat",
                    false,
                    getClass().getClassLoader());
        } catch (ClassNotFoundException expected) {
            return;
        }
        throw new AssertionError("global Bogo Sorter compatibility class still exists");
    }
}
