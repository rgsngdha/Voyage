package com.manchuan.tools.utils

import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

object PathParser {
    private val TAG = PathParser::class.java.simpleName

    /*
     * This is where the hard-to-parse paths are handled.
     * Uppercase rules are absolute positions, lowercase are relative.
     * Types of path rules:
     * <p/>
     * <ol>
     * <li>M/m - (x y)+ - Move to (without drawing)
     * <li>Z/z - (no params) - Close path (back to starting point)
     * <li>L/l - (x y)+ - Line to
     * <li>H/h - x+ - Horizontal ine to
     * <li>V/v - y+ - Vertical line to
     * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
     * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
     * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
     * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
     * </ol>
     * <p/>
     * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
     */
    @JvmStatic
    fun getPath(s: String): Path {
        val n = s.length
        val ph = ParserHelper(s)
        ph.skipWhitespace()
        val p = Path()
        var lastX = 0f
        var lastY = 0f
        var lastX1 = 0f
        var lastY1 = 0f
        var contourInitialX = 0f
        var contourInitialY = 0f
        val r = RectF()
        var prevCmd = 'm'
        var cmd = 'x'
        while (ph.pos < n) {
            val next = s[ph.pos]
            if (!Character.isDigit(next) && next != '.' && next != '-') {
                cmd = next
                ph.advance()
            } else if (cmd == 'M') { // implied command
                cmd = 'L'
            } else if (cmd == 'm') { // implied command
                cmd = 'l'
            }
            p.computeBounds(r, true)
            var wasCurve = false
            when (cmd) {
                'M', 'm' -> {
                    val x = ph.nextFloat()
                    val y = ph.nextFloat()
                    if (cmd == 'm') {
                        p.rMoveTo(x, y)
                        lastX += x
                        lastY += y
                    } else {
                        p.moveTo(x, y)
                        lastX = x
                        lastY = y
                    }
                    contourInitialX = lastX
                    contourInitialY = lastY
                }

                'Z', 'z' -> {

                    /// p.lineTo(contourInitialX, contourInitialY);
                    p.close()
                    lastX = contourInitialX
                    lastY = contourInitialY
                }

                'L', 'l' -> {
                    val x = ph.nextFloat()
                    val y = ph.nextFloat()
                    if (cmd == 'l') {
                        if ((prevCmd == 'M' || prevCmd == 'm') && x == 0f && y == 0f) {
                            p.addCircle(x, y, 1f, Path.Direction.CW)
                        } else {
                            p.rLineTo(x, y)
                            lastX += x
                            lastY += y
                        }
                    } else {
                        if ((prevCmd == 'M' || prevCmd == 'm') && x == lastX && y == lastY) {
                            p.addCircle(x, y, 1f, Path.Direction.CW)
                        } else {
                            p.lineTo(x, y)
                            lastX = x
                            lastY = y
                        }
                    }
                }

                'H', 'h' -> {
                    val x = ph.nextFloat()
                    if (cmd == 'h') {
                        p.rLineTo(x, 0f)
                        lastX += x
                    } else {
                        p.lineTo(x, lastY)
                        lastX = x
                    }
                }

                'V', 'v' -> {
                    val y = ph.nextFloat()
                    if (cmd == 'v') {
                        p.rLineTo(0f, y)
                        lastY += y
                    } else {
                        p.lineTo(lastX, y)
                        lastY = y
                    }
                }

                'C', 'c' -> {
                    wasCurve = true
                    var x1 = ph.nextFloat()
                    var y1 = ph.nextFloat()
                    var x2 = ph.nextFloat()
                    var y2 = ph.nextFloat()
                    var x = ph.nextFloat()
                    var y = ph.nextFloat()
                    if (cmd == 'c') {
                        x1 += lastX
                        x2 += lastX
                        x += lastX
                        y1 += lastY
                        y2 += lastY
                        y += lastY
                    }
                    p.cubicTo(x1, y1, x2, y2, x, y)
                    lastX1 = x2
                    lastY1 = y2
                    lastX = x
                    lastY = y
                }

                'S', 's' -> {
                    wasCurve = true
                    var x2 = ph.nextFloat()
                    var y2 = ph.nextFloat()
                    var x = ph.nextFloat()
                    var y = ph.nextFloat()
                    if (cmd == 's') {
                        x2 += lastX
                        x += lastX
                        y2 += lastY
                        y += lastY
                    }
                    val x1 = 2 * lastX - lastX1
                    val y1 = 2 * lastY - lastY1
                    p.cubicTo(x1, y1, x2, y2, x, y)
                    lastX1 = x2
                    lastY1 = y2
                    lastX = x
                    lastY = y
                }

                'A', 'a' -> {
                    val rx = ph.nextFloat()
                    val ry = ph.nextFloat()
                    val theta = ph.nextFloat()
                    val largeArc = ph.nextFloat().toInt()
                    val sweepArc = ph.nextFloat().toInt()
                    var x = ph.nextFloat()
                    var y = ph.nextFloat()
                    if (cmd == 'a') {
                        x += lastX
                        y += lastY
                    }
                    drawArc(
                        p,
                        lastX.toDouble(),
                        lastY.toDouble(),
                        x.toDouble(),
                        y.toDouble(),
                        rx.toDouble(),
                        ry.toDouble(),
                        theta.toDouble(),
                        largeArc == 1,
                        sweepArc == 1
                    )
                    lastX = x
                    lastY = y
                }

                'T', 't' -> {
                    wasCurve = true
                    var x = ph.nextFloat()
                    var y = ph.nextFloat()
                    if (cmd == 't') {
                        x += lastX
                        y += lastY
                    }
                    val x1 = 2 * lastX - lastX1
                    val y1 = 2 * lastY - lastY1
                    p.cubicTo(lastX, lastY, x1, y1, x, y)
                    lastX = x
                    lastY = y
                    lastX1 = x1
                    lastY1 = y1
                }

                'Q', 'q' -> {
                    wasCurve = true
                    var x1 = ph.nextFloat()
                    var y1 = ph.nextFloat()
                    var x = ph.nextFloat()
                    var y = ph.nextFloat()
                    if (cmd == 'q') {
                        x += lastX
                        y += lastY
                        x1 += lastX
                        y1 += lastY
                    }
                    p.cubicTo(lastX, lastY, x1, y1, x, y)
                    lastX1 = x1
                    lastY1 = y1
                    lastX = x
                    lastY = y
                }

                else -> {
                    Log.w(TAG, "Invalid path command: $cmd")
                    ph.advance()
                }
            }
            prevCmd = cmd
            if (!wasCurve) {
                lastX1 = lastX
                lastY1 = lastY
            }
            ph.skipWhitespace()
        }
        return p
    }

