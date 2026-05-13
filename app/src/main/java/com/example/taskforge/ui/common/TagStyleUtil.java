package com.example.taskforge.ui.common;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.example.taskforge.R;

public final class TagStyleUtil {

    private TagStyleUtil() {
    }

    public static void applyTagStyle(TextView view, String label) {
        @ColorInt int baseColor = resolveBaseColor(view, label);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(view, 12));
        drawable.setColor(ColorUtils.setAlphaComponent(baseColor, 56));
        drawable.setStroke((int) dp(view, 1), baseColor);

        view.setBackground(drawable);
        view.setTextColor(view.getContext().getColor(R.color.ui_heading_text));
        int horizontal = (int) dp(view, 10);
        int vertical = (int) dp(view, 4);
        view.setPadding(horizontal, vertical, horizontal, vertical);
    }

    @ColorInt
    private static int resolveBaseColor(TextView view, String label) {
        String normalized = label == null ? "" : label.trim().toLowerCase();
        if (normalized.contains("todo") || normalized.contains("to do")) {
            return view.getContext().getColor(R.color.ui_tag_todo);
        }
        if (normalized.contains("inprogress") || normalized.contains("in progress") || normalized.contains("inproggress")) {
            return view.getContext().getColor(R.color.ui_tag_in_progress);
        }
        if (normalized.contains("done")) {
            return view.getContext().getColor(R.color.ui_tag_done);
        }

        int[] palette = new int[]{
                view.getContext().getColor(R.color.ui_tag_todo),
                view.getContext().getColor(R.color.ui_tag_in_progress),
                view.getContext().getColor(R.color.ui_tag_done),
                view.getContext().getColor(R.color.ui_tag_info)
        };
        int index = Math.abs(normalized.hashCode()) % palette.length;
        return palette[index];
    }

    private static float dp(TextView view, int value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                view.getResources().getDisplayMetrics()
        );
    }
}
