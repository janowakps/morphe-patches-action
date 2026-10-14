package app.action.patches.theme

import app.action.patches.shared.Constants.COMPATIBILITY_ACTION
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
val actionSplashPatch = resourcePatch(
    name = "Dark Splash Screen",
    description = "Forces dark window and splash screen backgrounds in styles.xml to eliminate the white flash on app startup.",
    default = true
) {
    compatibleWith(COMPATIBILITY_ACTION)

    execute {
        val stylesFile = get("res/values/styles.xml")
        if (stylesFile.exists()) {
            var content = stylesFile.readText()
            content = content
                .replace(
                    """<item name="android:colorBackground">@color/white</item>""",
                    """<item name="android:colorBackground">#FF121212</item>"""
                )
                .replace(
                    """<item name="android:windowBackground">@color/white</item>""",
                    """<item name="android:windowBackground">#FF121212</item>"""
                )
                .replace(
                    """<style name="WhiteBackground" parent="@style/AppTheme">""",
                    """<style name="WhiteBackground" parent="@style/AppTheme">
        <item name="android:windowBackground">#FF121212</item>
        <item name="android:colorBackground">#FF121212</item>"""
                )
            stylesFile.writeText(content)
        }

        val v31StylesFile = get("res/values-v31/styles.xml")
        if (v31StylesFile.exists()) {
            var v31Content = v31StylesFile.readText()
            if (!v31Content.contains("WhiteBackground")) {
                v31Content = v31Content.replace(
                    "</resources>",
                    """
    <style name="WhiteBackground" parent="@style/AppTheme">
        <item name="android:windowSplashScreenBackground">#FF121212</item>
        <item name="android:windowBackground">#FF121212</item>
    </style>
</resources>"""
                )
                v31StylesFile.writeText(v31Content)
            }
        }
    }
}
