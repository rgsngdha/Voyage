package com.manchuan.tools.widget.colorpicker.builder;

import com.manchuan.tools.widget.colorpicker.ColorPickerView;
import com.manchuan.tools.widget.colorpicker.renderer.ColorWheelRenderer;
import com.manchuan.tools.widget.colorpicker.renderer.FlowerColorWheelRenderer;
import com.manchuan.tools.widget.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
	public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
		switch (wheelType) {
			case CIRCLE:
				return new SimpleColorWheelRenderer();
			case FLOWER:
				return new FlowerColorWheelRenderer();
		}
		throw new IllegalArgumentException("wrong WHEEL_TYPE");
	}
}