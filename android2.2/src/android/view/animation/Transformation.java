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

import android.graphics.Matrix;

import java.io.PrintWriter;

/**
 * 在一个动画的某一个时间点执行的转换<br>
 * Defines the transformation to be applied at one point in time of an
 * Animation.
 * 
 */
public class Transformation {
	/**
	 * alpha和矩阵都不影响 Indicates a transformation that has no effect (alpha = 1 and
	 * identity matrix.)
	 */
	public static int TYPE_IDENTITY = 0x0;
	/**
	 * 仅alpha Indicates a transformation that applies an alpha only (uses an
	 * identity matrix.)
	 */
	public static int TYPE_ALPHA = 0x1;
	/**
	 * 仅矩阵 Indicates a transformation that applies a matrix only (alpha = 1.)
	 */
	public static int TYPE_MATRIX = 0x2;
	/**
	 * 影响alpha和矩阵 Indicates a transformation that applies an alpha and a matrix.
	 */
	public static int TYPE_BOTH = TYPE_ALPHA | TYPE_MATRIX;
	
	/**
	 * 图像该矩阵
	 */
	protected Matrix mMatrix;
	/**
	 * alpha通道值
	 */
	protected float mAlpha;
	/**
	 * 转换值
	 */
	protected int mTransformationType;

	/**
	 * Creates a new transformation with alpha = 1 and the identity matrix.
	 */
	public Transformation() {
		clear();
	}

	/**
	 * Reset the transformation to a state that leaves the object being animated
	 * in an unmodified state. The transformation type is {@link #TYPE_BOTH} by
	 * default.
	 */
	public void clear() {
		if (mMatrix == null) {
			mMatrix = new Matrix();
		} else {
			mMatrix.reset();
		}
		mAlpha = 1.0f;
		mTransformationType = TYPE_BOTH;
	}

	/**
	 * Indicates the nature of this transformation.
	 * 
	 * @return {@link #TYPE_ALPHA}, {@link #TYPE_MATRIX}, {@link #TYPE_BOTH} or
	 *         {@link #TYPE_IDENTITY}.
	 */
	public int getTransformationType() {
		return mTransformationType;
	}

	/**
	 * Sets the transformation type.
	 * 
	 * @param transformationType
	 *            One of {@link #TYPE_ALPHA}, {@link #TYPE_MATRIX},
	 *            {@link #TYPE_BOTH} or {@link #TYPE_IDENTITY}.
	 */
	public void setTransformationType(int transformationType) {
		mTransformationType = transformationType;
	}

	/**
	 * Clones the specified transformation.
	 * 
	 * @param t
	 *            The transformation to clone.
	 */
	public void set(Transformation t) {
		mAlpha = t.getAlpha();
		mMatrix.set(t.getMatrix());
		mTransformationType = t.getTransformationType();
	}

	/**
	 * 讲一个转换应用到一个已经存在的转换上，例如执行一个缩放效果
	 * <br>
	 * Apply this Transformation to an existing Transformation, e.g. apply a
	 * scale effect to something that has already been rotated.
	 * 
	 * @param t
	 */
	public void compose(Transformation t) {
		mAlpha *= t.getAlpha();
		mMatrix.preConcat(t.getMatrix());
	}

	/**
	 * @return The 3x3 Matrix representing the trnasformation to apply to the
	 *         coordinates of the object being animated
	 */
	public Matrix getMatrix() {
		return mMatrix;
	}

	/**
	 * 设置透明度，1.0代表完全不透明，0.0代表完全透明
	 * Sets the degree of transparency
	 * 
	 * @param alpha
	 *            1.0 means fully opaqe and 0.0 means fully transparent
	 */
	public void setAlpha(float alpha) {
		mAlpha = alpha;
	}

	/**
	 * @return The degree of transparency
	 */
	public float getAlpha() {
		return mAlpha;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("Transformation");
		toShortString(sb);
		return sb.toString();
	}

	/**
	 * Return a string representation of the transformation in a compact form.
	 */
	public String toShortString() {
		StringBuilder sb = new StringBuilder(64);
		toShortString(sb);
		return sb.toString();
	}

	/**
	 * @hide
	 */
	public void toShortString(StringBuilder sb) {
		sb.append("{alpha=");
		sb.append(mAlpha);
		sb.append(" matrix=");
		mMatrix.toShortString(sb);
		sb.append('}');
	}

	/**
	 * Print short string, to optimize dumping.
	 * 
	 * @hide
	 */
	public void printShortString(PrintWriter pw) {
		pw.print("{alpha=");
		pw.print(mAlpha);
		pw.print(" matrix=");
		mMatrix.printShortString(pw);
		pw.print('}');
	}
}
