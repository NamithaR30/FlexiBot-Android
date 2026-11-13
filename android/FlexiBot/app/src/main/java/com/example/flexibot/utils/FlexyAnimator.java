package com.example.flexibot.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.Map;
import java.util.WeakHashMap;

public class FlexyAnimator {

    private static final Map<View, AnimatorSet> ACTIVE_ANIMATORS = new WeakHashMap<>();

    public static void startIdleAnimation(View target) {
        if (target == null) return;
        stopAnimation(target);

        float floatDistance = -5f * target.getResources().getDisplayMetrics().density;

        ObjectAnimator floatY = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0f, floatDistance, 0f);
        floatY.setDuration(1900);
        floatY.setRepeatCount(ValueAnimator.INFINITE);
        floatY.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator gentleTilt = ObjectAnimator.ofFloat(target, View.ROTATION, -2f, 2f, -2f);
        gentleTilt.setDuration(2400);
        gentleTilt.setRepeatCount(ValueAnimator.INFINITE);
        gentleTilt.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(floatY, gentleTilt);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void playThinkingAnimation(View target) {
        if (target == null) return;
        stopAnimation(target);

        ObjectAnimator tilt = ObjectAnimator.ofFloat(target, View.ROTATION, -7f, 7f, -7f);
        tilt.setDuration(750);
        tilt.setRepeatCount(ValueAnimator.INFINITE);
        tilt.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator pulseX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.96f, 1.05f, 0.96f);
        pulseX.setDuration(750);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.96f, 1.05f, 0.96f);
        pulseY.setDuration(750);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(tilt, pulseX, pulseY);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void playHappyBounce(View target, boolean resumeIdleAfter) {
        if (target == null) return;
        stopAnimation(target);

        float jumpHeight = -10f * target.getResources().getDisplayMetrics().density;

        ObjectAnimator jumpY = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0f, jumpHeight, 0f);
        jumpY.setDuration(420);
        jumpY.setInterpolator(new OvershootInterpolator(2.0f));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.88f, 1.08f, 1f);
        scaleX.setDuration(420);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.88f, 1.08f, 1f);
        scaleY.setDuration(420);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(jumpY, scaleX, scaleY);
        attachCompletionHandler(target, set, resumeIdleAfter);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void playExcitedJump(View target, boolean resumeIdleAfter) {
        if (target == null) return;
        stopAnimation(target);

        float jumpHeight = -14f * target.getResources().getDisplayMetrics().density;

        ObjectAnimator doubleJump = ObjectAnimator.ofFloat(
                target, View.TRANSLATION_Y, 0f, jumpHeight, 0f, jumpHeight * 0.6f, 0f
        );
        doubleJump.setDuration(680);
        doubleJump.setInterpolator(new OvershootInterpolator(1.8f));

        ObjectAnimator popX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.85f, 1.14f, 0.96f, 1.06f, 1f);
        popX.setDuration(680);

        ObjectAnimator popY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.85f, 1.14f, 0.96f, 1.06f, 1f);
        popY.setDuration(680);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(doubleJump, popX, popY);
        attachCompletionHandler(target, set, resumeIdleAfter);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void playEncouragingNod(View target, boolean resumeIdleAfter) {
        if (target == null) return;
        stopAnimation(target);

        ObjectAnimator nod = ObjectAnimator.ofFloat(target, View.ROTATION, 0f, -10f, 10f, -6f, 6f, 0f);
        nod.setDuration(550);
        nod.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(nod);
        attachCompletionHandler(target, set, resumeIdleAfter);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void playCelebratingSpin(View target, boolean resumeIdleAfter) {
        if (target == null) return;
        stopAnimation(target);

        float jumpHeight = -14f * target.getResources().getDisplayMetrics().density;

        ObjectAnimator jump = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0f, jumpHeight, 0f);
        jump.setDuration(750);

        ObjectAnimator wiggle = ObjectAnimator.ofFloat(target, View.ROTATION, 0f, -15f, 15f, -10f, 10f, 0f);
        wiggle.setDuration(750);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.9f, 1.18f, 1f);
        scaleX.setDuration(750);
        scaleX.setInterpolator(new OvershootInterpolator(2.2f));

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.9f, 1.18f, 1f);
        scaleY.setDuration(750);
        scaleY.setInterpolator(new OvershootInterpolator(2.2f));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(jump, wiggle, scaleX, scaleY);
        attachCompletionHandler(target, set, resumeIdleAfter);
        ACTIVE_ANIMATORS.put(target, set);
        set.start();
    }

    public static void stopAnimation(View target) {
        if (target == null) return;
        AnimatorSet existing = ACTIVE_ANIMATORS.remove(target);
        if (existing != null) {
            existing.removeAllListeners();
            existing.cancel();
        }
        target.animate().cancel();
        target.setTranslationX(0f);
        target.setTranslationY(0f);
        target.setScaleX(1f);
        target.setScaleY(1f);
        target.setRotation(0f);
    }

    private static void attachCompletionHandler(View target, AnimatorSet set, boolean resumeIdleAfter) {
        set.addListener(new AnimatorListenerAdapter() {
            private boolean wasCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ACTIVE_ANIMATORS.remove(target);
                if (!wasCancelled && resumeIdleAfter && target.isAttachedToWindow()) {
                    startIdleAnimation(target);
                }
            }
        });
    }
}