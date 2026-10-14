package app.action.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "unchecked"})
public final class ActionExtension {

    // 0: Default/Relevance, 1: Price Ascending (Price ↑), 2: Discount Descending (% ↓), 3: Price Descending (Price ↓)
    public static volatile int sortMode = 0;

    public static volatile Object currentViewModel = null;
    public static volatile boolean isWeekDealsActive = false;
    public static volatile String activeGridName = null;
    public static volatile Object currentGridPresenter = null;

    private static final String PREF_NAME = "action_morphe_prefs";
    private static final String KEY_SORT_MODE = "weekly_deals_sort_mode";
    private static final String KEY_HAS_DISCOUNTS = "weekly_deals_has_discounts";
    private static volatile boolean prefsLoaded = false;
    public static volatile boolean hasDiscounts = true;

    private static volatile String cachedRelevanceLabel = null;
    private static volatile String cachedPriceLabelBase = null;

    private static final AtomicBoolean autoLoadScheduled = new AtomicBoolean(false);
    private static volatile int lastLoadedSize = -1;
    private static volatile int sameSizeRetryCount = 0;

    private ActionExtension() {}

    private static Context getAppContext() {
        try {
            Class<?> atClass = Class.forName("android.app.ActivityThread");
            Method currentAppMethod = atClass.getMethod("currentApplication");
            Object app = currentAppMethod.invoke(null);
            if (app instanceof Context) {
                return (Context) app;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void loadSavedSortMode() {
        if (prefsLoaded) return;
        try {
            Context ctx = getAppContext();
            if (ctx != null) {
                SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sortMode = sp.getInt(KEY_SORT_MODE, 0);
                hasDiscounts = sp.getBoolean(KEY_HAS_DISCOUNTS, true);
                prefsLoaded = true;
                Log.i("ActionExtension", "Loaded saved sortMode: " + sortMode + ", hasDiscounts: " + hasDiscounts);
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to load saved sortMode", t);
        }
    }

    public static void saveSortMode(int mode) {
        try {
            Context ctx = getAppContext();
            if (ctx != null) {
                SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().putInt(KEY_SORT_MODE, mode).apply();
                Log.i("ActionExtension", "Saved sortMode: " + mode);
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to save sortMode", t);
        }
    }

    public static void saveHasDiscounts(boolean has) {
        try {
            Context ctx = getAppContext();
            if (ctx != null) {
                SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().putBoolean(KEY_HAS_DISCOUNTS, has).apply();
                Log.i("ActionExtension", "Saved hasDiscounts: " + has);
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to save hasDiscounts", t);
        }
    }

    private static String getRelevanceLabel() {
        if (cachedRelevanceLabel != null) return cachedRelevanceLabel;
        try {
            Context ctx = getAppContext();
            if (ctx != null) {
                int resId = ctx.getResources().getIdentifier("filter_and_sort_sorting_relevance", "string", ctx.getPackageName());
                if (resId != 0) {
                    String s = ctx.getString(resId);
                    if (s != null && !s.trim().isEmpty()) {
                        cachedRelevanceLabel = s;
                        return s;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to resolve filter_and_sort_sorting_relevance", t);
        }
        return "Recommended";
    }

    private static String getPriceLabelBase() {
        if (cachedPriceLabelBase != null) return cachedPriceLabelBase;
        try {
            Context ctx = getAppContext();
            if (ctx != null) {
                int resId = ctx.getResources().getIdentifier("filter_and_sort_price_title", "string", ctx.getPackageName());
                if (resId != 0) {
                    String s = ctx.getString(resId);
                    if (s != null && !s.trim().isEmpty()) {
                        cachedPriceLabelBase = s;
                        return s;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to resolve filter_and_sort_price_title", t);
        }
        return "Price";
    }

    /**
     * Inspects the presenter (zp.k) or any component to determine if it belongs to Weekly Deals.
     * In the Action app, Weekly Deals presenter zp.k contains f55447c (el.n) -> f17676a (el.k),
     * where trackingName is "Weekdeals" or "NextWeekdeals", and data contains "plp_weekdeals".
     */
    public static boolean isWeeklyDealsPresenter(Object presenter) {
        if (presenter == null) return false;
        try {
            String pStr = presenter.toString();
            if (pStr != null) {
                String lower = pStr.toLowerCase();
                if (lower.contains("weekdeal") || lower.contains("plp_weekdeals")) {
                    return true;
                }
            }

            for (Field f : presenter.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(presenter);
                if (val != null) {
                    String vStr = val.toString();
                    if (vStr != null) {
                        String lower = vStr.toLowerCase();
                        if (lower.contains("weekdeal") || lower.contains("plp_weekdeals")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Error inspecting presenter", t);
        }
        return false;
    }

    /**
     * Intercepts product fetch start at zp.k.a entry to immediately set the grid context
     * before page limits or GraphQL requests are made.
     */
    public static void onProductFetch(Object presenter) {
        boolean isDeals = isWeeklyDealsPresenter(presenter);
        isWeekDealsActive = isDeals;
        Log.i("ActionExtension", "onProductFetch: isWeeklyDeals=" + isDeals);
    }

    /**
     * Dynamically supplies page limit for zp.k.a queries:
     * - Returns 120 for Weekly Deals to load all deals in a single request.
     * - Returns 24 for 'New', Search, and Category pages to preserve continuous native pagination without skipped items.
     */
    public static int getPageLimit() {
        if (isWeekDealsActive) {
            return 120;
        }
        return 24;
    }

    /**
     * Intercepts the grid context in zp.d.Z to distinguish between Weekly Deals and
     * screens with native sorting/filtering (e.g. 'New', Search, Categories).
     */
    public static void setCurrentGridContext(String str, Object presenter, Object gridState) {
        activeGridName = str;
        currentGridPresenter = presenter;

        boolean isDeals = isWeeklyDealsPresenter(presenter);

        if (!isDeals && str != null) {
            String lower = str.toLowerCase();
            if (lower.contains("weekdeal") || lower.contains("plp_weekdeals")) {
                isDeals = true;
            }
        }

        if (!isDeals && currentViewModel != null) {
            String vmName = currentViewModel.getClass().getName();
            if (vmName.contains("WeekDealsViewModel") || vmName.contains("weekdeals")) {
                isDeals = true;
            }
        }

        // Also verify whether filters are hidden on this gridState
        if (gridState != null) {
            try {
                // zp.f -> field g is boolean hideFilters
                Field gField = gridState.getClass().getDeclaredField("g");
                gField.setAccessible(true);
                boolean hideFilters = gField.getBoolean(gridState);
                if (!hideFilters) {
                    // This grid has its own native filter/sort button (like 'New' or Search)!
                    // Under NO circumstance should we treat it as Weekly Deals.
                    isDeals = false;
                }
            } catch (Throwable ignored) {}
        }

        isWeekDealsActive = isDeals;
        if (!isDeals) {
            currentViewModel = null;
        }
        Log.i("ActionExtension", "setCurrentGridContext: str=" + str + ", isDeals=" + isDeals + ", isWeekDealsActive=" + isWeekDealsActive);
    }

    /**
     * Intercepts the product list before grid building in bq.a0.d.
     * Returns a new sorted list according to active sortMode.
     * When sorting is active, automatically schedules loading of remaining pages.
     */
    public static List<?> sortProductList(List<?> originalList) {
        if (originalList == null || originalList.size() <= 1) {
            return originalList;
        }

        // CRITICAL: Only sort if the currently active grid is Weekly Deals!
        // Never override native sorting or filters on 'New', Search, or Category pages.
        if (!isWeekDealsActive) {
            return originalList;
        }

        // Evaluate whether any product in this list has a discount percentage
        boolean anyDiscount = false;
        for (Object item : originalList) {
            if (getDiscount(item) > 0) {
                anyDiscount = true;
                break;
            }
        }

        if (hasDiscounts != anyDiscount) {
            hasDiscounts = anyDiscount;
            saveHasDiscounts(anyDiscount);
            if (currentViewModel != null) {
                try {
                    Method bMethod = currentViewModel.getClass().getMethod("b");
                    bMethod.invoke(currentViewModel);
                } catch (Throwable ignored) {}
            }
        }

        loadSavedSortMode();

        // If discounts are not available on this market/grid, reset sortMode from 2 (% discount) to 0 (default)
        if (!hasDiscounts && sortMode == 2) {
            sortMode = 0;
            saveSortMode(0);
        }

        if (sortMode == 0) {
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
            } else if (sortMode == 3) {
                // Price Descending: highest price first
                Collections.sort(copy, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        BigDecimal p1 = getPrice(o1);
                        BigDecimal p2 = getPrice(o2);
                        return p2.compareTo(p1);
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

            // Check if there are more products to auto-load
            scheduleAutoLoadIfNecessary(copy.size());

            return copy;
        } catch (Throwable t) {
            Log.e("ActionExtension", "Error in sortProductList", t);
            return originalList;
        }
    }

    /**
     * Automatically triggers loading more products if the active catalog has more pages.
     */
    public static void scheduleAutoLoadIfNecessary(final int currentSize) {
        if (!isWeekDealsActive || sortMode == 0 || currentViewModel == null) {
            return;
        }

        if (autoLoadScheduled.compareAndSet(false, true)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Object vm = currentViewModel;
                        if (!isWeekDealsActive || vm == null || sortMode == 0) {
                            return;
                        }

                        Object presenter = findActivePresenter(vm);
                        if (presenter != null && hasMoreProducts(presenter, currentSize)) {
                            if (currentSize == lastLoadedSize) {
                                sameSizeRetryCount++;
                            } else {
                                lastLoadedSize = currentSize;
                                sameSizeRetryCount = 0;
                            }

                            if (sameSizeRetryCount < 3) {
                                triggerPresenterLoadMore(presenter);
                            } else {
                                Log.w("ActionExtension", "Max retries reached for size " + currentSize + ", stopping auto-load");
                            }
                        }
                    } catch (Throwable t) {
                        Log.e("ActionExtension", "Error in auto-load run", t);
                    } finally {
                        autoLoadScheduled.set(false);
                    }
                }
            }, 150);
        }
    }

    /**
     * Resolves the active presenter (zp.k) from WeekDealsViewModel for the selected tab.
     */
    public static Object findActivePresenter(Object viewModel) {
        if (viewModel == null) return null;
        try {
            // Step 1: Check if active tab is NEXT_WEEKDEALS
            boolean isNext = false;
            try {
                for (Field f : viewModel.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(viewModel);
                    if (val != null) {
                        try {
                            Method getValue = val.getClass().getMethod("getValue");
                            Object v = getValue.invoke(val);
                            if (v != null && v.toString().contains("NEXT_WEEKDEALS")) {
                                isNext = true;
                                break;
                            }
                        } catch (NoSuchMethodException ignored) {}
                    }
                }
            } catch (Throwable t) {
                Log.w("ActionExtension", "Failed to check active tab enum", t);
            }

            // Step 2: Collect resolvers (dl.c)
            List<Object> resolvers = new ArrayList<>();
            for (Field f : viewModel.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(viewModel);
                if (val != null && "dl.c".equals(val.getClass().getName())) {
                    resolvers.add(val);
                }
            }

            List<Object> candidateResolvers = new ArrayList<>();
            if (isNext && resolvers.size() > 1) {
                candidateResolvers.add(resolvers.get(1));
                candidateResolvers.add(resolvers.get(0));
            } else {
                candidateResolvers.addAll(resolvers);
            }

            // Step 3: From candidate resolvers, find the presenter having method J0()
            for (Object resolver : candidateResolvers) {
                for (Field f : resolver.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(resolver);
                    if (val instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) val;
                        for (Object entry : map.values()) {
                            if (entry == null) continue;
                            for (Field ef : entry.getClass().getDeclaredFields()) {
                                ef.setAccessible(true);
                                Object compObj = ef.get(entry);
                                if (compObj != null) {
                                    try {
                                        compObj.getClass().getMethod("J0");
                                        return compObj;
                                    } catch (NoSuchMethodException ignored) {}
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e("ActionExtension", "Error finding active presenter", t);
        }
        return null;
    }

    /**
     * Checks if the presenter still has unreached products.
     */
    public static boolean hasMoreProducts(Object presenter, int currentSize) {
        if (presenter == null) return false;
        try {
            // Check if already loading
            for (Field f : presenter.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(presenter);
                if (val != null) {
                    try {
                        Method getValue = val.getClass().getMethod("getValue");
                        Object v = getValue.invoke(val);
                        if (v instanceof Boolean && ((Boolean) v).booleanValue()) {
                            // Currently loading
                            return false;
                        }
                    } catch (NoSuchMethodException ignored) {}
                }
            }

            // Check kr.b state via d()
            Method dMethod = null;
            try {
                dMethod = presenter.getClass().getMethod("d");
            } catch (NoSuchMethodException ignored) {}

            if (dMethod != null) {
                Object state = dMethod.invoke(presenter);
                if (state != null) {
                    List<Field> intFields = new ArrayList<>();
                    for (Field f : state.getClass().getDeclaredFields()) {
                        if (f.getType() == int.class) {
                            f.setAccessible(true);
                            intFields.add(f);
                        }
                    }
                    if (intFields.size() >= 3) {
                        int totalCount = intFields.get(0).getInt(state);
                        int currentPage = intFields.get(1).getInt(state);
                        int totalPages = intFields.get(2).getInt(state);

                        Log.i("ActionExtension", "Pagination check: currentSize=" + currentSize +
                                ", totalCount=" + totalCount + ", currentPage=" + currentPage + ", totalPages=" + totalPages);

                        if (totalCount > 0 && currentSize < totalCount) {
                            return true;
                        }
                        if (totalPages > 1 && currentPage < totalPages - 1) {
                            return true;
                        }
                        return false;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w("ActionExtension", "Failed to check hasMoreProducts", t);
        }
        return false;
    }

    /**
     * Invokes J0() on the presenter to fetch the next page of products.
     */
    public static void triggerPresenterLoadMore(Object presenter) {
        if (presenter == null) return;
        try {
            Log.i("ActionExtension", "Invoking presenter.J0()");
            Method j0 = presenter.getClass().getMethod("J0");
            j0.invoke(presenter);
        } catch (Throwable t) {
            try {
                for (Method m : presenter.getClass().getMethods()) {
                    if ("J0".equals(m.getName()) && m.getParameterTypes().length == 0) {
                        m.setAccessible(true);
                        m.invoke(presenter);
                        return;
                    }
                }
            } catch (Throwable t2) {
                Log.e("ActionExtension", "Failed to invoke J0 on presenter", t2);
            }
        }
    }

    /**
     * Injects the sorting chips row into the LazyListScope in de.b.invoke.
     */
    public static void addSortingChips(Object lazyListScope, Object viewModel) {
        if (lazyListScope == null) return;
        currentViewModel = viewModel;
        isWeekDealsActive = true;

        try {
            Log.i("ActionExtension", "addSortingChips called with lazyListScope=" + lazyListScope);
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
            Log.i("ActionExtension", "addSortingChips successfully registered item into LazyListScope");
        } catch (Throwable t) {
            Log.e("ActionExtension", "Error in addSortingChips", t);
        }
    }

    /**
     * Composes the chips row inside the LazyListScope item.
     */
    private static void renderChips(Object composer, Object viewModel, ClassLoader cl) {
        try {
            Log.i("ActionExtension", "renderChips started");
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
                Log.w("ActionExtension", "Failed to resolve palette colors, using fallbacks", t);
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
                Log.w("ActionExtension", "Failed to resolve shape / bg / border methods", t);
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

            loadSavedSortMode();

            // Render Chip 1: Relevance (native Action string e.g. "Polecane", "Empfohlen", "Aanbevolen")
            String relLabel = getRelevanceLabel();
            Object mod0 = buildChipModifier(sortMode == 0, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
            hMethod.invoke(null, relLabel, sortMode == 0, createOnClickProxy(0, viewModel, cl), mod0, composer, 0);

            // Render Chip 2: Price (Bidirectional with native Action string e.g. "Cena ↑", "Preis ↓")
            boolean isPriceSelected = (sortMode == 1 || sortMode == 3);
            String priceBase = getPriceLabelBase();
            String priceLabel = (sortMode == 3) ? (priceBase + " \u2193") : (priceBase + " \u2191");
            Object mod1 = buildChipModifier(isPriceSelected, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
            hMethod.invoke(null, priceLabel, isPriceSelected, createOnClickProxy(1, viewModel, cl), mod1, composer, 0);

            // Render Chip 3: Discount Descending (% ↓)
            // Dynamically hidden if discounts are not present/applicable on this market (e.g. CZ, DE)
            if (hasDiscounts) {
                Object mod2 = buildChipModifier(sortMode == 2, baseModifier, padMethod, bgMethod, borderMethod, circleShape, selectedBgColor, unselectedBgColor, borderColor);
                hMethod.invoke(null, "% \u2193", sortMode == 2, createOnClickProxy(2, viewModel, cl), mod2, composer, 0);
            }

            // End Row node
            qMethod.invoke(composer, true);
            Log.i("ActionExtension", "renderChips completed successfully");
        } catch (Throwable t) {
            Log.e("ActionExtension", "Error in renderChips", t);
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
            Log.w("ActionExtension", "Failed to build chip modifier", t);
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
                    int targetMode = mode;
                    if (mode == 1) {
                        // Toggle between Price Ascending (1) and Price Descending (3)
                        if (sortMode == 1) {
                            targetMode = 3;
                        } else {
                            targetMode = 1;
                        }
                    }

                    Log.i("ActionExtension", "Sorting chip clicked: targetMode=" + targetMode + " (prev sortMode=" + sortMode + ")");
                    isWeekDealsActive = true;
                    sortMode = targetMode;
                    saveSortMode(targetMode);
                    lastLoadedSize = -1;
                    sameSizeRetryCount = 0;

                    if (targetMode != 0 && viewModel != null) {
                        Object presenter = findActivePresenter(viewModel);
                        if (presenter != null && hasMoreProducts(presenter, 0)) {
                            triggerPresenterLoadMore(presenter);
                        }
                    }

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
                                Log.e("ActionExtension", "Failed to trigger viewModel refresh", t2);
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



