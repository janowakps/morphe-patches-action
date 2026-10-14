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
                // Secondary sort: lowest price first
                Collections.sort(copy, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        int d1 = getDiscount(o1);
                        int d2 = getDiscount(o2);
                        int cmp = Integer.compare(d2, d1);
                        if (cmp != 0) return cmp;
                        return getPrice(o1).compareTo(getPrice(o2));
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
            android.util.Log.i("ActionExtension", "addSortingChips called with lazyListScope=" + lazyListScope);
            ClassLoader cl = lazyListScope.getClass().getClassLoader();
            Class<?> hClass = cl.loadClass("h2.h");
            Class<?> pClass = cl.loadClass("yb0.p");
            Class<?> o00aClass = cl.loadClass("o00.a");
            Class<?> u3cClass = cl.loadClass("u3.c");
            final Class<?> zClass = cl.loadClass("jb0.z");
            final Object unitInstance = zClass.getField("a").get(null);

            final Object vm = viewModel;
            final ClassLoader loader = cl;

            Object composableBlock = Proxy.newProxyInstance(cl, new Class<?>[]{ pClass }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("invoke".equals(method.getName())) {
                        if (args != null && args.length >= 1 && args[0] != null) {
                            renderChips(args[0], vm, loader);
                        }
                        return unitInstance;
                    }
                    if ("toString".equals(method.getName())) {
                        return "SortingChipsBlock";
                    }
                    return null;
                }
            });

            // u3.c constructor is (Object content, boolean tracked, int key)
            Constructor<?> u3cCons = u3cClass.getConstructor(Object.class, boolean.class, int.class);
            Object composableLambda = u3cCons.newInstance(composableBlock, true, 133742);

            Method aMethod = o00aClass.getMethod("a", hClass, Object.class, Object.class, pClass);
            aMethod.invoke(null, lazyListScope, "action_sorting_chips", "action_sorting_chips", composableLambda);
            android.util.Log.i("ActionExtension", "addSortingChips successfully registered item into LazyListScope");
        } catch (Throwable t) {
            android.util.Log.e("ActionExtension", "Error in addSortingChips", t);
        }
    }

    /**
     * Composes the chips row inside the LazyListScope item.
     */
    private static void renderChips(Object composer, Object viewModel, ClassLoader cl) {
        try {
            android.util.Log.i("ActionExtension", "renderChips started");
            Class<?> g0Class = composer.getClass();
            Method d0Method = g0Class.getMethod("d0");
            Method lMethod = g0Class.getMethod("l", cl.loadClass("yb0.a"));
            Method m0Method = g0Class.getMethod("m0");
            Method qMethod = g0Class.getMethod("q", boolean.class);
            Method mMethod = g0Class.getMethod("m");
            Field sField = g0Class.getField("S");
            Field tField = g0Class.getField("T");

            // Row measure policy: e2.k2.a(e2.j.e, a4.b.j, composer, 0)
            Method kaMethod = cl.loadClass("e2.k2").getMethod("a",
                cl.loadClass("e2.g"),
                cl.loadClass("a4.d"),
                cl.loadClass("k3.j"),
                int.class
            );
            Object arrangementCenter = cl.loadClass("e2.j").getField("e").get(null);
            Object alignmentCenterVertically = cl.loadClass("a4.b").getField("j").get(null);
            Object measurePolicy = kaMethod.invoke(null, arrangementCenter, alignmentCenterVertically, composer, 0);

            // Setters in z4.g
            Class<?> z4gClass = cl.loadClass("z4.g");
            Object nodeConstructor = z4gClass.getField("b").get(null);
            Object measurePolicySetter = z4gClass.getField("f").get(null);
            Object densitySetter = z4gClass.getField("e").get(null);
            Object compositeKeyHashSetter = z4gClass.getField("g").get(null);
            Object onNodeSetter = z4gClass.getField("h").get(null);
            Object modifierSetter = z4gClass.getField("d").get(null);

            // Row Modifier: fillMaxWidth with 8dp vertical padding
            Class<?> pClass = cl.loadClass("a4.p");
            Object baseModifier = pClass.getField("b").get(null);

            Class<?> e2dClass = cl.loadClass("e2.d");
            Method padMethod = e2dClass.getMethod("E", cl.loadClass("a4.s"), float.class, float.class);

            Class<?> q2Class = cl.loadClass("e2.q2");
            Method fillMaxWidthMethod = q2Class.getMethod("d", cl.loadClass("a4.s"), float.class);
            Object fullWidthModifier = fillMaxWidthMethod.invoke(null, baseModifier, 1.0f);
            Object rowModifier = padMethod.invoke(null, fullWidthModifier, 0.0f, 8.0f);

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

            // Resolve palette colors (with dark mode defaults)
            long selectedBgColor = 0xFF5C9CFFL;   // darkBlue500
            long unselectedBgColor = 0xFF1E1E1EL; // neutral50
            long borderColor = 0xFF484848L;       // neutral300
            try {
                Object palette = cl.loadClass("ai.a").getMethod("a", cl.loadClass("k3.j")).invoke(null, composer);
                Class<?> paletteClass = palette.getClass();
                selectedBgColor = paletteClass.getField("l").getLong(palette);
                unselectedBgColor = paletteClass.getField("c").getLong(palette);
                borderColor = paletteClass.getField("d").getLong(palette);
            } catch (Throwable t) {
                android.util.Log.w("ActionExtension", "Failed to resolve palette colors, using fallbacks", t);
            }

            // Resolve shape and modifier methods
            Object circleShape = null;
            Method bgMethod = null;
            Method borderMethod = null;
            try {
                circleShape = cl.loadClass("n2.e").getField("a").get(null);
                Class<?> y1nClass = cl.loadClass("y1.n");
                bgMethod = y1nClass.getMethod("g", cl.loadClass("a4.s"), long.class, cl.loadClass("h4.t0"));
                borderMethod = y1nClass.getMethod("h", cl.loadClass("a4.s"), float.class, long.class, cl.loadClass("h4.t0"));
            } catch (Throwable t) {
                android.util.Log.w("ActionExtension", "Failed to resolve shape / bg / border methods", t);
            }

            // Method fz.ec.h(String, boolean, yb0.a, a4.s, k3.j, int)
            Method hMethod = cl.loadClass("fz.ec").getMethod("h",
                String.class,
                boolean.class,
                cl.loadClass("yb0.a"),
                cl.loadClass("a4.s"),
                cl.loadClass("k3.j"),
                int.class
            );

            // Render Chip 1: Default
            Object mod0 = buildChipModifier(sortMode == 0, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
            hMethod.invoke(null, "Domy\u015blnie", sortMode == 0, createOnClickProxy(0, viewModel, cl), mod0, composer, 0);

            // Render Chip 2: Price Ascending
            Object mod1 = buildChipModifier(sortMode == 1, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
            hMethod.invoke(null, "Cena \u2191", sortMode == 1, createOnClickProxy(1, viewModel, cl), mod1, composer, 0);

            // Render Chip 3: Discount Descending
            Object mod2 = buildChipModifier(sortMode == 2, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
            hMethod.invoke(null, "% Rabatu \u2193", sortMode == 2, createOnClickProxy(2, viewModel, cl), mod2, composer, 0);

            // End Row node
            qMethod.invoke(composer, true);
            android.util.Log.i("ActionExtension", "renderChips completed successfully");
        } catch (Throwable t) {
            android.util.Log.e("ActionExtension", "Error in renderChips", t);
        }
    }

    private static Object buildChipModifier(boolean isSelected, Object baseModifier, Method padMethod, Method bgMethod, Method borderMethod, Object circleShape, long selectedBg, long unselectedBg, long borderCol) {
        try {
            // 4dp outer horizontal margin between chips
            Object mod = padMethod.invoke(null, baseModifier, 4.0f, 0.0f);
            if (circleShape != null && bgMethod != null) {
                if (isSelected) {
                    // Filled pill background with primary brand color (darkBlue500)
                    mod = bgMethod.invoke(null, mod, selectedBg, circleShape);
                } else {
                    // Subtle container background with 1dp border
                    mod = bgMethod.invoke(null, mod, unselectedBg, circleShape);
                    if (borderMethod != null) {
                        mod = borderMethod.invoke(null, mod, 1.0f, borderCol, circleShape);
                    }
                }
            }
            return mod;
        } catch (Throwable t) {
            android.util.Log.w("ActionExtension", "Failed to build chip modifier", t);
            return baseModifier;
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
                    android.util.Log.i("ActionExtension", "Sorting chip clicked: mode=" + mode);
                    sortMode = mode;
                    if (viewModel != null) {
                        try {
                            Method bMethod = viewModel.getClass().getMethod("b");
                            bMethod.invoke(viewModel);
                        } catch (Throwable t) {
                            try {
                                for (Method m : viewModel.getClass().getMethods()) {
                                    if ("b".equals(m.getName()) && m.getParameterTypes().length == 0) {
                                        m.setAccessible(true);
                                        m.invoke(viewModel);
                                        break;
                                    }
                                }
                            } catch (Throwable t2) {
                                android.util.Log.e("ActionExtension", "Failed to trigger viewModel refresh", t2);
                            }
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
