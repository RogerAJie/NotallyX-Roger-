# Personal Notes — Roger edition

This fork keeps NotallyX's original source code and GPL-3.0 license. The custom
launcher is a deliberately simple, offline-only notebook with exactly two tabs.

## Screen 1 — 记事本
- Add multiple independent memos.
- Each memo has an optional title and a full-page multiline body editor.
- If the title is blank, the first sentence of the memo body is displayed
  as the title. The body is not shortened or removed.
- Tap a memo to edit it. Long-press it to delete (confirmation required).
- Changes are saved locally on leaving the editor.

## Screen 2 — To-do List
- The + button adds one task per row; no date, reminder, folder or deadline.
- Tap the small checkbox to toggle completion.
- Completed tasks immediately move below incomplete tasks and appear grey with
  strikethrough. Unchecking moves them back among incomplete tasks.
- Tap the task text to edit or delete it.

## Data and compatibility
- SQLite database: app-private file named personal_notes.db.
- No cloud account, server, Firebase, or network is required.
- This is separate from the original NotallyX Room database: records from
  the original NotallyX app are not automatically imported into the custom tabs.
- The package ID is com.rogerajie.personalnotes, independent from the
  official NotallyX package. Uninstalling the new app can delete its local data;
  a backup/export function is not yet included.
- The original NotallyX code remains in the repository but its old MainActivity
  is no longer the launcher.

## Compile and install
1. Open this repository's root folder in Android Studio.
2. Allow Gradle to sync and install the matching Android SDK/NDK if prompted.
3. Select Build > Build Bundle(s) / APK(s) > Build APK(s) (debug build).
4. Install the APK from app/build/outputs/apk/debug/.
5. Alternatively, enable GitHub Actions and use the artifact from
   Build personal notes APK after it completes successfully.

Release-signing properties are optional for the debug build. To create an
upgradable release APK, configure a persistent signing key yourself.

The custom pages have not yet been verified on a real handset; test creating,
editing, checking, unchecking, and reopening the app before relying on it.
