# ForexFloat — Android starter

This is the first Android prototype for ForexFloat.

## Current features
- Android floating overlay window
- Drag-to-move ticker
- EURUSD, GBPUSD, USDJPY and XAUUSD rows
- Foreground service notification
- Simulated demo prices that move every second

## Important
**Prices are simulated, not live market data.** Do not use this prototype for trading decisions. A licensed live data provider and commercial redistribution permission must be integrated before public release.

## Build APK with GitHub Actions
1. Create a new GitHub repository named `ForexFloat`.
2. Upload the contents of this folder to the repository root.
3. Open the repository's **Actions** tab.
4. Select **Build ForexFloat APK** and click **Run workflow** (or wait for the automatic build after pushing to `main`).
5. Open the completed workflow run and download the `ForexFloat-debug-apk` artifact.
6. Extract the artifact ZIP to get `app-debug.apk`.

## Install on Samsung
Transfer `app-debug.apk` to the phone and open it. Allow installation from the file manager if Android asks. Launch ForexFloat, tap **Start floating ticker**, and grant **Display over other apps** permission.

## Next development steps
- Replace simulated prices with a licensed live streaming data source.
- Add pair selection and overlay controls.
- Improve error handling, battery behavior and Android-version compatibility.
- Create a signed release build before publishing.
