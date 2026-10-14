package app.action.patches.theme

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val actionDarkModePatch = bytecodePatch(
    name = "Dark Mode",
    description = "Forces dark mode throughout the Action app by overriding the core Jetpack Compose color palette.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    execute {
        val method = PaletteClinitFingerprint.method
        val paletteClass = method.definingClass

        // Locate the return-void opcode at the end of the static initializer
        val returnIndex = method.instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
        if (returnIndex == -1) {
            throw IllegalStateException("Failed to find return-void opcode in palette <clinit>")
        }

        // Inject dark theme color overrides right before return-void
        method.addInstructions(
            returnIndex,
            """
                # neutral0: App background (replace #FFFFFFFF with dark #FF121212)
                const-wide v0, 0x00000000ff121212L
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->a:J

                # cardBackground: Card and tile backgrounds (replace #FFFFFFFF with #FF1E1E1E)
                const-wide v0, 0x00000000ff1e1e1eL
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->A0:J

                # neutral50: Container surface background (replace #FFCED4DA with #FF1E1E1E)
                const-wide v0, 0x00000000ff1e1e1eL
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->b:J

                # neutral40: Input fields and row backgrounds (replace #FFF3F4F6 with #FF262626)
                const-wide v0, 0x00000000ff262626L
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->c:J

                # neutral900: Primary text (replace dark #FF1A1A1A with light #FFF0F0F0)
                const-wide v0, 0x00000000fff0f0f0L
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->h:J

                # neutral700: Secondary text (replace dark #FF505862 with light gray #FFB0B0B0)
                const-wide v0, 0x00000000ffb0b0b0L
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->g:J

                # neutral500: Auxiliary / disabled text (replace #FF8593A3 with #FF888888)
                const-wide v0, 0x00000000ff888888L
                invoke-static {v0, v1}, Lh4/e0;->e(J)J
                move-result-wide v0
                sput-wide v0, $paletteClass->e:J
            """
        )
    }
}
