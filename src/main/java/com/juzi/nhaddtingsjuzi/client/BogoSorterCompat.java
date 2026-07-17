package com.juzi.nhaddtingsjuzi.client;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.inventory.Container;

public final class BogoSorterCompat {

    private static final String BOGO_API =
            "com.cleanroommc.bogosorter.api.IBogoSortAPI";
    private static final String SLOT_ACCESSOR =
            "com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor";
    private static final String LEGACY_CONTAINER =
            "com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer";
    private static final String LEGACY_BASE_SLOT =
            "com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot";
    private static final String MODULARUI2_CONTAINER =
            "com.cleanroommc.modularui.screen.ModularContainer";

    private BogoSorterCompat() {}

    public static void register() {
        try {
            Class<?> apiClass = Class.forName(BOGO_API);
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method addCompatSimple = apiClass.getMethod("addCompatSimple", Class.class, BiConsumer.class);
            BiConsumer<Container, Object> genericCompat = new BiConsumer<Container, Object>() {
                @Override
                public void accept(Container container, Object builder) {
                    addGenericSlotGroup(builder);
                }
            };

            registerContainerCompat(addCompatSimple, api, LEGACY_CONTAINER, genericCompat);
            registerContainerCompat(addCompatSimple, api, MODULARUI2_CONTAINER, genericCompat);
            registerLegacySlotGetter(apiClass, api);
        } catch (ReflectiveOperationException ignored) {
        } catch (LinkageError ignored) {
        }
    }

    private static void registerContainerCompat(Method addCompatSimple, Object api,
                                                String className,
                                                BiConsumer<Container, Object> compat)
            throws ReflectiveOperationException {
        Class<?> containerClass;
        try {
            containerClass = Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return;
        }
        addCompatSimple.invoke(api, containerClass, compat);
    }

    private static void registerLegacySlotGetter(Class<?> apiClass, Object api)
            throws ReflectiveOperationException {
        final Class<?> baseSlotClass;
        final Class<?> slotAccessorClass;
        try {
            baseSlotClass = Class.forName(LEGACY_BASE_SLOT);
            slotAccessorClass = Class.forName(SLOT_ACCESSOR);
        } catch (ClassNotFoundException ignored) {
            return;
        }

        Method addSlotGetter = apiClass.getMethod("addSlotGetter", Class.class, Function.class);
        addSlotGetter.invoke(api, baseSlotClass, new Function<Object, Object>() {
            @Override
            public Object apply(Object slot) {
                return adaptLegacySlot(slot, slotAccessorClass);
            }
        });
    }

    private static Object adaptLegacySlot(final Object slot, Class<?> slotAccessorClass) {
        final Object playerInventory = findPlayerInventory(slot);

        return Proxy.newProxyInstance(
                slotAccessorClass.getClassLoader(),
                new Class<?>[] { slotAccessorClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        if ("getInventory".equals(method.getName())) {
                            return playerInventory == null
                                    ? invokeSlotMethod(slot, method, args)
                                    : playerInventory;
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        return invokeSlotMethod(slot, method, args);
                    }
                });
    }

    private static Object invokeSlotMethod(Object slot, Method method, Object[] args)
            throws Throwable {
        try {
            return method.invoke(slot, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private static Object findPlayerInventory(Object slot) {
        try {
            Object handler = slot.getClass().getMethod("getItemHandler").invoke(slot);
            try {
                Object inventory = handler.getClass().getMethod("getInventoryPlayer").invoke(handler);
                if (inventory != null) {
                    return inventory;
                }
            } catch (NoSuchMethodException ignored) {
            }
            try {
                return handler.getClass().getMethod("getSourceInventory").invoke(handler);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void addGenericSlotGroup(Object builder) {
        try {
            builder.getClass().getMethod("addGenericSlotGroup").invoke(builder);
        } catch (ReflectiveOperationException ignored) {
        }
    }

}
