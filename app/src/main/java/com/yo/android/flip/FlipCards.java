/*
Copyright 2012 Aphid Mobile

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
	 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

package com.yo.android.flip;

import android.view.MotionEvent;
import android.view.View;

import junit.framework.Assert;

import javax.microedition.khronos.opengles.GL10;

public class FlipCards {

    private static final float ACCELERATION = 0.65f;
    private static final float MOVEMENT_RATE = 1.5f;
    private static final int MAX_TIP_ANGLE = 60;
    private static final int MAX_TOUCH_MOVE_ANGLE = 15;
    private static final float MIN_MOVEMENT = 4f;

    private static final int STATE_INIT = 0;
    private static final int STATE_TOUCH = 1;
    private static final int STATE_AUTO_ROTATE = 2;
    private static final int ONE_EIGHTY_DEGREES = 180;

    private ViewDualCards frontCards;
    private ViewDualCards backCards;

    private float accumulatedAngle = 0f;
    private boolean forward = true;
    private int animatedFrame = 0;
    private int state = STATE_INIT;

    private boolean orientationVertical = true;
    private float lastPosition = -1;

    private FlipViewController controller;

    private volatile boolean visible = false;

    private volatile boolean firstDrawFinished = false;

    private int maxIndex = 0;

    private int lastPageIndex;

    public FlipCards(FlipViewController controller, boolean orientationVertical) {
        this.controller = controller;

        frontCards = new ViewDualCards(orientationVertical);
        backCards = new ViewDualCards(orientationVertical);
        this.orientationVertical = orientationVertical;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isFirstDrawFinished() {
        return firstDrawFinished;
    }

    public void setFirstDrawFinished(boolean firstDrawFinished) {
        this.firstDrawFinished = firstDrawFinished;
    }

    boolean refreshPageView(View view) {
        boolean match = false;
        if (frontCards.getView() == view) {
            frontCards.resetWithIndex(frontCards.getIndex());
            match = true;
        }
        if (backCards.getView() == view) {
            backCards.resetWithIndex(backCards.getIndex());
            match = true;
        }

        return match;
    }

    boolean refreshPage(int pageIndex) {
        boolean match = false;
        if (frontCards.getIndex() == pageIndex) {
            frontCards.resetWithIndex(pageIndex);
            match = true;
        }
        if (backCards.getIndex() == pageIndex) {
            backCards.resetWithIndex(pageIndex);
            match = true;
        }

        return match;
    }

    void refreshAllPages() {
        frontCards.resetWithIndex(frontCards.getIndex());
        backCards.resetWithIndex(backCards.getIndex());
    }

    public void reloadTexture(int frontIndex, View frontView, int backIndex, View backView) {
        synchronized (this) {
            boolean
                    frontChanged =
                    frontCards.loadView(frontIndex, frontView, controller.getAnimationBitmapFormat());
            boolean
                    backChanged =
                    backCards.loadView(backIndex, backView, controller.getAnimationBitmapFormat());

            if (AphidLog.ENABLE_DEBUG) {
                AphidLog
                        .d("reloading texture: %s and %s; old views: %s, %s, front changed %s, back changed %s",
                                frontView, backView, frontCards.getView(), backCards.getView(), frontChanged,
                                backChanged);
            }

            if (AphidLog.ENABLE_DEBUG) {
                AphidLog.d("reloadTexture: activeIndex %d, front %d, back %d, angle %.1f",
                        getPageIndexFromAngle(accumulatedAngle), frontIndex, backIndex,
                        accumulatedAngle);
            }
        }
    }

    synchronized void resetSelection(int selection, int maxIndex) {
        UI.assertInMainThread();

        //stop flip animation when selection is manually changed

        this.maxIndex = maxIndex;
        setVisible(false);
        setState(STATE_INIT);
        accumulatedAngle = selection * ONE_EIGHTY_DEGREES;
        frontCards.resetWithIndex(selection);
        backCards.resetWithIndex(selection + 1 < maxIndex ? selection + 1 : -1);
        controller.postHideFlipAnimation();
    }

    public synchronized void draw(FlipRenderer renderer, GL10 gl) {
        frontCards.buildTexture(renderer, gl);
        backCards.buildTexture(renderer, gl);

        if (!TextureUtils.isValidTexture(frontCards.getTexture()) &&
                !TextureUtils.isValidTexture(backCards.getTexture())) {
            return;
        }

        if (!visible) {
            return;
        }

        switch (state) {
            case STATE_INIT:
            case STATE_TOUCH:
                break;
            case STATE_AUTO_ROTATE: {
                animatedFrame++;
                float delta = (forward ? ACCELERATION : -ACCELERATION) * animatedFrame % ONE_EIGHTY_DEGREES;

                float oldAngle = accumulatedAngle;

                accumulatedAngle += delta;
                //bouncing back after flip backward and over the first page
                if (oldAngle < 0) {
                    Assert.assertTrue(forward);
                    if (accumulatedAngle >= 0) {
                        accumulatedAngle = 0;
                        setState(STATE_INIT);
                    }
                } else {
                    flipForward(oldAngle);
                }
                if (state == STATE_INIT) {
                    controller.postHideFlipAnimation();
                } else {
                    controller.getSurfaceView().requestRender();
                }
            }
            break;
            default:
                AphidLog.e("Invalid state: " + state);
                break;
        }

        float angle = getDisplayAngle();
        if (angle < 0) {
            frontCards.getTopCard().setAxis(Card.AXIS_BOTTOM);
            frontCards.getTopCard().setAngle(-angle);
            frontCards.getTopCard().draw(gl);

            frontCards.getBottomCard().setAngle(0);
            frontCards.getBottomCard().draw(gl);
            //no need to draw backCards here
        } else {
            //render front view over back view
            if (angle < 90) {
                frontCards.getTopCard().setAngle(0);
                frontCards.getTopCard().draw(gl);

                backCards.getBottomCard().setAngle(0);
                backCards.getBottomCard().draw(gl);

                frontCards.getBottomCard().setAxis(Card.AXIS_TOP);
                frontCards.getBottomCard().setAngle(angle);
                frontCards.getBottomCard().draw(gl);
            } else {
                //render back view first
                frontCards.getTopCard().setAngle(0);
                frontCards.getTopCard().draw(gl);

                backCards.getTopCard().setAxis(Card.AXIS_BOTTOM);
                backCards.getTopCard().setAngle(ONE_EIGHTY_DEGREES - angle);
                backCards.getTopCard().draw(gl);

                backCards.getBottomCard().setAngle(0);
                backCards.getBottomCard().draw(gl);
            }
        }

        if ((frontCards.getView() == null || TextureUtils.isValidTexture(frontCards.getTexture())) &&
                (backCards.getView() == null || TextureUtils.isValidTexture(backCards.getTexture()))
                ) {
            firstDrawFinished = true;
        }
    }

    private void flipForward(float oldAngle) {
        //bouncing back after flip forward and over the last page
        if (frontCards.getIndex() == maxIndex - 1 && oldAngle > frontCards.getIndex()
                * ONE_EIGHTY_DEGREES) {
            Assert.assertTrue(!forward);
            if (accumulatedAngle <= frontCards.getIndex() * ONE_EIGHTY_DEGREES) {
                setState(STATE_INIT);
                accumulatedAngle = frontCards.getIndex() * ONE_EIGHTY_DEGREES;
            }
        } else {
            if (forward) {
                Assert.assertTrue(
                        "index of backCards should not be -1 when automatically flipping forward",
                        backCards.getIndex() != -1);
                if (accumulatedAngle >= backCards.getIndex() * ONE_EIGHTY_DEGREES) { //moved to the next page
                    accumulatedAngle = backCards.getIndex() * ONE_EIGHTY_DEGREES;
                    setState(STATE_INIT);
                    controller.postFlippedToView(backCards.getIndex());

                    swapCards();
                    backCards.resetWithIndex(frontCards.getIndex() + 1);
                }
            } else {
                //backward
                if (accumulatedAngle <= frontCards.getIndex() * ONE_EIGHTY_DEGREES) { //firstCards restored
                    accumulatedAngle = frontCards.getIndex() * ONE_EIGHTY_DEGREES;
                    setState(STATE_INIT);
                }
            }
        }
    }

    public void invalidateTexture() {
        frontCards.abandonTexture();
        backCards.abandonTexture();
    }

    public synchronized boolean handleTouchEvent(MotionEvent event, boolean isOnTouchEvent) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // remember page we started on...
                lastPageIndex = getPageIndexFromAngle(accumulatedAngle);
                lastPosition = orientationVertical ? event.getY() : event.getX();
                return isOnTouchEvent;
            case MotionEvent.ACTION_MOVE:
                float delta = orientationVertical ? (lastPosition - event.getY()) : (lastPosition - event.getX());

                if (Math.abs(delta) > controller.getTouchSlop()) {
                    setState(STATE_TOUCH);
                    forward = delta > 0;
                }
                if (state == STATE_TOUCH) {
                    //ignore small movements
                    if (Math.abs(delta) > MIN_MOVEMENT) {
                        forward = delta > 0;
                    }

                    controller.showFlipAnimation();

                    float angleDelta;
                    if (orientationVertical) {
                        angleDelta = ONE_EIGHTY_DEGREES * delta / controller.getContentHeight() * MOVEMENT_RATE;
                    } else {
                        angleDelta = ONE_EIGHTY_DEGREES * delta / controller.getContentWidth() * MOVEMENT_RATE;
                    }
                    //prevent large delta when moving too fast
                    if (Math.abs(angleDelta) > MAX_TOUCH_MOVE_ANGLE) {
                        angleDelta = Math.signum(angleDelta) * MAX_TOUCH_MOVE_ANGLE;
                    }
                    // do not flip more than one page with one touch...
                    if (Math.abs(getPageIndexFromAngle(accumulatedAngle + angleDelta) - lastPageIndex) <= 1) {
                        accumulatedAngle += angleDelta;
                    }

                    //Bounce the page for the first and the last page
                    if (frontCards.getIndex() == maxIndex - 1) { //the last page
                        if (accumulatedAngle > frontCards.getIndex() * ONE_EIGHTY_DEGREES) {
                            accumulatedAngle =
                                    Math.min(accumulatedAngle,
                                            controller.isOverFlipEnabled() ? (frontCards.getIndex() * ONE_EIGHTY_DEGREES
                                                    + MAX_TIP_ANGLE)
                                                    : (frontCards.getIndex() * ONE_EIGHTY_DEGREES));
                        }
                    }

                    if (accumulatedAngle < 0) {
                        accumulatedAngle =
                                Math.max(accumulatedAngle, controller.isOverFlipEnabled() ? -MAX_TIP_ANGLE : 0);
                    }

                    int anglePageIndex = getPageIndexFromAngle(accumulatedAngle);

                    if (accumulatedAngle >= 0) {
                        if (anglePageIndex != frontCards.getIndex()) {
                            if (anglePageIndex == frontCards.getIndex() - 1) { //moved to previous page
                                swapCards(); //frontCards becomes the backCards
                                frontCards.resetWithIndex(backCards.getIndex() - 1);
                                controller.flippedToView(anglePageIndex, false);
                            } else if (anglePageIndex == frontCards.getIndex() + 1) { //moved to next page
                                swapCards();
                                backCards.resetWithIndex(frontCards.getIndex() + 1);
                                controller.flippedToView(anglePageIndex, false);
                            } else {
                                throw new RuntimeException(AphidLog.format(
                                        "Inconsistent states: anglePageIndex: %d, accumulatedAngle %.1f, frontCards %d, backCards %d",
                                        anglePageIndex, accumulatedAngle, frontCards.getIndex(), backCards.getIndex()));
                            }
                        }
                    }

                    lastPosition = orientationVertical ? event.getY() : event.getX();

                    controller.getSurfaceView().requestRender();
                    return true;
                }

                return isOnTouchEvent;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (state == STATE_TOUCH) {
                    if (accumulatedAngle < 0) {
                        forward = true;
                    } else if (accumulatedAngle >= frontCards.getIndex() * ONE_EIGHTY_DEGREES
                            && frontCards.getIndex() == maxIndex - 1) {
                        forward = false;
                    }

                    setState(STATE_AUTO_ROTATE);
                    controller.getSurfaceView().requestRender();
                }
                return isOnTouchEvent;
        }

        return false;
    }

    private void swapCards() {
        ViewDualCards tmp = frontCards;
        frontCards = backCards;
        backCards = tmp;
    }

    private void setState(int state) {
        if (this.state != state) {
            this.state = state;
            animatedFrame = 0;
        }
    }

    private int getPageIndexFromAngle(float angle) {
        return ((int) angle) / ONE_EIGHTY_DEGREES;
    }

    private float getDisplayAngle() {
        return accumulatedAngle % ONE_EIGHTY_DEGREES;
    }
}