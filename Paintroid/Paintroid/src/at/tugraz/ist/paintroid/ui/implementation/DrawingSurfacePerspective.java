/*
 *   This file is part of Paintroid, a software part of the Catroid project.
 *   Copyright (C) 2010  Catroid development team
 *   <http://code.google.com/p/catroid/wiki/Credits>
 *
 *   Paintroid is free software: you can redistribute it and/or modify it
 *   under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.tugraz.ist.paintroid.ui.implementation;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import at.tugraz.ist.paintroid.ui.Perspective;

/**
 * The purpose of this class is to provide an independent interface to manipulate the scale and
 * translation of the DrawingSurface. The direct manipulation of the Canvas is synchronized on the
 * SurfaceHolder on which the DrawingSurface must also synchronize its own drawing.
 */
public class DrawingSurfacePerspective implements Perspective {
	public static final float MIN_SCALE = 0.5f;

	private final SurfaceHolder surfaceHolder;
	private final PointF surfaceCenter;
	private final PointF surfaceTranslation;
	private final Rect surfaceFrame;
	private float surfaceScale;

	/**
	 * Initialize a Perspective object with the SurfaceHolder of the DrawingSurface.
	 * 
	 * @param surfaceHolder SurfaceHolder holding the DrawingSurface's Canvas.
	 */
	public DrawingSurfacePerspective(SurfaceHolder holder) {
		surfaceHolder = holder;
		surfaceFrame = holder.getSurfaceFrame();
		surfaceCenter = new PointF(surfaceFrame.exactCenterX(), surfaceFrame.exactCenterY());
		surfaceTranslation = new PointF(0, 0);
	}

	@Override
	public void scale(float scale) {
		if (scale >= MIN_SCALE) {
			surfaceScale = scale;
		} else {
			surfaceScale = MIN_SCALE;
		}
		applyToCanvas();
	}

	@Override
	public void translate(float dx, float dy) {
		surfaceTranslation.offset(Math.round(dx / surfaceScale), Math.round(dy / surfaceScale));
		applyToCanvas();
	}

	private void applyToCanvas() {
		synchronized (surfaceHolder) {
			Canvas canvas = null;
			try {
				canvas = surfaceHolder.lockCanvas();
				canvas.scale(surfaceScale, surfaceScale, surfaceCenter.x, surfaceCenter.y);
				canvas.translate(surfaceTranslation.x, surfaceTranslation.y);
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	@Override
	public void translateScreenToCanvas(Point coords) {
		coords.x = (int) ((coords.x - surfaceCenter.x) / surfaceScale + surfaceCenter.x - surfaceTranslation.x);
		coords.y = (int) ((coords.y - surfaceCenter.y) / surfaceScale + surfaceCenter.y - surfaceTranslation.y);
	}
}