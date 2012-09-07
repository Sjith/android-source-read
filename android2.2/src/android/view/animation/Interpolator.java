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

/**
 * Interpolator（插值器）表示动画改变的速度。允许基本的动画效果(alpha,scale,translate,rotate)进行加速，减速，
 * 重复等等。<br>
 * An interpolator defines the rate of change of an animation. This allows the
 * basic animation effects (alpha, scale, translate, rotate) to be accelerated,
 * decelerated, repeated, etc.
 */
public interface Interpolator {

	/**
	 * 将时间轴上的一个点映射到一个倍数，以执行动画的一次转换。
	 * <br>
	 * Maps a point on the timeline to a multiplier to be applied to the
	 * transformations of an animation.
	 * 
	 * @param input
	 * 			  0到1.0之间的一个float型数值，0代表开始，1.0代表结束。
	 *            A value between 0 and 1.0 indicating our current point in the
	 *            animation where 0 represents the start and 1.0 represents the
	 *            end
	 * @return The interpolation value. This value can be more than 1.0 for
	 *         Interpolators which overshoot their targets, or less than 0 for
	 *         Interpolators that undershoot their targets.
	 */
	float getInterpolation(float input);

}
