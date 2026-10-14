package app.action.patches.promotions

import app.morphe.patcher.Fingerprint
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
        "Laq/f$c;",
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
