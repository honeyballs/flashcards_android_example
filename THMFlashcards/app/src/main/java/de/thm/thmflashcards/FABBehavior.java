package de.thm.thmflashcards;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Yannick Bals on 01.12.2017.
 */

//This class is set in layout files containing FABs
public class FABBehavior extends FloatingActionButton.Behavior {

    public FABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        //Child is the FAB
        //If we scroll up, the button is shown
        if (dyConsumed > 0) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            //Check how far the button has to be pushed up
            int bottomMargin = layoutParams.bottomMargin;
            //Animate the button
            child.animate().translationY(child.getHeight() + bottomMargin)
                    .setInterpolator(new LinearInterpolator()).start();
        } else if (dyConsumed < 0) {
            //Pull the button offscreen
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }

    }

    //Whether the scrolling is interesting to us or not. If it is vertical, the button is animated
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
