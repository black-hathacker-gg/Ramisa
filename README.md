<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/eea32b17-1204-4d0f-8b89-9671a82a51c0

## Run locally

Use Android Studio or the included Gradle wrapper with JDK 17 or newer, Android SDK 36.1 and Build Tools 36.0.0. Set the SDK location in Android Studio or through `ANDROID_HOME`. Run `gradlew.bat :app:assembleDebug` on Windows. The debug variant installs as **RAMISA Test** under a separate app ID so you can keep the original installation.

## Demonstrate the updated flows

- In Tools, open **AI Threat Guard**. Choose **Replay speech sample** in the debug variant to run the on-device YAMNet model without microphone access. You can also explicitly start the microphone. Model scores describe sounds. A candidate prompt appears only when enabled and requires user confirmation. The bundled recording and manual preview cannot send an alert.
- Open **Emergency SOS** to see the enabled trusted contacts and the available location. The SMS status separates network send confirmation and delivery confirmation. Empty contacts or missing SMS access block submission. Test carrier delivery separately with a consenting contact on a SIM-equipped phone.
- **Offline Mesh Preview** uses simulated peers and messages. It is separate from the cellular SMS path.

The acoustic classifier needs no API key. Existing integrations outside these two flows may still require their own project configuration.

## Enable online accounts

GitHub and AI Studio builds use the RAMISA project’s public client settings in `.env.example`. To use another Supabase project locally, copy that file to `.env` and change `SUPABASE_URL` and `SUPABASE_ANON_KEY` to the HTTPS project URL and a client publishable key. The Android build embeds these client values. Keep privileged Supabase keys and SMTP credentials on the server.

Enable email and password sign-in in Supabase Auth and set the Auth Site URL to your public HTTPS page. With email verification enabled, the user opens the confirmation email and returns to RAMISA to sign in. Configure custom SMTP for delivery to users outside your Supabase organization. For details, see [Supabase API keys](https://supabase.com/docs/guides/api/api-keys), [Auth redirect URLs](https://supabase.com/docs/guides/auth/redirect-urls) and [SMTP setup](https://supabase.com/docs/guides/auth/auth-smtp).

Guest access and on-device safety features work without Supabase. Keep your emergency PIN, contact and safety profile details up to date on each device you use because the account service handles identity and the app keeps those details on the device.

## Maps and safety alerts

- The street map uses [MapLibre Native](https://maplibre.org/maplibre-native/android/) and keyless [OpenFreeMap](https://openfreemap.org/quick_start/) tiles, with OpenStreetMap attribution. Offline Maps downloads selected regional street tiles. Saved map pins and example incidents do not indicate current conditions.
- Safe Journey tracks device location and prompts for a check-in at the expected arrival time. Ride Safety shares ride details and the latest available coordinates through Android's share sheet. Both trip sessions are device-local.
- Home, hardware previews, and Android shortcuts open SOS review. Direct SMS from the lock screen, deliberate shake detection, and low-battery ping each have an explicit switch. The check-in timer attempts SMS after a missed check-in while its screen is running.
- Community posting and general help requests need an authenticated Supabase session and the `community_messages` table with access policies. For a fresh database, see `supabase/schema/community_messages.sql`. Review existing policies before migrating an existing table. Offline posts stay in the current session. Volunteer matching and Bluetooth mesh are on-device previews.

To verify the Android build, run `gradlew.bat :app:testDebugUnitTest` and `gradlew.bat :app:connectedDebugAndroidTest` with a disposable emulator. The navigation smoke test opens all 24 tools from the guest account and checks that opening SOS review leaves the SMS ledger unchanged.
