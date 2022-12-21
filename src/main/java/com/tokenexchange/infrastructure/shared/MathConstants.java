package com.tokenexchange.infrastructure.shared;

import java.math.MathContext;
import java.math.RoundingMode;

public class MathConstants {
    public static final Integer SCALE = 7;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.UP;

    public static final MathContext MATH_CONTEXT = new MathContext(SCALE, ROUNDING_MODE);
}
