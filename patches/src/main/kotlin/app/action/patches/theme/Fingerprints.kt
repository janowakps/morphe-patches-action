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
 * Unique in the app for creating an ARGB_8888 bitmap and calling setPixels to write pixel arrays.
 */
object QrCodeBitmapFingerprint : Fingerprint(
    returnType = "Landroid/graphics/Bitmap;",
    filters = listOf(
        opcode(Opcode.INVOKE_STATIC), // Bitmap.createBitmap
        opcode(Opcode.INVOKE_VIRTUAL), // Bitmap.setPixels
        opcode(Opcode.RETURN_OBJECT)
    )
)
