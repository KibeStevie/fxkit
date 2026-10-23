package dev.fxkit.core;

import java.lang.reflect.Field;

/**
 * Reads a private field of a component. Used sparingly, only where the thing under test (for example
 * a dropdown's popup window) has no public accessor. Works because tests live in the component's own
 * module.
 */
public final class TestReflection {

    private TestReflection() {
        // static utility class, not meant to be instantiated
    }

    public static <T> T field(Object target, String name, Class<T> type) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return type.cast(field.get(target));
            } catch (NoSuchFieldException notHere) {
                // try the superclass
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("no field " + name + " in " + target.getClass());
    }
}
