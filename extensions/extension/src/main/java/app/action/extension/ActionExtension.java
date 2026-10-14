package app.action.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({"unused", "unchecked"})
public final class ActionExtension {

    // 0: Default, 1: Price Ascending (Cena ↑), 2: Discount Descending (% Rabatu ↓)
    public static volatile int sortMode = 0;

    private ActionExtension() {}

    /**
     * Intercepts the product list before grid building in bq.a0.d.
     * Returns a new sorted list according to active sortMode.
     */
    public static List<?> sortProductList(List<?> originalList) {
        if (originalList == null || originalList.size() <= 1 || sortMode == 0) {
            return originalList;
        }

        try {
            List<Object> copy = new ArrayList<>(originalList);

            if (sortMode == 1) {
                // Price Ascending: lowest price first
                Collections.sort(copy, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        BigDecimal p1 = getPrice(o1);
                        BigDecimal p2 = getPrice(o2);
                        return p1.compareTo(p2);
                    }
                });
            } else if (sortMode == 2) {
                // Discount Descending: highest discount percentage first
                Collections.sort(copy, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        int d1 = getDiscount(o1);
                        int d2 = getDiscount(o2);
                        return Integer.compare(d2, d1);
                    }
                });
            }

            return copy;
        } catch (Throwable t) {
            t.printStackTrace();
            return originalList;
        }
    }

    /**
     * Injects the sorting chips row into the LazyListScope in de.b.invoke.
     */
    public static void addSortingChips(Object lazyListScope, Object viewModel) {
        if (lazyListScope == null) return;

        try {
            ClassLoader cl = lazyListScope.getClass().getClassLoader();
            Class<?> hClass = cl.loadClass("h2.h");
            Class<?> pClass = cl.loadClass("yb0.p");
            Class<?> o00aClass = cl.loadClass("o00.a");
            Class<?> u3cClass = cl.loadClass("u3.c");

            final Object vm = viewModel;
            final ClassLoader loader = cl;

            Object composableBlock = Proxy.newProxyInstance(cl, new Class<?>[]{ pClass }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("invoke".equals(method.getName())) {
                        if (args != null && args.length >= 1 && args[0] != null) {
                            renderChips(args[0], vm, loader);
                        }
                        return null;
                    }
                    if ("toString".equals(method.getName())) {
                        return "SortingChipsBlock";
                    }
                    return null;
                }
            });

            Constructor<?> u3cCons = u3cClass.getConstructor(int.class, boolean.class, Object.class);
            Object composableLambda = u3cCons.newInstance(133742, true, composableBlock);

            Method aMethod = o00aClass.getMethod("a", hClass, Object.class, Object.class, pClass);
            aMethod.invoke(null, lazyListScope, "action_sorting_chips", "action_sorting_chips", composableLambda);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Composes the chips row inside the LazyListScope item.
     */
    private static void renderChips(Object composer, Object viewModel, ClassLoader cl) {
        try {
            Class<?> g0Class = composer.getClass();
            Method d0Method = g0Class.getMethod("d0");
            Method lMethod = g0Class.getMethod("l", Object.class);
            Method m0Method = g0Class.getMethod("m0");
            Method qMethod = g0Class.getMethod("q", boolean.class);
            Method mMethod = g0Class.getMethod("m");
            Field sField = g0Class.getField("S");
            Field tField = g0Class.getField("T");

            // Row measure policy: e2.k2.a(e2.j.c, a4.b.j, composer, 0)
            Method k2a = cl.loadClass("e2.k2").getMethod("a",
                cl.loadClass("e2.g"),
                cl.loadClass("a4.d"),
                cl.loadClass("k3.j"),
                int.class
            );
            Object arrangementCenter = cl.loadClass("e2.j").getField("c").get(null);
            Object alignmentCenter = cl.loadClass("a4.b").getField("j").get(null);
            Object measurePolicy = k2a.invoke(null, arrangementCenter, alignmentCenter, composer, 0);

            // Setters in z4.g
            Class<?> z4gClass = cl.loadClass("z4.g");
            Object nodeConstructor = z4gClass.getField("b").get(null);
            Object measurePolicySetter = z4gClass.getField("f").get(null);
            Object densitySetter = z4gClass.getField("e").get(null);
            Object compositeKeyHashSetter = z4gClass.getField("g").get(null);
            Object onNodeSetter = z4gClass.getField("h").get(null);
            Object modifierSetter = z4gClass.getField("d").get(null);

            // Row Modifier: a4.p.b with padding
            Class<?> pClass = cl.loadClass("a4.p");
            Object baseModifier = pClass.getField("b").get(null);

            Class<?> e2dClass = cl.loadClass("e2.d");
            Method padMethod = e2dClass.getMethod("E", cl.loadClass("a4.s"), float.class, float.class);
            // 8dp horizontal, 4dp vertical on the row container
            Object rowModifier = padMethod.invoke(null, baseModifier, 8.0f, 4.0f);

            Method materializeMethod = cl.loadClass("a4.a").getMethod("c", cl.loadClass("a4.s"), cl.loadClass("k3.j"));
            Object materializedModifier = materializeMethod.invoke(null, rowModifier, composer);

            // Layout emission
            long hash = tField.getLong(composer);
            int hashCode = Long.hashCode(hash);
            Object locals = mMethod.invoke(composer);

            d0Method.invoke(composer);
            boolean inserting = sField.getBoolean(composer);
            if (inserting) {
                lMethod.invoke(composer, nodeConstructor);
            } else {
                m0Method.invoke(composer);
            }

            Class<?> kpClass = cl.loadClass("k3.p");
            Method cMethod = kpClass.getMethod("C", Object.class, cl.loadClass("k3.j"), cl.loadClass("yb0.p"));
            Method yMethod = kpClass.getMethod("y", cl.loadClass("k3.j"), cl.loadClass("yb0.l"));

            cMethod.invoke(null, measurePolicy, composer, measurePolicySetter);
            cMethod.invoke(null, locals, composer, densitySetter);
            cMethod.invoke(null, Integer.valueOf(hashCode), composer, compositeKeyHashSetter);
            yMethod.invoke(null, composer, onNodeSetter);
            cMethod.invoke(null, materializedModifier, composer, modifierSetter);

            // Render each chip using fz.ec.h(String, boolean, yb0.a, a4.s, k3.j, int)
            Method hMethod = cl.loadClass("fz.ec").getMethod("h",
                String.class,
                boolean.class,
                cl.loadClass("yb0.a"),
                cl.loadClass("a4.s"),
                cl.loadClass("k3.j"),
                int.class
            );

            // Individual chip padding: 4dp horizontal
            Object chipModifier = padMethod.invoke(null, baseModifier, 4.0f, 0.0f);

            // Chip 1: Default
            hMethod.invoke(null, "Domyślnie", sortMode == 0, createOnClickProxy(0, viewModel, cl), chipModifier, composer, 0);

            // Chip 2: Price Ascending
            hMethod.invoke(null, "Cena ↑", sortMode == 1, createOnClickProxy(1, viewModel, cl), chipModifier, composer, 0);

            // Chip 3: Discount Descending
            hMethod.invoke(null, "% Rabatu ↓", sortMode == 2, createOnClickProxy(2, viewModel, cl), chipModifier, composer, 0);

            // End Row node
            qMethod.invoke(composer, true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Object createOnClickProxy(final int mode, final Object viewModel, ClassLoader cl) throws Exception {
        Class<?> aClass = cl.loadClass("yb0.a");
        final Class<?> zClass = cl.loadClass("jb0.z");
        final Object unitInstance = zClass.getField("a").get(null);

        return Proxy.newProxyInstance(cl, new Class<?>[]{ aClass }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("invoke".equals(method.getName())) {
                    sortMode = mode;
                    if (viewModel != null) {
                        try {
                            Method bMethod = viewModel.getClass().getMethod("b");
                            bMethod.invoke(viewModel);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    return unitInstance;
                }
                if ("toString".equals(method.getName())) {
                    return "SortChipOnClick(" + mode + ")";
                }
                if ("hashCode".equals(method.getName())) {
                    return Integer.valueOf(mode);
                }
                if ("equals".equals(method.getName())) {
                    return Boolean.valueOf(proxy == (args != null && args.length > 0 ? args[0] : null));
                }
                return null;
            }
        });
    }

    private static BigDecimal getPrice(Object item) {
        if (item == null) return BigDecimal.valueOf(Double.MAX_VALUE);

        try {
            // Fast path: item.m (vr.b) -> field a (BigDecimal)
            Field mField = item.getClass().getDeclaredField("m");
            mField.setAccessible(true);
            Object bObj = mField.get(item);
            if (bObj != null) {
                Field aField = bObj.getClass().getDeclaredField("a");
                aField.setAccessible(true);
                Object priceObj = aField.get(bObj);
                if (priceObj instanceof BigDecimal) {
                    return (BigDecimal) priceObj;
                }
            }
        } catch (Throwable ignored) {
            // Fallback: search all fields for BigDecimal
            try {
                for (Field f : item.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(item);
                    if (val instanceof BigDecimal) {
                        return (BigDecimal) val;
                    }
                    if (val != null) {
                        for (Field inner : val.getClass().getDeclaredFields()) {
                            inner.setAccessible(true);
                            Object innerVal = inner.get(val);
                            if (innerVal instanceof BigDecimal) {
                                return (BigDecimal) innerVal;
                            }
                        }
                    }
                }
            } catch (Throwable ignored2) {}
        }

        return BigDecimal.valueOf(Double.MAX_VALUE);
    }

    private static int getDiscount(Object item) {
        if (item == null) return 0;

        try {
            // Fast path 1: item.v (Integer)
            Field vField = item.getClass().getDeclaredField("v");
            vField.setAccessible(true);
            Object vObj = vField.get(item);
            if (vObj instanceof Integer) {
                int val = ((Integer) vObj).intValue();
                if (val > 0) return val;
            }
        } catch (Throwable ignored) {}

        try {
            // Fast path 2: item.m (vr.b) -> field j (String, e.g. "-25%")
            Field mField = item.getClass().getDeclaredField("m");
            mField.setAccessible(true);
            Object bObj = mField.get(item);
            if (bObj != null) {
                Field jField = bObj.getClass().getDeclaredField("j");
                jField.setAccessible(true);
                Object jObj = jField.get(bObj);
                if (jObj instanceof String) {
                    int parsed = parsePercent((String) jObj);
                    if (parsed > 0) return parsed;
                }
            }
        } catch (Throwable ignored) {}

        // Fallback: search for any percentage string
        try {
            for (Field f : item.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(item);
                if (val instanceof String && ((String) val).contains("%")) {
                    int parsed = parsePercent((String) val);
                    if (parsed > 0) return parsed;
                }
            }
        } catch (Throwable ignored) {}

        return 0;
    }

    private static int parsePercent(String str) {
        if (str == null) return 0;
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (Character.isDigit(c)) {
                    sb.append(c);
                }
            }
            if (sb.length() > 0) {
                return Integer.parseInt(sb.toString());
            }
        } catch (Throwable ignored) {}
        return 0;
    }
}
