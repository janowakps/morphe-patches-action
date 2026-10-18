package app.action.patches.theme

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private data class ColorOverride(
    val field: String,
    val argb: Long,
    val comment: String
)

@Suppress("unused")
val actionDarkModePatch = bytecodePatch(
    name = "Dark Mode",
    description = "Forces dark mode throughout the Action app by overriding the core Jetpack Compose color palette and customer card QR code.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    execute {
        val returnIndex = PaletteClinitFingerprint.instructionMatches.last().index
        val method = PaletteClinitFingerprint.method
        val paletteClass = method.definingClass

        val overrides = listOf(
            // Neutrals (surfaces, text, borders)
            ColorOverride("a", 0xFF121212L, "neutral0: App background (#FFFFFFFF -> #121212)"),
            ColorOverride("c", 0xFF1E1E1EL, "neutral50: Surface background (#FFF3F4F6 -> #1E1E1E)"),
            ColorOverride("b", 0xFF262626L, "neutral40: Input fields and row backgrounds (#FFCED4DA -> #262626)"),
            ColorOverride("f", 0xFF2E2E2EL, "neutral150: Dividers and separators (#FFDADFE3 -> #2E2E2E)"),
            ColorOverride("d", 0xFF484848L, "neutral300: Outline borders and inactive icons (#FFB6BEC8 -> #484848)"),
            ColorOverride("e", 0xFF9E9E9EL, "neutral500: Auxiliary text / muted (#FF8593A3 -> #9E9E9E)"),
            ColorOverride("g", 0xFFC4C4C4L, "neutral700: Secondary text and subtitles (#FF505862 -> #C4C4C4)"),
            ColorOverride("h", 0xFFF0F0F0L, "neutral900: Primary text and titles (#FF1A1A1A -> #F0F0F0)"),
            ColorOverride("A0", 0xFF282828L, "cardBackground: Cards and tiles (#FFFFFFFF -> #282828)"),

            // Primary Brand Colors (darkBlue family)
            ColorOverride("i", 0xFF1A2333L, "darkBlue50: Header card and pill container background (#E6E8F3 -> #1A2333)"),
            ColorOverride("j", 0xFF26354DL, "darkBlue150: Subtle blue container (#B3B9DC -> #26354D)"),
            ColorOverride("k", 0xFF3D557AL, "darkBlue300: Medium blue accent (#6672B8 -> #3D557A)"),
            ColorOverride("l", 0xFF5C9CFFL, "darkBlue500: Primary brand color - titles, links, buttons (#001489 -> #5C9CFF)"),
            ColorOverride("m", 0xFF7AAEFFL, "darkBlue600: Hover/active blue (#00106E -> #7AAEFF)"),
            ColorOverride("n", 0xFF94C0FFL, "darkBlue700: Light blue variant (#000C52 -> #94C0FF)"),
            ColorOverride("o", 0xFFB8D6FFL, "darkBlue900: Lightest blue text (#00041B -> #B8D6FF)"),

            // Warning Alerts
            ColorOverride("n0", 0xFF332612L, "warning50: Warning banner container background (#FFF7E8 -> #332612)"),
            ColorOverride("o0", 0xFFFBBF24L, "warning500: Warning icon and highlight (#DF7B00 -> #FBBF24)"),
            ColorOverride("p0", 0xFFFEEBC8L, "warning900: Warning message text (#332305 -> #FEEBC8)"),

            // Error Alerts (Product recall banner)
            ColorOverride("t0", 0xFF361717L, "error50: Error/recall banner container background (#F9E9E9 -> #361717)"),
            ColorOverride("u0", 0xFFF87171L, "error500: Error icon and highlight (#C21E1E -> #F87171)"),
            ColorOverride("v0", 0xFFFEE2E2L, "error900: Error message text (#270606 -> #FEE2E2)"),

            // Info Alerts & Banners
            ColorOverride("w0", 0xFF152538L, "info50: Info banner container background (#E6F2FA -> #152538)"),
            ColorOverride("x0", 0xFF223954L, "info150: Info subtle container (#B2D7F0 -> #223954)"),
            ColorOverride("y0", 0xFF38BDF8L, "info500: Info icon and accent (#007ACC -> #38BDF8)"),
            ColorOverride("z0", 0xFFE0F2FEL, "info900: Info message text (#001829 -> #E0F2FE)"),

            // Success Alerts
            ColorOverride("q0", 0xFF132A1AL, "success50: Success banner container background (#E9F4E6 -> #132A1A)"),
            ColorOverride("r0", 0xFF4ADE80L, "success500: Success icon and highlight (#259300 -> #4ADE80)"),
            ColorOverride("s0", 0xFFDCFCE7L, "success900: Success message text (#071D00 -> #DCFCE7)"),

            // Petrol (category badges and filters)
            ColorOverride("J", 0xFF1B252EL, "petrol50: Petrol container (#EDF1F5 -> #1B252E)"),
            ColorOverride("M", 0xFF6E9AC4L, "petrol500: Petrol icon/text (#4E769C -> #6E9AC4)"),
            ColorOverride("N", 0xFF96B8D8L, "petrol700: Petrol text (#2F475E -> #96B8D8)"),
            ColorOverride("O", 0xFFD0E2F2L, "petrol900: Petrol light text (#10181F -> #D0E2F2)"),

            // SkyBlue (promotions and badges)
            ColorOverride("P", 0xFF162534L, "skyBlue50: SkyBlue container (#F9FCFE -> #162534)"),
            ColorOverride("Q", 0xFF1F354DL, "skyBlue150: SkyBlue container border (#ECF6FC -> #1F354D)"),
            ColorOverride("R", 0xFF2D4B6DL, "skyBlue300: SkyBlue surface (#D9EDF9 -> #2D4B6D)"),
            ColorOverride("S", 0xFF4D9BE0L, "skyBlue500: SkyBlue badge (#C0E1F5 -> #4D9BE0)"),
            ColorOverride("T", 0xFF90C2F0L, "skyBlue700: SkyBlue text (#738793 -> #90C2F0)"),
            ColorOverride("U", 0xFFD4E7F9L, "skyBlue900: SkyBlue light text (#262D31 -> #D4E7F9)"),

            // Orange (deal tags & badges)
            ColorOverride("p", 0xFF332314L, "orange50: Orange badge container (#FFF3E6 -> #332314)"),
            ColorOverride("s", 0xFFFFA338L, "orange500: Orange badge (#FF8200 -> #FFA338)"),
            ColorOverride("t", 0xFFFFB866L, "orange700: Orange text (#994E00 -> #FFB866)"),
            ColorOverride("u", 0xFFFFD9B3L, "orange900: Orange light text (#331A00 -> #FFD9B3)"),

            // Cyan (highlights)
            ColorOverride("v", 0xFF122830L, "cyan50: Cyan container (#E6F8FD -> #122830)"),
            ColorOverride("A", 0xFF38C8F0L, "cyan700: Cyan text (#006D8B -> #38C8F0)"),
            ColorOverride("B", 0xFF80E1F8L, "cyan900: Cyan light text (#00242E -> #80E1F8)"),

            // Green (prices & tags)
            ColorOverride("C", 0xFF132A1CL, "green50: Green container (#E6F8EE -> #132A1C)"),
            ColorOverride("G", 0xFF34D376L, "green700: Green text (#006E31 -> #34D376)"),
            ColorOverride("H", 0xFF86EFACL, "green900: Green light text (#002510 -> #86EFAC)"),

            // Pink (special tags)
            ColorOverride("b0", 0xFF331525L, "pink50: Pink container (#FEEDF5 -> #331525)"),
            ColorOverride("f0", 0xFFF472B6L, "pink700: Pink text (#902F5B -> #F472B6)"),
            ColorOverride("g0", 0xFFFBCFE8L, "pink900: Pink light text (#30101E -> #FBCFE8)"),

            // Purple (special tags)
            ColorOverride("h0", 0xFF2B1833L, "purple50: Purple container (#F0E7F2 -> #2B1833)"),
            ColorOverride("l0", 0xFFC084FCL, "purple700: Purple text (#410B4C -> #C084FC)"),
            ColorOverride("m0", 0xFFE9D5FFL, "purple900: Purple light text (#160419 -> #E9D5FF)")
        )

        val smaliInstructions = buildString {
            for (override in overrides) {
                val hex = "0x00000000%08xL".format(override.argb and 0xFFFFFFFFL)
                appendLine("    # ${override.comment}")
                appendLine("    const-wide v0, $hex")
                appendLine("    invoke-static {v0, v1}, Lh4/e0;->e(J)J")
                appendLine("    move-result-wide v0")
                appendLine("    sput-wide v0, $paletteClass->${override.field}:J")
                appendLine()
            }
        }

        method.addInstructions(returnIndex, smaliInstructions)

        // Override QR code / barcode renderer in gz.q7.b
        // Changes foreground color to black and background color to solid white
        val qrMethod = QrCodeBitmapFingerprint.method
        val bgMoveInstruction = QrCodeBitmapFingerprint.instructionMatches[3]
        val bgMoveIndex = bgMoveInstruction.index

        // Replace background color assignment (move v8, v0) with white constant (const/4 v8, -1)
        qrMethod.replaceInstruction(bgMoveIndex, "const/4 v8, -0x1")

        // Force foreground color parameter (p1) to solid black (0xFF000000) at method entry
        qrMethod.addInstructions(0, "const/high16 p1, -0x1000000")

        // Override DropdownMenu (Material 3 popup) background container color in g3.l.a
        // When containerColor is unspecified (0L), it defaults to Material 3 light SurfaceContainer (#F3EDF7).
        // In dark mode, primary text (neutral900) is #FFF0F0F0, causing invisible white-on-light text in the receipt
        // overflow menu ("Pobierz paragon", "Udostępnij swój paragon").
        // This forces the fallback containerColor to dark card background (#FF282828).
        val dropdownMethod = DropdownMenuFingerprint.method
        val moveResultInstruction = DropdownMenuFingerprint.instructionMatches.last()
        val moveResultIndex = moveResultInstruction.index
        val reg = (moveResultInstruction.instruction as? OneRegisterInstruction)?.registerA ?: 11
        val regName = "v$reg"
        val regNext = "v${reg + 1}"

        dropdownMethod.addInstructions(
            moveResultIndex + 1,
            """
                const-wide $regName, 0x00000000ff282828L
                invoke-static {$regName, $regNext}, Lh4/e0;->e(J)J
                move-result-wide $regName
            """
        )
    }
}


