package asf.dungeon.android;

import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import asf.dungeon.DungeonApp;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * https://github.com/TheInvader360/tutorial-libgdx-google-ads/blob/9a4c9342d98c02e3c44e0b62fcfaa153d257130a/tutorial-libgdx-google-ads-android/src/com/theinvader360/tutorial/libgdx/google/ads/MainActivity.java
 * https://github.com/TheInvader360/tutorial-libgdx-google-ads/commit/0a5ea376d4eb92b8e87c13a03245adb40b53e811
 * <p/>
 * https://github.com/googleads/googleads-mobile-android-examples/blob/master/admob/interstitial/src/com/google/example/gms/ads/advanced/InterstitialSample.java
 */
public class AndroidLauncher extends AndroidApplication implements DungeonApp.Resolver{
        private final String bannerAdUnitId = null;//"ca-app-pub-3542905976626572/7731680641";
        private final String interstitalAdUnitId = null; // "ca-app-pub-3542905976626572/7731680641";
        private final String[] testDeviceIds = new String[]{"90BB3531E9DE22C6EE2CFFD0FBD8CD0E", "EC11ABF15B7CD200ED08B48C56C2A5BA"};
        private InterstitialAd interstitialAd;
        private AdView bannerAdView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
                config.useAccelerometer = false;
                config.useCompass = false;
                config.numSamples = 2;
                config.useImmersiveMode = true;
                config.depth = 1;


                DungeonApp dungeonGame = new DungeonApp();
                dungeonGame.setPlatformActionResolver(this);

                if (bannerAdUnitId == null) {
                        initialize(dungeonGame, config);
                } else {
                        initWithBannerAd(dungeonGame, config);
                }

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                makeInterstitalAd();

        }

        private void makeInterstitalAd() {
                if (interstitalAdUnitId == null)
                        return;
                interstitialAd = new InterstitialAd(this);
                interstitialAd.setAdUnitId(interstitalAdUnitId);
                interstitialAd.setAdListener(new InterstitialAdListener());
        }

        private void initWithBannerAd(ApplicationListener app, AndroidApplicationConfiguration config) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


                RelativeLayout layout = new RelativeLayout(this);

                View gameView = initializeForView(app, config);

                layout.addView(gameView);

                bannerAdView = new AdView(this);
                bannerAdView.setAdSize(AdSize.SMART_BANNER);
                bannerAdView.setAdUnitId(bannerAdUnitId);
                //adView.setId(12345);// this is an arbitrary id, allows for relative positioning in createGameView()


                AdRequest adRequest = makeAdRequest();
                try {
                        bannerAdView.loadAd(adRequest);
                } catch (NoClassDefFoundError e) {
                        System.err.println("------------- no class def found error");
                        e.printStackTrace();
                }


                RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP); // , RelativeLayout.TRUE
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); // , RelativeLayout.TRUE

                layout.addView(bannerAdView, params);

                setContentView(layout);
        }

        @Override
        public void onResume() {
                super.onResume();
                if (bannerAdView != null) bannerAdView.resume();
        }

        @Override
        public void onPause() {
                if (bannerAdView != null) bannerAdView.pause();
                super.onPause();
        }

        @Override
        public void onDestroy() {
                if (bannerAdView != null) bannerAdView.destroy();
                super.onDestroy();
        }

        private AdRequest makeAdRequest() {
                AdRequest.Builder builder = new AdRequest.Builder();
                //String[] testDeviceIds = null;//gm.getAdSpace().getTestDeviceIds();

                if (testDeviceIds != null) {
                        for (int i = 0; i < testDeviceIds.length; i++) {
                                builder.addTestDevice(testDeviceIds[i]);
                        }
                }

                builder.setGender(0); // gm.getAdSpace().getGender()
                String[] keywords = null;//gm.getAdSpace().getKeywords();
                if (keywords != null) {
                        for (int i = 0; i < keywords.length; i++) {
                                builder.addKeyword(keywords[i]);
                        }

                }

                AdRequest adRequest = builder.build();
                return adRequest;
        }


        public float getAdMargin() {
                String adBannerUnitId = bannerAdUnitId;
                if (adBannerUnitId == null) {
                        return 0;
                }
                return AdSize.SMART_BANNER.getHeightInPixels(this);
        }

        private volatile int interstitialAdState = 0;
        private boolean interstitialShowAd = false;

        /*
         * 0 = not loaded
         * 1 = loading
         * 2 = loaded
         * 3 = visible
         */
        @Override
        public int getInterstitialAdState() {
                return interstitialAdState;
        }

        @Override
        public void loadInterstitialAd() {
                runOnUiThread(new Runnable() {
                        public void run() {
                                if (interstitialAdState != 0) {
                                        return;
                                }

                                try {
                                        interstitialAdState = 1;
                                        interstitialShowAd = false;
                                        interstitialAd.loadAd(makeAdRequest());
                                } catch (java.lang.NoClassDefFoundError e) {
                                        System.out.println("-------------messed up error was thrown:   " + e.getMessage());
                                        e.printStackTrace();
                                }
                        }

                });
        }

        @Override
        public void showInterstitialAd() {
                runOnUiThread(new Runnable() {
                        public void run() {
                                interstitialShowAd = true;
                                if (interstitialAdState == 2) {
                                        if (interstitialAd.isLoaded()) {
                                                interstitialAd.show();
                                        } else {
                                                throw new AssertionError("ad not loaded");
                                        }
                                } else if (interstitialAdState == 0) {
                                        interstitialAdState = 1;
                                        interstitialAd.loadAd(makeAdRequest());
                                }

                        }

                });
        }

        private class InterstitialAdListener extends AdListener {
                @Override
                public void onAdClosed() {
                        interstitialAdState = 0;
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                        interstitialAdState = 0;
                }

                @Override
                public void onAdLeftApplication() {
                        super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                        interstitialAdState = 3;
                }

                @Override
                public void onAdLoaded() {
                        interstitialAdState = 2;
                        if (interstitialShowAd) {
                                if (interstitialAd.isLoaded()) {
                                        interstitialAd.show();
                                } else {
                                        throw new AssertionError("ad not loaded");
                                }
                        }
                }
        }
}
