package com.example.flexibot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.flexibot.R;
import com.example.flexibot.models.WeightEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightTrendChartView extends View {

    private final List<WeightEntry> chronologicalEntries = new ArrayList<>();
    private final SimpleDateFormat labelDateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint dotCenterPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint emptyTextPaint;

    public WeightTrendChartView(Context context) {
        super(context);
        init(context);
    }

    public WeightTrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeightTrendChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int redColor = ContextCompat.getColor(context, R.color.electric_red);
        int greyColor = ContextCompat.getColor(context, R.color.grey_text);
        int whiteColor = ContextCompat.getColor(context, R.color.white);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(redColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(3));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(redColor);
        dotPaint.setStyle(Paint.Style.FILL);

        dotCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotCenterPaint.setColor(whiteColor);
        dotCenterPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#2A2A2A"));
        gridPaint.setStrokeWidth(dpToPx(1));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(greyColor);
        textPaint.setTextSize(spToPx(10));
        textPaint.setTextAlign(Paint.Align.CENTER);

        emptyTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyTextPaint.setColor(greyColor);
        emptyTextPaint.setTextSize(spToPx(13));
        emptyTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setEntries(List<WeightEntry> entriesOldestFirst) {
        chronologicalEntries.clear();
        if (entriesOldestFirst != null) {
            // Keep up to the 10 most recent chronological points for clean spacing
            int start = Math.max(0, entriesOldestFirst.size() - 10);
            chronologicalEntries.addAll(entriesOldestFirst.subList(start, entriesOldestFirst.size()));
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (chronologicalEntries.isEmpty()) {
            canvas.drawText(
                    "Log weight entries in Trackers to view your trend chart",
                    width / 2f,
                    height / 2f,
                    emptyTextPaint
            );
            return;
        }

        float padLeft = dpToPx(24);
        float padRight = dpToPx(24);
        float padTop = dpToPx(24);
        float padBottom = dpToPx(28);

        float chartWidth = width - padLeft - padRight;
        float chartHeight = height - padTop - padBottom;

        // Draw 3 subtle horizontal gridlines
        for (int i = 0; i <= 2; i++) {
            float y = padTop + (chartHeight * i / 2f);
            canvas.drawLine(padLeft, y, width - padRight, y, gridPaint);
        }

        double minWeight = Double.MAX_VALUE;
        double maxWeight = Double.MIN_VALUE;
        for (WeightEntry e : chronologicalEntries) {
            if (e.getWeightKg() < minWeight) minWeight = e.getWeightKg();
            if (e.getWeightKg() > maxWeight) maxWeight = e.getWeightKg();
        }

        // Prevent flat-line division by zero
        if (Math.abs(maxWeight - minWeight) < 0.5) {
            minWeight -= 1.0;
            maxWeight += 1.0;
        } else {
            double padding = (maxWeight - minWeight) * 0.15;
            minWeight -= padding;
            maxWeight += padding;
        }

        int count = chronologicalEntries.size();
        float[] xs = new float[count];
        float[] ys = new float[count];

        for (int i = 0; i < count; i++) {
            WeightEntry entry = chronologicalEntries.get(i);
            xs[i] = count == 1
                    ? padLeft + chartWidth / 2f
                    : padLeft + (chartWidth * i / (float) (count - 1));

            double normalized = (entry.getWeightKg() - minWeight) / (maxWeight - minWeight);
            ys[i] = (float) (padTop + chartHeight - (normalized * chartHeight));
        }

        if (count > 1) {
            Path linePath = new Path();
            Path areaPath = new Path();

            linePath.moveTo(xs[0], ys[0]);
            areaPath.moveTo(xs[0], padTop + chartHeight);
            areaPath.lineTo(xs[0], ys[0]);

            for (int i = 1; i < count; i++) {
                linePath.lineTo(xs[i], ys[i]);
                areaPath.lineTo(xs[i], ys[i]);
            }

            areaPath.lineTo(xs[count - 1], padTop + chartHeight);
            areaPath.close();

            int redColor = ContextCompat.getColor(getContext(), R.color.electric_red);
            fillPaint.setShader(new LinearGradient(
                    0, padTop, 0, padTop + chartHeight,
                    Color.argb(90, Color.red(redColor), Color.green(redColor), Color.blue(redColor)),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));

            canvas.drawPath(areaPath, fillPaint);
            canvas.drawPath(linePath, linePaint);
        }

        // Draw points, weight labels, and date labels
        for (int i = 0; i < count; i++) {
            WeightEntry entry = chronologicalEntries.get(i);
            canvas.drawCircle(xs[i], ys[i], dpToPx(5), dotPaint);
            canvas.drawCircle(xs[i], ys[i], dpToPx(2.2f), dotCenterPaint);

            String weightLabel = String.format(Locale.getDefault(), "%.1f", entry.getWeightKg());
            canvas.drawText(weightLabel, xs[i], ys[i] - dpToPx(9), textPaint);

            String dateLabel = labelDateFormat.format(new Date(entry.getTimestamp()));
            canvas.drawText(dateLabel, xs[i], height - dpToPx(6), textPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}