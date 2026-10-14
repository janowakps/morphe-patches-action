package app.action.patches.promotions

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/action/extension/ActionExtension;"

@Suppress("unused")
val actionPromotionSortPatch = bytecodePatch(
    name = "Weekly Deals Sorting",
    description = "Adds bidirectional sorting options with interactive chips for Weekly Deals, keeps native filtering/sorting untouched on 'New' and category pages, increases initial deals page limit to 120, and fixes badge label clipping.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    extendWith("extensions/extension.mpe")

    execute {
        // Hook 0: Track active grid context in zp.d.Z to isolate Weekly Deals from 'New'/Search/Categories
        // Parameters: p1 (v20) = LazyListScope, p2 (v21) = gridName (String), p3 (v22) = presenter, p4 (v23) = gridState (zp.f)
        val gridScopeMethod = ProductGridScopeFingerprint.method
        gridScopeMethod.addInstructions(
            0,
            """
                invoke-static/range {p2 .. p4}, $EXTENSION_CLASS->setCurrentGridContext(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
            """
        )

        // Hook 1: Product grid sorting at bq.a0.d entry
        // Parameter p0 is register v20 (> 15), so Dalvik format 35c (invoke-static) cannot encode it.
        // Format 3rc (invoke-static/range) must be used.
        val gridMethod = ProductGridBuildFingerprint.method
        gridMethod.addInstructions(
            0,
            """
                invoke-static/range {p0 .. p0}, $EXTENSION_CLASS->sortProductList(Ljava/util/List;)Ljava/util/List;
                move-result-object p0
            """
        )

        // Hook 2: Injects sorting chips row into LazyListScope in de.b.invoke
        val listScopeMethod = WeekDealsListScopeFingerprint.method
        val iteratorMatch = WeekDealsListScopeFingerprint.instructionMatches.first()
        val iteratorIndex = iteratorMatch.index

        // Inject call to addSortingChips before the iterator loop.
        // Register v13 holds LazyListScope (h2.h), v7 holds WeekDealsViewModel (yr.c).
        listScopeMethod.addInstructions(
            iteratorIndex,
            """
                invoke-static {v13, v7}, $EXTENSION_CLASS->addSortingChips(Ljava/lang/Object;Ljava/lang/Object;)V
            """
        )

        // Move any branch target labels from the iterator instruction to the newly inserted
        // addSortingChips instruction so that both leaflet-enabled and leaflet-disabled paths execute it.
        try {
            val newInstruction = listScopeMethod.getInstruction(iteratorIndex)
            val shiftedInstruction = listScopeMethod.getInstruction(iteratorIndex + 1)
            val labels = shiftedInstruction.location.labels.toList()
            if (labels.isNotEmpty()) {
                shiftedInstruction.location.labels.clear()
                newInstruction.location.labels.addAll(labels)
            }
        } catch (ignored: Throwable) {
            // Fallback: instruction is already injected at iteratorIndex
        }

        // Hook 3: Dynamic page limit in zp.k.a (WeeklyDealsLimitFingerprint)
        // Uses ActionExtension.onProductFetch at method entry to detect whether the active grid
        // is Weekly Deals vs 'New'/Categories, and getPageLimit to return 120 for Weekly Deals
        // (fetching all deals in a single request) and 24 for 'New'/Categories (preserving native pagination).
        val limitMethod = WeeklyDealsLimitFingerprint.method
        val limitMatches = WeeklyDealsLimitFingerprint.instructionMatches

        limitMethod.addInstructions(
            0,
            """
                invoke-static {p0}, $EXTENSION_CLASS->onProductFetch(Ljava/lang/Object;)V
            """
        )

        if (limitMatches.size >= 2) {
            val idx0 = limitMatches[0].index + 1
            limitMethod.replaceInstruction(idx0, "invoke-static {}, $EXTENSION_CLASS->getPageLimit()I")
            limitMethod.addInstructions(idx0 + 1, "move-result v3")

            val idx1 = limitMatches[1].index + 2
            limitMethod.replaceInstruction(idx1, "invoke-static {}, $EXTENSION_CLASS->getPageLimit()I")
            limitMethod.addInstructions(idx1 + 1, "move-result v3")
        }

        // Hook 4: Fix "Promocja tygodnia" badge vertical truncation in ur.h.f
        // Increases container height from 22dp to 24dp and decreases vertical padding from 5dp to 2dp
        // so that descenders ('p', 'g', 'y', 'j') fit without clipping.
        val badgeMethod = ProductBadgeFingerprint.method
        val badgeMatches = ProductBadgeFingerprint.instructionMatches
        if (badgeMatches.size >= 2) {
            badgeMethod.replaceInstruction(badgeMatches[0].index, "const/16 v11, 24")
            badgeMethod.replaceInstruction(badgeMatches[1].index, "const/4 v11, 2")
        }
    }
}

