# Brewkery — Coffee & Bakery Ordering App

Native Android (Kotlin) take-home assignment for Clickretina — Android Developer role.

A 4-screen coffee & bakery ordering app powered by a single REST API:
**Menu → Item Detail → Cart → Order Status**

## Features
- Menu catalog with category filter, store banner, ratings and badges
- Item detail with dynamic customizations (sizes, milk options, sugar levels) and live price recalculation
- Cart with quantity steppers, subtotal + $2.50 delivery + 8% tax bill math
- Order placement with generated ticket ID (`BK-xxxxx`) and PREPARING status screen
- Loading + error + retry states on every network screen
- Images loaded with Coil, network with Retrofit + Coroutines, UI in Jetpack Compose (MVVM)

## Architecture (MVVM)
- `data/model` — Gson data classes matching the API JSON
- `data/network` — Retrofit `BrewkeryApi` + singleton `RetrofitClient`
- `data/repository` — `MenuRepository` (single source of truth for menu calls)
- `data/cart` — `CartManager` (in-memory cart, resets on app close — no DB needed)
  + `CartCalculator` (pure billing math, unit-tested)
- `ui` — `MenuViewModel`, `DetailViewModel`, `CartViewModel` (activity-scoped, shared cart)
- `ui/screens` — 4 Compose screens + `NavGraph` (Navigation Compose)

## API
- Menu: `GET https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json`
- Item: `GET https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json`

## Setup
1. Android Studio (recent version, JDK 17), `minSdk 24`
2. Clone and open the project, let Gradle sync
3. Add the dependencies from `GRADLE_DEPS.txt` if sync complains (all listed with versions)
4. Run on emulator or device (internet required for API + images)

## Build debug APK
Android Studio: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
APK lands in `app/build/outputs/apk/debug/app-debug.apk`

## Run unit tests
`./gradlew testDebugUnitTest` — tests the cart billing math
(`CartCalculatorTest`: line totals, tax-on-subtotal rule, prototype bill $12.65)

## Time spent
~5 hours, as time-boxed in the brief. No over-engineering: no database (per brief),
manual DI, single repository.

---

## 🤖 How AI was used (per brief requirement)

**Tools used:** An agentic AI coding assistant (AI pair-programmer) inside the editor
for scaffolding and boilerplate, plus Gemini in Android Studio for quick fixes.
All generated code was read, tested and reviewed by me — I own every line shipped.

**3 actual prompts sent:**
1. "Generate Kotlin data classes with Gson @SerializedName annotations for this
   menu JSON (meta, categories, items, customizations with sizes/milk/sugar)."
2. "Write a pure-Kotlin cart billing helper: line total = (base + sizeExtra +
   milkExtra) x qty; subtotal; 8% tax on subtotal only; total; $ formatting."
3. "Build a Jetpack Compose item-detail screen: hero image, ingredients chips,
   radio groups for size/milk/sugar with live price recalculation + quantity
   stepper and Add to Cart button."

**One thing AI got RIGHT:** The Gson models matched the snake_case API JSON
exactly on the first pass (`base_price`, `milk_options`, nested customizations),
so the menu parsed with zero fixes.

**One thing AI got WRONG — and how I fixed it:** The first billing version applied
the 8% tax on (subtotal + delivery fee), giving $0.95 on the prototype's bill.
I cross-checked with the prototype ($9.40 × 8% = $0.75 shown) and fixed the rule
to tax-on-subtotal-only in `CartCalculator.tax()` — covered by the unit test
`tax_appliesOnSubtotalOnly_notOnDeliveryFee`.

---

Author: Vivek Singh — Android Developer (Kotlin, MVVM, Retrofit, Compose)
