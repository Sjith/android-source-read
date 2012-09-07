/*
 * Copyright (C) 2007 The Android Open Source Project
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.util.Xml;
import android.os.SystemClock;

import java.io.IOException;

/**
 * 使用animation的工具 <br>
 * Defines common utilities for working with animations.
 * 
 */
public class AnimationUtils {
	/**
	 * 返回动画的当前时间以毫秒为单位。 <br>
	 * Returns the current animation time in milliseconds. This time should be
	 * used when invoking {@link Animation#setStartTime(long)}. Refer to
	 * {@link android.os.SystemClock} for more information about the different
	 * available clocks. The clock used by this method is <em>not</em> the
	 * "wall" clock (it is not {@link System#currentTimeMillis}).
	 * 
	 * @return the current animation time in milliseconds
	 * 
	 * @see android.os.SystemClock
	 */
	public static long currentAnimationTimeMillis() {
		return SystemClock.uptimeMillis();
	}

	/**
	 * 从一个资源加载动画 <br>
	 * Loads an {@link Animation} object from a resource
	 * 
	 * @param context
	 *            Application context used to access resources
	 * @param id
	 *            动画的资源<br>
	 *            The resource id of the animation to load
	 * @return The animation object reference by the specified id
	 * @throws NotFoundException
	 *             when the animation cannot be loaded
	 */
	public static Animation loadAnimation(Context context, int id)
			throws NotFoundException {

		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createAnimationFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private static Animation createAnimationFromXml(Context c,
			XmlPullParser parser) throws XmlPullParserException, IOException {

		return createAnimationFromXml(c, parser, null,
				Xml.asAttributeSet(parser));
	}

	private static Animation createAnimationFromXml(Context c,
			XmlPullParser parser, AnimationSet parent, AttributeSet attrs)
			throws XmlPullParserException, IOException {

		Animation anim = null;

		// Make sure we are on a start tag.
		int type;
		int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("set")) {
				// 集合
				anim = new AnimationSet(c, attrs);
				createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
			} else if (name.equals("alpha")) {
				// 仅仅修改alpha
				anim = new AlphaAnimation(c, attrs);
			} else if (name.equals("scale")) {
				// 缩放
				anim = new ScaleAnimation(c, attrs);
			} else if (name.equals("rotate")) {
				// 旋转
				anim = new RotateAnimation(c, attrs);
			} else if (name.equals("translate")) {
				// 变化位置
				anim = new TranslateAnimation(c, attrs);
			} else {
				throw new RuntimeException("Unknown animation name: "
						+ parser.getName());
			}

			if (parent != null) {
				// 将animation添加到set中
				parent.addAnimation(anim);
			}
		}

		return anim;

	}

	public static LayoutAnimationController loadLayoutAnimation(
			Context context, int id) throws NotFoundException {

		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createLayoutAnimationFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private static LayoutAnimationController createLayoutAnimationFromXml(
			Context c, XmlPullParser parser) throws XmlPullParserException,
			IOException {

		return createLayoutAnimationFromXml(c, parser,
				Xml.asAttributeSet(parser));
	}

	private static LayoutAnimationController createLayoutAnimationFromXml(
			Context c, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {

		LayoutAnimationController controller = null;

		int type;
		int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

			if (type != XmlPullParser.START_TAG) {
				// 有可能为注释
				continue;
			}

			String name = parser.getName();

			if ("layoutAnimation".equals(name)) {
				controller = new LayoutAnimationController(c, attrs);
			} else if ("gridLayoutAnimation".equals(name)) {
				controller = new GridLayoutAnimationController(c, attrs);
			} else {
				throw new RuntimeException("Unknown layout animation name: "
						+ name);
			}
		}

		return controller;
	}

	/**
	 * 使得动画对象显示，使用slide和fade效果 <br>
	 * Make an animation for objects becoming visible. Uses a slide and fade
	 * effect.
	 * 
	 * @param c
	 *            Context for loading resources
	 * @param fromLeft
	 *            is the object to be animated coming from the left
	 * @return The new animation
	 */
	public static Animation makeInAnimation(Context c, boolean fromLeft) {
		Animation a;
		if (fromLeft) {
			a = AnimationUtils.loadAnimation(c,
					com.android.internal.R.anim.slide_in_left);
		} else {
			a = AnimationUtils.loadAnimation(c,
					com.android.internal.R.anim.slide_in_right);
		}

		a.setInterpolator(new DecelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	/**
	 * 使得动画对象消失，使用slide和fade效果 <br>
	 * Make an animation for objects becoming invisible. Uses a slide and fade
	 * effect.
	 * 
	 * @param c
	 *            Context for loading resources
	 * @param toRight
	 *            is the object to be animated exiting to the right
	 * @return The new animation
	 */
	public static Animation makeOutAnimation(Context c, boolean toRight) {
		Animation a;
		if (toRight) {
			a = AnimationUtils.loadAnimation(c,
					com.android.internal.R.anim.slide_out_right);
		} else {
			a = AnimationUtils.loadAnimation(c,
					com.android.internal.R.anim.slide_out_left);
		}

		a.setInterpolator(new AccelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	/**
	 * Make an animation for objects becoming visible. Uses a slide up and fade
	 * effect.
	 * 
	 * @param c
	 *            Context for loading resources
	 * @return The new animation
	 */
	public static Animation makeInChildBottomAnimation(Context c) {
		Animation a;
		a = AnimationUtils.loadAnimation(c,
				com.android.internal.R.anim.slide_in_child_bottom);
		a.setInterpolator(new AccelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	/**
	 * Loads an {@link Interpolator} object from a resource
	 * 
	 * @param context
	 *            Application context used to access resources
	 * @param id
	 *            The resource id of the animation to load
	 * @return The animation object reference by the specified id
	 * @throws NotFoundException
	 */
	public static Interpolator loadInterpolator(Context context, int id)
			throws NotFoundException {
		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createInterpolatorFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			NotFoundException rnf = new NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}

	}

	private static Interpolator createInterpolatorFromXml(Context c,
			XmlPullParser parser) throws XmlPullParserException, IOException {

		Interpolator interpolator = null;

		// Make sure we are on a start tag.
		int type;
		int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			AttributeSet attrs = Xml.asAttributeSet(parser);

			String name = parser.getName();

			if (name.equals("linearInterpolator")) {
				interpolator = new LinearInterpolator(c, attrs);
			} else if (name.equals("accelerateInterpolator")) {
				interpolator = new AccelerateInterpolator(c, attrs);
			} else if (name.equals("decelerateInterpolator")) {
				interpolator = new DecelerateInterpolator(c, attrs);
			} else if (name.equals("accelerateDecelerateInterpolator")) {
				interpolator = new AccelerateDecelerateInterpolator(c, attrs);
			} else if (name.equals("cycleInterpolator")) {
				interpolator = new CycleInterpolator(c, attrs);
			} else if (name.equals("anticipateInterpolator")) {
				interpolator = new AnticipateInterpolator(c, attrs);
			} else if (name.equals("overshootInterpolator")) {
				interpolator = new OvershootInterpolator(c, attrs);
			} else if (name.equals("anticipateOvershootInterpolator")) {
				interpolator = new AnticipateOvershootInterpolator(c, attrs);
			} else if (name.equals("bounceInterpolator")) {
				interpolator = new BounceInterpolator(c, attrs);
			} else {
				throw new RuntimeException("Unknown interpolator name: "
						+ parser.getName());
			}

		}

		return interpolator;

	}
}
