# IosFacebook – iOS-style Facebook shell for Android (Kotlin + Compose)

iOS 26 "Liquid Glass" chrome (floating glass buttons, floating capsule tab bar, scroll-edge fade,
haptics, spring animations) around `m.facebook.com` running in persistent per-tab WebViews.

## Run
1. Open the folder in Android Studio (Koala or newer), let Gradle sync
   (it downloads Gradle 8.7 from `gradle-wrapper.properties`).
2. Run on a device/emulator (minSdk 26).

## Where to tweak the look
- `app/src/main/assets/ios_fb.css`  – CSS injected into every page.
- `app/src/main/assets/ios_fb.js`   – hides Facebook's own top/bottom bars.
  If the web bars reappear after a Facebook update, add selectors to `KNOWN`.
- `ui/FacebookApp.kt` – native top bar, tab bar, skeleton, error screen.
- `web/FbTab.kt` – tabs and their URLs.
- `web/TabState.kt` – WebView settings, link routing, push/pop animation.

## iOS 26 look / "not a web page"
- Page scrolls under the glass bars; the app passes the bar heights to the page as `--iosfb-top` / `--iosfb-bottom` (CSS).
- Web header, icon row and "Open app" banner are hidden by `ios_fb.js` (label/text based, EN + AR).
- User-Agent = Chrome minus WebView markers + `[FB_IAB/FB4A;FBAV/...]` (`FB_IAB_SUFFIX` in `TabState.kt`),
  so Facebook treats it as its in-app browser. Bump the FBAV number if Facebook stops honouring it.

## Behaviour
- Re-tap active tab: scroll to top, then refresh.
- Pull-to-refresh, progress line, no-connection screen, file upload, downloads, fullscreen video.
- Outbound links open in Chrome Custom Tabs (l.facebook.com wrappers are unwrapped).

## GitHub Release (APK)
1. Push the project to a GitHub repo.
2. `git tag v1.0 && git push --tags`
3. Actions builds the APK and attaches it to Releases.
   (Or run "Build & Release APK" manually from the Actions tab; the APK is then in the run's artifacts.)
