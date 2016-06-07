package com.rakshith.manthra.sathya.pickcolorfromcamera;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by rakshith on 7/6/16.
 */
public class ViewUtils {
    @BindingAdapter({"android:src"})
    public static void loadImage(ImageView view, int res) {
        try {
            view.setImageResource(res);
        } catch (Exception e) {

        }
    }

    @BindingAdapter({"android:src"})
    public static void loadImage(ImageView view, Drawable drawable) {
        try {
            view.setImageDrawable(drawable);
        } catch (Exception e) {

        }
    }

    @BindingAdapter({"android:src"})
    public static void loadImageResource(ImageView view, String name) {
        try {
            Context context = view.getContext();
            int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            view.setImageResource(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
