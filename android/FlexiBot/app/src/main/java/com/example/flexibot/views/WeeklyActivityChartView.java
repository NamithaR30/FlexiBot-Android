package com.example.flexibot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.flexibot.R;

public class WeeklyActivityChartView extends View {

    private String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private int[] values = {0, 0, 0, 0, 0, 0, 0};
    private String unitSuffix = "m";

    private Paint barActivePaint;
    private Paint barEmptyPaint;
    private Paint textPaint;
    private Paint valueTextPaint;
    private final RectF barRect = new RectF();

    public WeeklyActivityChartView(Context context) {
        super(context);
        init(context);
    }

    public WeeklyActivityChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeeklyActivityChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int redColor = ContextCompat.getColor(context, R.color.electric_red);
        int greyColor = ContextCompat.getColor(context, R.color.grey_text);
        int whiteColor = ContextCompat.getColor(context, R.color.white);

        barActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barActivePaint.setColor(redColor);
        barActivePaint.setStyle(Paint.Style.FILL);

        barEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barEmptyPaint.setColor(Color.parseColor("#2A2A2A"));
        barEmptyPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(greyColor);
        textPaint.setTextSize(spToPx(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valueTextPaint.setColor(whiteColor);
        valueTextPaint.setTextSize(spToPx(10));
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setWeeklyData(String[] labels, int[] dailyValues, String unitSuffix) {
        if (labels != null && dailyValues != null && labels.length == dailyValues.length) {
            this.dayLabels = labels;
            this.values = dailyValues;
            this.unitSuffix = unitSuffix != null ? unitSuffix : "";
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || values.length == 0) return;

        float width = getWidth();
        float height = getHeight();

        float padLeft = dpToPx(12);
        float padRight = dpToPx(12);
        float padTop = dpToPx(22);
        float padBottom = dpToPx(24);

        float usableWidth = width - padLeft - padRight;
        float maxBarHeight = height - padTop - padBottom;

        int maxVal = 1;
        for (int v : values) {
            if (v > maxVal) maxVal = v;
        }

        int count = values.length;
        float slotWidth = usableWidth / count;
        float barWidth = Math.min(dpToPx(24), slotWidth * 0.52f);
        float cornerRadius = dpToPx(6);

        for (int i = 0; i < count; i++) {
            float centerX = padLeft + (slotWidth * i) + (slotWidth / 2f);
            float bottomY = height - padBottom;

            // Background full-height track bar
            barRect.set(centerX - barWidth / 2f, padTop, centerX + barWidth / 2f, bottomY);
            canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barEmptyPaint);

            // Foreground active value bar
            if (values[i] > 0) {
                float ratio = (float) values[i] / (float) maxVal;
                float barHeight = Math.max(dpToPx(6), maxBarHeight * ratio);
                float topY = bottomY - barHeight;

                barRect.set(centerX - barWidth / 2f, topY, centerX + barWidth / 2f, bottomY);
                canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barActivePaint);

                canvas.drawText(values[i] + unitSuffix, centerX, topY - dpToPx(6), valueTextPaint);
            }

            canvas.drawText(dayLabels[i], centerX, height - dpToPx(6), textPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}