# Action App Reverse Engineering & Architecture Map

Target APK: `com.action.consumerapp` (v1.133.0)

This document serves as a persistent, rapid-lookup reference for the Action Android app architecture to ensure fast, zero-risk patch development.

---

## 1. Jetpack Compose Color Palette (`ai.b`)

The Action app uses a centralized Jetpack Compose color palette defined and initialized in `ai.b.<clinit>()`. Overriding static fields in `ai.b` propagates theme colors throughout the entire Compose hierarchy.

| Field | Original Hex | Dark Mode Hex | Semantic Role / Component |
|-------|--------------|---------------|---------------------------|
| `a` | `#FFF5F5F7` | `#FF121212` | Root app window & scaffold background |
| `c` | `#FFFFFFFF` | `#FF1E1E1E` | Primary surface background (sheets, dialogs, bottom sheets) |
| `d` | `#FFE6E8F3` | `#FF2A2A2A` | Card & divider subtle borders / outlines |
| `e` | `#FF8593A3` | `#FF9E9E9E` | Secondary/auxiliary text, muted labels, timestamps |
| `g` | `#FF505862` | `#FFC4C4C4` | Subtitles, body text, secondary icons |
| `h` | `#FF1A1A1A` | `#FFF0F0F0` | Primary headings, product titles, bold text |
| `A0` | `#FFFFFFFF` | `#FF282828` | **Card & product tile background** (photos sit on this!) |
| `i` | `#FFE6E8F3` | `#FF1A2333` | Brand tint 50: Header card & pill containers |
| `v` | `#FF001489` | `#FF2979FF` | Primary brand blue (buttons, active indicators) |
| `y` | `#FFF1F5FA` | `#FF1C2430` | Interactive chip/pill inactive background |
| `z` | `#FFD2E1F0` | `#FF263850` | Interactive chip/pill active background |

> **Key Takeaway for Product Images**:
> Product photos are transparent PNGs (`digital_square` URLs). In the original app, they appeared to have a white background because their container card `A0` was white (`#FFFFFFFF`). Setting `A0` to `#FF282828` provides a clean, gentle gray contrast for all dark products without touching image composable bytecode.

---

## 2. Weekly Deals & Grid Architecture

### Grid Scope & Identification (`zp.d.Z`)
- Method `zp.d.Z` renders product grids.
- `gridName` (String parameter `p2`):
  - Weekly Deals: typically contains `weekdeal` or `plp_weekdeals`.
  - 'New' (Nowości / Neu / Nové), Search, and Categories: have their own grids.
- `gridState` (`zp.f` parameter `p4`):
  - Field `g` (boolean `hideFilters`): `false` on grids that have their own native filter/sort button (like 'New' and Search).
  - Only screens with `hideFilters == true` and Weekly Deals presenters are treated as Weekly Deals.

### Product List Sorting (`bq.a0.d`)
- Products are held in `java.util.List` passed to `bq.a0.d`.
- `ActionExtension.sortProductList(List)` intercepts the list and sorts by:
  - Mode 0: Default / Relevance (native order)
  - Mode 1: Price Ascending (lowest price first)
  - Mode 2: Discount Descending (deepest discount first, `% ↓`)
  - Mode 3: Price Descending (highest price first)
- **Interactive Sorting Chips**:
  - Chip 1 (Relevance): dynamically pulls native localized string `filter_and_sort_sorting_relevance` from APK resources (e.g. `Polecane` in PL, `Empfohlen` in DE, `Doporučené` in CZ, `Aanbevolen` in NL).
  - Chip 2 (Price): dynamically pulls native localized string `filter_and_sort_price_title` + direction arrow (`Cena ↑` / `Cena ↓`, `Preis ↑` / `Preis ↓`, `Prijs ↑` / `Prijs ↓`).
  - Chip 3 (`% ↓`): internationally recognized symbol for discount. **Dynamically hidden** when no products on the active grid/market contain discount percentages (e.g., in Germany or Czech Republic).

### Deals Pagination Limit (`zp.k.a`)
- Intercepted by `WeeklyDealsLimitFingerprint`.
- `ActionExtension.getPageLimit()`:
  - Returns `120` for Weekly Deals to load all deals in a single request.
  - Returns `24` for 'New', Search, and Category grids to preserve continuous native pagination without skipped items.

---

## 3. Customer Card & QR Code (`yh.p.b`)
- Method `yh.p.b` generates the barcode / QR code bitmap.
- Overriding parameter `p1` to solid black (`0xFF000000` via `const/high16 p1, -0x1000000`) forces the QR code lines to remain dark and scannable by physical barcode scanners in stores.

---

## 4. Engineering Principles & Safety Rules
1. **Prefer Palette Over Bytecode Injection**: Never hook Jetpack Compose composables (`hg.c.a`, etc.) directly with arbitrary registers. Compose bytecode is heavily optimized and type-checked by the ART Verifier (`VerifyError`).
2. **Single `extendWith` Rule**: `extendWith("extensions/extension.mpe")` must ONLY be declared ONCE in the patch bundle. Multiple patches declaring it causes duplicate DEX classes (`DexException`).
3. **Always Check Local Cheatsheet First**: Refer to this document before reverse-engineering from scratch.
