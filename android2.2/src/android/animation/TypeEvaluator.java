/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.animation;

/**
 * 用于{@link ValueAnimator#setEvaluator(TypeEvaluator)}
 * 方法的接口，Evalutor允许开发者可以再任意类型的 属性上执行动画，给那些不能够自动理解的类型提供自定义的evalutor，并被动画系统使用。 <br>
 * Interface for use with the {@link ValueAnimator#setEvaluator(TypeEvaluator)}
 * function. Evaluators allow developers to create animations on arbitrary
 * property types, by allowing them to supply custom evaulators for types that
 * are not automatically understood and used by the animation system.
 * 
 * @see ValueAnimator#setEvaluator(TypeEvaluator)
 */
public interface TypeEvaluator<T> {

	/**
	 * 返回线性Interpolating动画开始和结束的结果。 <br>
	 * This function returns the result of linearly interpolating the start and
	 * end values, with <code>fraction</code> representing the proportion
	 * between the start and end values. The calculation is a simple parametric
	 * calculation: <code>result = x0 + t * (v1 - v0)</code>, where
	 * <code>x0</code> is <code>startValue</code>, <code>x1</code> is
	 * <code>endValue</code>, and <code>t</code> is <code>fraction</code>.
	 * 
	 * @param fraction
	 *            The fraction from the starting to the ending values
	 * @param startValue
	 *            The start value.
	 * @param endValue
	 *            The end value.
	 * @return A linear interpolation between the start and end values, given
	 *         the <code>fraction</code> parameter.
	 */
	public T evaluate(float fraction, T startValue, T endValue);

}