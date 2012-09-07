/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * 该插值器的改动速度开始时较慢，然后增加。 <br>
 * An interpolator where the rate of change starts out slowly and and then
 * accelerates.
 * 
 */
public class AccelerateInterpolator implements Interpolator {
	/**
	 * 减缓因子
	 */
	private final float mFactor;
	/**
	 * 双倍因子
	 */
	private final double mDoubleFactor;

	public AccelerateInterpolator() {
		mFactor = 1.0f;
		mDoubleFactor = 2.0;
	}

	/**
	 * Constructor
	 * 
	 * @param factor
	 *            动画使用的速度因此，如果因子为1.0，则产生y=x^2的抛物线.增加因子数炒锅1.0则会产生一个夸张的效果（开始很慢，
	 *            结束很快)<br>
	 *            Degree to which the animation should be eased. Seting factor
	 *            to 1.0f produces a y=x^2 parabola. Increasing factor above
	 *            1.0f exaggerates the ease-in effect (i.e., it starts even
	 *            slower and ends evens faster)
	 */
	public AccelerateInterpolator(float factor) {
		mFactor = factor;
		mDoubleFactor = 2 * mFactor;
	}

	public AccelerateInterpolator(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				com.android.internal.R.styleable.AccelerateInterpolator);

		mFactor = a.getFloat(
				com.android.internal.R.styleable.AccelerateInterpolator_factor,
				1.0f);
		mDoubleFactor = 2 * mFactor;

		a.recycle();
	}

	public float getInterpolation(float input) {
		if (mFactor == 1.0f) {
			return input * input;
		} else {
			return (float) Math.pow(input, mDoubleFactor);
		}
	}
}
