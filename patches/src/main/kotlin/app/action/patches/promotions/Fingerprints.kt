package app.action.patches.promotions

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/**
 * Fingerprint matching the product grid layout generator method in bq.a0.
 * Signature: bq.a0.d(List, ig.b, aq.f$c, aq.h, List)Llb0/c;
 * Takes product items and groups them into rows for the Compose grid.
 */
object ProductGridBuildFingerprint : Fingerprint(
    name = "d",
    returnType = "Llb0/c;",
    parameters = listOf(
        "Ljava/util/List;",
        "Lig/b;",
        "Laq/f\$c;",
        "Laq/h;",
        "Ljava/util/List;"
    )
)

/**
 * Fingerprint matching the LazyListScope content builder lambda in de.b.
 * Unique signature with parameter (Object) returning Object,
 * contains the string "weekDealsLeafletShortcut",
 * and matches the first invoke-interface instruction (Iterable.iterator).
 */
object WeekDealsListScopeFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("weekDealsLeafletShortcut"),
    filters = listOf(
        opcode(Opcode.INVOKE_INTERFACE)
    )
)

/**
 * Fingerprint matching the weekly deals product fetcher in zp.k.
 * Signature: zp.k.a(zp.k, int, ir.p, pb0.c)Ljava/lang/Object;
 * Contains two hardcoded limitPerPage constants of 24 (one for themed deals, one for standard deals).
 */
object WeeklyDealsLimitFingerprint : Fingerprint(
    name = "a",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Lzp/k;",
        "I",
        "Lir/p;",
        "Lpb0/c;"
    ),
    filters = listOf(
        literal(24L),
        literal(24L)
    )
)

/**
 * Fingerprint matching the product badge / sticker composable container in ur.h.
 * Signature: ur.h.f(long, a4.s, h4.w, u3.c, k3.j, int, int)V
 * Sets container height (22dp) and padding (5dp top/bottom), which clips descenders.
 */
object ProductBadgeFingerprint : Fingerprint(
    name = "f",
    returnType = "V",
    parameters = listOf(
        "J",
        "La4/s;",
        "Lh4/w;",
        "Lu3/c;",
        "Lk3/j;",
        "I",
        "I"
    ),
    filters = listOf(
        literal(22L),
        literal(5L)
    )
)

