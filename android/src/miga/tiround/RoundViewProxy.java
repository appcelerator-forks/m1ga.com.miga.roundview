package com.miga.roundview;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.view.TiDrawableReference;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.proxy.TiViewProxy;
import android.app.Activity;
import org.appcelerator.titanium.TiApplication;
import android.view.LayoutInflater;
import android.view.View;
import java.io.IOException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.makeramen.roundedimageview.RoundedImageView;
import android.graphics.Color;

@Kroll.proxy(creatableInModule = TiRoundModule.class)
public class RoundViewProxy extends TiViewProxy {

    ImageView mVideoView;
    TiApplication appContext;
    Activity activity;
    String imageSrc;
    int borderWidth = 0;
    int cornerRadius = 0;
    String borderColor = "#000000";
    boolean isOval = false;
    boolean isMutated = true;

    public RoundViewProxy() {
        super();
        appContext = TiApplication.getInstance();
        activity   = appContext.getCurrentActivity();
    }

    @Override
    public TiUIView createView(Activity activity) {
        TiUIView view = new RoundView(this);
        return view;
    }

    // Handle creation options
    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);

        if (options.containsKey("image")) {
            imageSrc = options.getString("image");
        }

        if (options.containsKey("cornerradius")) {
            cornerRadius = options.getInt("cornerradius");
        }
        if (options.containsKey("borderwidth")) {
            borderWidth = options.getInt("borderwidth");
        }
        if (options.containsKey("bordercolor")) {
            borderColor = options.getString("bordercolor");
        }

        if (options.containsKey("isOval")) {
            isOval = options.getBoolean("isOval");
        }
        if (options.containsKey("isMutated")) {
            isMutated = options.getBoolean("isMutated");
        }
    }

    private class RoundView extends TiUIView {
        // create view
        public RoundView(final TiViewProxy proxy) {
            super(proxy);

            String packageName = proxy.getActivity().getPackageName();
            Resources resources = proxy.getActivity().getResources();
            View videoWrapper;
            int resId_videoHolder = -1;
            int resId_video       = -1;

            resId_videoHolder = resources.getIdentifier("layout", "layout", packageName);
            resId_video       = resources.getIdentifier("roundimage", "id", packageName);

            LayoutInflater inflater     = LayoutInflater.from(getActivity());
            videoWrapper = inflater.inflate(resId_videoHolder, null);

            TiBlob imgObj = loadImageFromApplication(imageSrc);
            TiDrawableReference ref = TiDrawableReference.fromBlob(proxy.getActivity(), imgObj);

            RoundedImageView circularImageView = (RoundedImageView)videoWrapper.findViewById(resId_video);
            circularImageView.setImageBitmap(ref.getBitmap());
            circularImageView.setBorderColor(Color.parseColor(borderColor));
            if (borderWidth > 0) {
                circularImageView.setBorderWidth((float)borderWidth);
            }
            //circularImageView.setScaleType(ScaleType.CENTER_CROP);
            circularImageView.setCornerRadius((float)cornerRadius);
            circularImageView.mutateBackground(isMutated);
            // circularImageView.setImageDrawable(drawable);
            // circularImageView.setBackground(backgroundDrawable);
            circularImageView.setOval(isOval);
            // circularImageView.setTileModeX(Shader.TileMode.REPEAT);
            // circularImageView.setTileModeY(Shader.TileMode.REPEAT);
            setNativeView(videoWrapper);
        }

        @Override
        public void processProperties(KrollDict d) {
            super.processProperties(d);
        }

        private String getPathToApplicationAsset(String assetName) {
            String result = resolveUrl(null, assetName);
            return result;
        }

        public TiBlob loadImageFromApplication(String imageName) {
            TiBlob result = null;
            try {
                // Load the image from the application assets
                String url = getPathToApplicationAsset(imageName);
                TiBaseFile file = TiFileFactory.createTitaniumFile(new String[] {
                    url
                }, false);
                Bitmap bitmap = TiUIHelper.createBitmap(file.getInputStream());
                result = TiBlob.blobFromImage(bitmap);
            } catch (IOException e) {
                Log.e("RoundView", " EXCEPTION - IO");
            }
            return result;
        }

    }
}
