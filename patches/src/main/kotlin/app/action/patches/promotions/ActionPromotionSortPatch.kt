package app.action.patches.promotions

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/action/extension/ActionExtension;"

@Suppress("unused")
val actionPromotionSortPatch = bytecodePatch(
    name = "Weekly Deals Sorting",
    description = "Adds sorting options (by lowest price and highest discount percentage) with interactive chips to the Weekly Deals tabs.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    extendWith("extensions/extension.mpe")

    execute {
        // Hook 1: Product grid sorting at bq.a0.d entry
        val gridMethod = ProductGridBuildFingerprint.method
        gridMethod.addInstructions(
            0,
            """
                invoke-static {p0}, $EXTENSION_CLASS->sortProductList(Ljava/util/List;)Ljava/util/List;
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
        val newInstruction = listScopeMethod.getInstruction(iteratorIndex)
        val shiftedInstruction = listScopeMethod.getInstruction(iteratorIndex + 1)
        val labels = shiftedInstruction.location.labels
        if (labels.isNotEmpty()) {
            newInstruction.location.labels.addAll(labels)
            labels.clear()
        }
    }
}
