package app.action.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_ACTION = Compatibility(
        name = "Action",
        packageName = "com.action.consumerapp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x001489,
        targets = listOf(
            AppTarget(
                version = "1.133.0"
            ),
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}