    /*
     * Elliptical arc implementation based on the SVG specification notes
     * Adapted from the Batik library (Apache-2 license) by SAU
     */
    private fun drawArc(
        path: Path, x0: Double, y0: Double, x: Double, y: Double, rx: Double,
        ry: Double, angle: Double, largeArcFlag: Boolean, sweepFlag: Boolean,
    ) {
        var rx = rx
        var ry = ry
        var angle = angle
        val dx2 = (x0 - x) / 2.0
        val dy2 = (y0 - y) / 2.0
        angle = Math.toRadians(angle % 360.0)
        val cosAngle = Math.cos(angle)
        val sinAngle = Math.sin(angle)
        val x1 = cosAngle * dx2 + sinAngle * dy2
        val y1 = -sinAngle * dx2 + cosAngle * dy2
        rx = abs(rx)
        ry = abs(ry)
        var Prx = rx * rx
        var Pry = ry * ry
        val Px1 = x1 * x1
        val Py1 = y1 * y1

        // check that radii are large enough
        val radiiCheck = Px1 / Prx + Py1 / Pry
        if (radiiCheck > 1) {
            rx *= sqrt(radiiCheck)
            ry *= sqrt(radiiCheck)
            Prx = rx * rx
            Pry = ry * ry
        }

        // Step 2 : Compute (cx1, cy1)
        var sign = (if (largeArcFlag == sweepFlag) -1 else 1).toDouble()
        var sq = ((Prx * Pry - Prx * Py1 - Pry * Px1) / (Prx * Py1 + Pry * Px1))
        sq = if (sq < 0) 0.0 else sq
        val coef = sign * sqrt(sq)
        val cx1 = coef * (rx * y1 / ry)
        val cy1 = coef * -(ry * x1 / rx)
        val sx2 = (x0 + x) / 2.0
        val sy2 = (y0 + y) / 2.0
        val cx = sx2 + (cosAngle * cx1 - sinAngle * cy1)
        val cy = sy2 + (sinAngle * cx1 + cosAngle * cy1)

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        val ux = (x1 - cx1) / rx
        val uy = (y1 - cy1) / ry
        val vx = (-x1 - cx1) / rx
        val vy = (-y1 - cy1) / ry
        var p: Double
        var n: Double

        // Compute the angle start
        n = Math.sqrt(ux * ux + uy * uy)
        p = ux // (1 * ux) + (0 * uy)
        sign = if (uy < 0) -1.0 else 1.0
        var angleStart = Math.toDegrees(sign * Math.acos(p / n))

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
        p = ux * vx + uy * vy
        sign = if (ux * vy - uy * vx < 0) -1.0 else 1.0
        var angleExtent = Math.toDegrees(sign * Math.acos(p / n))
        if (!sweepFlag && angleExtent > 0) {
            angleExtent -= 360.0
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360.0
        }
        angleExtent %= 360.0
        angleStart %= 360.0
        val oval = RectF(
            (cx - rx).toFloat(), (cy - ry).toFloat(), (cx + rx).toFloat(), (cy + ry).toFloat()
        )
        path.addArc(oval, angleStart.toFloat(), angleExtent.toFloat())
    }

