package app.action.patches.theme

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Fingerprint matching the static initializer (<clinit>) of the Action app's color palette class.
 * Matches against the primary text color constant (neutral900: #FF1A1A1A) and the closing return-void.
 */
object PaletteClinitFingerprint : Fingerprint(
    name = "<clinit>",
    accessFlags = listOf(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        literal(0x00000000ff1a1a1aL),
        opcode(Opcode.RETURN_VOID)
    )
)

/**
 * Fingerprint matching the barcode/QR code bitmap generator method in gz.q7.
 * Unique signature with parameters (BitMatrix, int) returning Bitmap,
 * and the inner loop instruction pattern setting the pixel colors.
 */
object QrCodeBitmapFingerprint : Fingerprint(
    name = "b",
    returnType = "Landroid/graphics/Bitmap;",
    parameters = listOf("Ln40/b;", "I"),
    filters = listOf(
        opcode(Opcode.IF_EQZ),
        opcode(Opcode.MOVE),
        opcode(Opcode.GOTO),
        opcode(Opcode.MOVE),
        opcode(Opcode.APUT)
    )
)

/**
 * Fingerprint matching the Jetpack Compose Material 3 DropdownMenu composable (g3.l.a).
 * Matches the unique restart group key (1725609375 / 0x66dd6d3f) and the fallback containerColor move-result-wide.
 */
object DropdownMenuFingerprint : Fingerprint(
    name = "a",
    accessFlags = listOf(AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    filters = listOf(
        literal(1725609375),
        opcode(Opcode.MOVE_RESULT_WIDE)
    )
)


