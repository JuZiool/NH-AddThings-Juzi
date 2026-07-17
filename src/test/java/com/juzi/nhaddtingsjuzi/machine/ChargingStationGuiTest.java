package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

public class ChargingStationGuiTest {

    @Test
    public void statusTextStartsAtTopLeftBesideCircuitSlot() throws Exception {
        TextWidget statusText = MTEChargingStation.createStatusTextWidget(new Supplier<String>() {
            @Override
            public String get() {
                return "status";
            }
        });

        assertEquals(32, statusText.getPos().x);
        assertEquals(18, statusText.getPos().y);
        assertEquals("nh_addtings_juzi.charging_station", statusText.getInternalName());

        Field alignmentField = TextWidget.class.getDeclaredField("textAlignment");
        alignmentField.setAccessible(true);
        assertSame(Alignment.TopLeft, alignmentField.get(statusText));

        Field syncField = DynamicTextWidget.class.getDeclaredField("syncsToClient");
        syncField.setAccessible(true);
        assertEquals(false, syncField.get(statusText));
    }

    @Test
    public void circuitSlotDoesNotAcceptShiftInsert() {
        assertEquals(
                Integer.MIN_VALUE,
                ChargingStationUiSpec.CIRCUIT_SLOT_SHIFT_CLICK_PRIORITY);
    }

    @Test
    public void customWidgetsDoNotBindPlayerInventoryTwice() throws IOException {
        final AtomicInteger bindingCalls = new AtomicInteger();
        InputStream classBytes = MTEChargingStation.class.getResourceAsStream(
                "/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.class");

        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if (!"addUIWidgets".equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName,
                                                String methodDescriptor, boolean isInterface) {
                        if ("bindPlayerInventory".equals(methodName)) {
                            bindingCalls.incrementAndGet();
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertEquals(
                "GregTech already binds the player inventory before addUIWidgets",
                0,
                bindingCalls.get());
    }

    @Test
    public void statusFieldsUseTypedRawValueSyncers() throws IOException {
        final AtomicInteger syncerConstructors = new AtomicInteger();
        InputStream classBytes = MTEChargingStation.class.getResourceAsStream(
                "/com/juzi/nhaddtingsjuzi/machine/MTEChargingStation.class");

        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.NEW && type.startsWith(
                                "com/gtnewhorizons/modularui/common/widget/FakeSyncWidget$")) {
                            syncerConstructors.incrementAndGet();
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertEquals(10, syncerConstructors.get());
    }
}