    /**
     * Parses numbers from SVG text. Based on the Batik Number Parser (Apache 2 License).
     *
     * @author Apache Software Foundation, Larva Labs LLC
     */
    internal class ParserHelper(private val s: CharSequence) {
        private var current: Char
        var pos = 0
        private val n: Int
        private fun read(): Char {
            if (pos < n) {
                pos++
            }
            return if (pos == n) {
                '\u0000'
            } else {
                s[pos]
            }
        }

        fun skipWhitespace() {
            while (pos < n) {
                if (Character.isWhitespace(s[pos])) {
                    advance()
                } else {
                    break
                }
            }
        }

        fun skipNumberSeparator() {
            while (pos < n) {
                val c = s[pos]
                when (c) {
                    ' ', ',', '\n', '\t' -> advance()
                    else -> return
                }
            }
        }

        fun advance() {
            current = read()
        }

        //Parses the content of the buffer and converts it to a float.
        fun parseFloat(): Float {
            var mant = 0
            var mantDig = 0
            var mantPos = true
            var mantRead = false
            var exp = 0
            var expDig = 0
            var expAdj = 0
            var expPos = true
            when (current) {
                '-' -> {
                    mantPos = false
                    current = read()
                }

                '+' -> current = read()
            }
            when (current) {
                '.' -> {}
                '0' -> {
                    l@ while (true) {
                        current = read()
                        when (current) {
                            '1', '2', '3', '4', '5', '6', '7', '8', '9' -> break@l
                            '.', 'e', 'E' -> break
                            '0' -> {}
                            else -> return 0.0f
                        }
                    }
                    mantRead = true
                    l@ while (true) {
                        if (mantDig < 9) {
                            mantDig++
                            mant = mant * 10 + (current.code - '0'.code)
                        } else {
                            expAdj++
                        }
                        current = read()
                        when (current) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                            else -> break@l
                        }
                    }
                }

                '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    mantRead = true
                    l@ while (true) {
                        if (mantDig < 9) {
                            mantDig++
                            mant = mant * 10 + (current.code - '0'.code)
                        } else {
                            expAdj++
                        }
                        current = read()
                        when (current) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                            else -> break@l
                        }
                    }
                }

