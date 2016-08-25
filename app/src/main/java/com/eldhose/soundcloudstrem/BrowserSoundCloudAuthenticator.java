package com.eldhose.soundcloudstrem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;
import java.util.Map;

/**
 * Created by ELDHOSE on 2016-08-18.
 */

    public class BrowserSoundCloudAuthenticator extends SoundCloudAuthenticator {

        private final Activity context;

        public BrowserSoundCloudAuthenticator(String clientId, String redirectUri, Activity context) {
            super(clientId, redirectUri);

            this.context = context;
        }


        @Override public boolean prepareAuthenticationFlow() {
            // Launches synchronously in this implementation, no preparation needed.
            return true;
        }


        @Override public void launchAuthenticationFlow() {
            Intent loginIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl()));
            loginIntent.setPackage(getBrowserPackageName());
            addReferrerToIntent(loginIntent, context.getPackageName());

            context.startActivity(loginIntent);
        }



        /**
         * Resolves the package name for the browser that should open the authentication web page.
         *
         * @return the name of the application package that should open the URL.
         */
        private String getBrowserPackageName() {
            String packageName = "com.android.browser"; // Probably exists

            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

            List<ResolveInfo> resolveInfos = context.getPackageManager()
                    .queryIntentActivities(webIntent, PackageManager.MATCH_DEFAULT_ONLY);

            // List should only contain one value if a default browser is selected
            if(resolveInfos != null && resolveInfos.size() > 0) {
                for (ResolveInfo info : resolveInfos) {

                    // Go to the next list item if this ResolveInfo doesn't have an associated packageName
                    if(info.activityInfo == null) continue;
                    if(info.activityInfo.packageName == null) continue;

                    packageName = info.activityInfo.packageName;

                    break; // A package to handle the intent was found
                }
            }

            return packageName;
        }
    }


