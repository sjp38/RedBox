
package org.clc.android.app.redbox.ad;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

/**
 * Managing advertisement.
 * 
 * @author sj38_park
 */
public class AdvertisementManager {
    public AdvertisementManager() {
    }

    static public View getAdvertisementView(Activity client) {
        String adId = "";
        final Resources resources = client.getResources();
        final int resourceId = resources.getIdentifier("admob_id", "string",
                client.getPackageName());
        if (resourceId != 0) {
            adId = resources.getString(resourceId);
        }

        final AdView adView = new AdView(client, AdSize.BANNER, adId);
        final AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        adView.loadAd(adRequest);

        return adView;
    }

    static public void destroyAd(final View v) {
        if (v instanceof AdView) {
            final AdView adView = (AdView) v;
            adView.destroy();
        }
    }

}