                else -> return Float.NaN
            }
            if (current == '.') {
                current = read()
                when (current) {
                    'e', 'E' -> if (!mantRead) {
                        reportUnexpectedCharacterError(current)
                        return 0.0f
                    }

                    '0' -> {
                        if (mantDig == 0) {
                            l@ while (true) {
                                current = read()
                                expAdj--
                                when (current) {
                                    '1', '2', '3', '4', '5', '6', '7', '8', '9' -> break@l
                                    '0' -> {}
                                    else -> {
                                        if (!mantRead) {
                                            return 0.0f
                                        }
                                        break
                                    }
                                }
                            }
                        }
                        l@ while (true) {
                            if (mantDig < 9) {
                                mantDig++
                                mant = mant * 10 + (current.code - '0'.code)
                                expAdj--
                            }
                            current = read()
                            when (current) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                                else -> break@l
                            }
                        }
                    }

                    '1', '2', '3', '4', '5', '6', '7', '8', '9' -> l@ while (true) {
                        if (mantDig < 9) {
                            mantDig++
                            mant = mant * 10 + (current.code - '0'.code)
                            expAdj--
                        }
                        current = read()
                        when (current) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                            else -> break@l
                        }
                    }

                    else -> if (!mantRead) {
                        reportUnexpectedCharacterError(current)
                        return 0.0f
                    }
                }
            }
            when (current) {
                'e', 'E' -> {
                    current = read()
                    when (current) {
                        '-' -> {
                            expPos = false
                            current = read()
                            when (current) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                                else -> {
                                    reportUnexpectedCharacterError(current)
                                    return 0f
                                }
                            }
                        }

                        '+' -> {
                            current = read()
                            when (current) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                                else -> {
                                    reportUnexpectedCharacterError(current)
                                    return 0f
                                }
                            }
                        }

                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                        else -> {
                            reportUnexpectedCharacterError(current)
                            return 0f
                        }
                    }
                    when (current) {
                        '0' -> {
                            l@ while (true) {
                                current = read()
                                when (current) {
                                    '1', '2', '3', '4', '5', '6', '7', '8', '9' -> break@l
                                    '0' -> {}
                                    else -> break
                                }
                            }
                            l@ while (true) {
                                if (expDig < 3) {
                                    expDig++
                                    exp = exp * 10 + (current.code - '0'.code)
                                }
                                current = read()
                                when (current) {
                                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                                    else -> break@l
                                }
                            }
                        }

                        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> l@ while (true) {
                            if (expDig < 3) {
                                expDig++
                                exp = exp * 10 + (current.code - '0'.code)
                            }
                            current = read()
                            when (current) {
                                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {}
                                else -> break@l
                            }
                        }
                    }
                }

                else -> {}
            }
            if (!expPos) {
                exp = -exp
            }
            exp += expAdj
            if (!mantPos) {
                mant = -mant
            }
            return buildFloat(mant, exp)
        }

        private fun reportUnexpectedCharacterError(c: Char) {
            throw RuntimeException("Unexpected char '$c'.")
        }

        init {
            n = s.length
            current = s[pos]
        }

        fun nextFloat(): Float {
            skipWhitespace()
            val f = parseFloat()
            skipNumberSeparator()
            return f
        }

        companion object {
            //Computes a float from mantissa and exponent.
            private fun buildFloat(mant: Int, exp: Int): Float {
                var mant = mant
                if (exp < -125 || mant == 0) {
                    return 0.0f
                }
                if (exp >= 128) {
                    return if (mant > 0) Float.POSITIVE_INFINITY else Float.NEGATIVE_INFINITY
                }
                if (exp == 0) {
                    return mant.toFloat()
                }
                if (mant >= 1 shl 26) {
                    mant++ // round up trailing bits if they will be dropped.
                }
                return (if (exp > 0) mant * pow10[exp] else mant / pow10[-exp]).toFloat()
            }

            /**
             * Array of powers of ten. Using double instead of float gives a tiny bit more precision.
             */
            private val pow10 = DoubleArray(128)

            init {
                for (i in pow10.indices) {
                    pow10[i] = Math.pow(10.0, i.toDouble())
                }
            }
        }
    }
}