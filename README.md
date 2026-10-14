# Action App Dark Mode Patches

Custom Morphe patches for the Action store consumer app (`com.action.consumerapp`).

## About

This repository provides a custom patch bundle for Morphe that enables Dark Mode in the official Action Android application (built with Jetpack Compose).

### How to use these patches in Morphe Manager

1. Open Morphe Manager on your Android device.
2. Go to **Settings** > **Sources**.
3. Add custom source: `https://github.com/janowakps/morphe-patches-action`
4. Or tap this link on your device: [Add to Morphe](https://morphe.software/add-source?github=janowakps/morphe-patches-action)
5. Select **Action**, enable **Dark Mode**, and patch the app!

## Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.17](https://github.com/janowakps/morphe-patches-action/releases/tag/v1.0.0-dev.17)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
<details open>
<summary>📦 Action&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 1.133.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Dark Mode](#dark-mode) | Forces dark mode throughout the Action app by overriding the core Jetpack Compose color palette and customer card QR code. |  |
| [Dark Splash Screen](#dark-splash-screen) | Forces dark window and splash screen backgrounds in styles.xml to eliminate the white flash on app startup. |  |
| [Weekly Deals Sorting](#weekly-deals-sorting) | Adds bidirectional sorting options with interactive chips for Weekly Deals, keeps native filtering/sorting untouched on Nowości and category pages, increases initial deals page limit to 120, and fixes badge label clipping. |  |

</details>

<!-- PATCHES_END -->

## Development

- All modifications are made on the `dev` branch.
- Automated releases are generated via GitHub Actions using semantic releases.
