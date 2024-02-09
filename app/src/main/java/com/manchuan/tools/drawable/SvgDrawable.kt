package com.manchuan.tools.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.util.Base64
import android.util.Log
import android.util.Xml
import androidx.annotation.ColorInt
import androidx.annotation.RawRes
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.PathParser
import com.manchuan.tools.utils.PathParser.getPath
import com.manchuan.tools.utils.SystemUiUtil.dpToPx
import com.manchuan.tools.utils.SystemUiUtil.getDisplayHeight
import com.manchuan.tools.utils.SystemUiUtil.getDisplayWidth
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Objects
import java.util.Random

class SvgDrawable(context: Context, @RawRes resId: Int) {
    private val objects: MutableList<SvgObject>
    private val ids: MutableList<String?>
    private var offsetX = 0f
    private var offsetY = 0f
    private var scale: Float
    private var zoom = 0f
    private val pixelUnit: Float
    private var svgWidth = 0f
    private var svgHeight = 0f
    private val paint: Paint
    private val paintIntersection: Paint
    private val paintDebug: Paint
    private var backgroundColor = 0
    private val rectF: RectF
    private var pointF: PointF? = null
    private val random: Random
    private val pathTransformed: Path
    private val pathIntersected: Path
    private val matrixCanvas: Matrix
    private val matrixPathTransformation: Matrix

    init {
        pixelUnit = getPixelUnit(context)
        objects = ArrayList()
        ids = ArrayList()
        try {
            parse(context.resources.openRawResource(resId))
        } catch (e: IOException) {
            Log.e(TAG, "Could not open SVG resource", e)
        }
        scale = 1f
        paint = Paint()
        paintIntersection = Paint(Paint.ANTI_ALIAS_FLAG)
        rectF = RectF()
        random = Random()
        pathTransformed = Path()
        pathIntersected = Path()
        matrixCanvas = Matrix()
        matrixPathTransformation = Matrix()
        paintDebug = Paint(Paint.ANTI_ALIAS_FLAG)
        paintDebug.strokeWidth = dpToPx(context, 4f).toFloat()
        paintDebug.style = Paint.Style.STROKE
        paintDebug.strokeCap = Cap.ROUND
        paintDebug.color = Color.CYAN
    }

    fun findObjectById(id: String?): SvgObject? {
        return if (ids.contains(id)) {
            objects[ids.indexOf(id)]
        } else {
            null
        }
    }

    fun requireObjectById(id: String?): SvgObject {
        return if (ids.contains(id)) {
            objects[ids.indexOf(id)]
        } else {
            SvgObject(SvgObject.TYPE_NONE)
        }
    }

    /**
     * The final offset is calculated with the elevation
     */
    fun setOffset(offsetX: Float, offsetY: Float) {
        this.offsetX = offsetX
        this.offsetY = offsetY
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    /**
     * Set how much should be zoomed out. The final value is calculated with the elevation of each
     * object. An object with elevation of 1 (nearest) is zoomed out much more than an object with the
     * elevation 0.1 (almost no parallax/zoom effect).
     *
     * @param zoom value from 0-1: 0 = original size; 1 = max zoomed out (depending on the elevation)
     */
    fun setZoom(zoom: Float) {
        this.zoom = zoom
    }

    /**
     * Apply random elevation between 0 (no parallax/zoom) to 1 (maximal effects) to all objects
     *
     * @param min Set the minimal parallax/zoom intensity (good if nothing should be static)
     */
    fun applyRandomElevationToAll(min: Float) {
        for (`object` in objects) {
            `object`.elevation = min + random.nextFloat() * (1 - min)
        }
    }

    /**
     * Apply elevation between 0 (no parallax/zoom) to 1 (maximal effects) to all objects in the
     * original order
     *
     * @param min Set the minimal parallax/zoom intensity (good if nothing should be static)
     */
    fun applyRelativeElevationToAll(min: Float) {
        val step = (1 - min) / objects.size
        for (i in objects.indices) {
            objects[i].elevation = Math.min(min + step * i, 1f)
        }
    }

    /**
     * Apply random rotation to all objects, which is applied with the current zoom intensity
     *
     * @param min Set the minimal rotation in degrees (can be negative)
     * @param max Set the maximal rotation in degrees
     */
    fun applyRandomZoomRotationToAll(min: Int, max: Int) {
        for (`object` in objects) {
            if (`object`.isRotatable) {
                if (DEBUG) {
                    `object`.zoomRotation = 260
                } else {
                    `object`.zoomRotation =
                        if (min == 0 && max == 0) 0 else random.nextInt(max - min + 1) + min
                }
            } else {
                `object`.zoomRotation = 0
            }
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        for (`object` in objects) {
            drawObject(canvas, `object`, null)
        }
    }

    @Throws(IOException::class)
    private fun parse(inputStream: InputStream) {
        try {
            inputStream.use {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, ENABLE_IMAGES)
                parser.setInput(inputStream, null)
                parser.next()
                readSvg(parser)
            }
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "parse", e)
        } catch (e: IOException) {
            Log.e(TAG, "parse", e)
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readSvg(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, null, "svg")
        val viewBox = parser.getAttributeValue(null, "viewBox")
        if (viewBox != null) {
            val metrics = viewBox.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            svgWidth = metrics[2].toFloat() - metrics[0].toFloat()
            svgHeight = metrics[3].toFloat() - metrics[1].toFloat()
        } else {
            Log.e(TAG, "readSvg: required viewBox attribute is missing")
            return
        }
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            readObject(parser, null)
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readObject(parser: XmlPullParser, parentGroup: SvgObject?) {
        when (parser.name) {
            SvgObject.TYPE_GROUP -> if (parentGroup == null) {
                readGroup(parser)
            } else {
                Timber.tag(TAG).w("readSvg: child groups in groups are not supported, skipping...")
                skip(parser)
            }

            SvgObject.TYPE_PATH -> readPath(parser, parentGroup)
            SvgObject.TYPE_RECT -> readRect(parser, parentGroup)
            SvgObject.TYPE_CIRCLE -> readCircle(parser, parentGroup)
            SvgObject.TYPE_ELLIPSE -> readEllipse(parser, parentGroup)
            SvgObject.TYPE_IMAGE -> if (ENABLE_IMAGES) {
                readImage(parser, parentGroup)
            }

            else -> skip(parser)
        }
    }

    private fun drawObject(canvas: Canvas, `object`: SvgObject, parentGroup: SvgObject?) {
        val zoomRotation: Float = if (`object`.isRotatable) `object`.zoomRotation * zoom else 0F
        val hasPivotOffset = `object`.pivotOffsetX != 0f || `object`.pivotOffsetY != 0f
        val canvasTransformed =
            !`object`.isInGroup && (`object`.rotation != 0f || zoomRotation != 0f)
        if (canvasTransformed) {
            canvas.save()
        }
        if (!`object`.isInGroup && (`object`.rotation != 0f || zoomRotation != 0f)) {
            if (!hasPivotOffset) {
                // Even for groups this rotation is required
                canvas.rotate(
                    `object`.rotation + zoomRotation,
                    `object`.cx * canvas.width,
                    `object`.cy * canvas.height
                )
            } else {
                if (`object`.rotation != 0f) {
                    canvas.rotate(
                        `object`.rotation, `object`.cx * canvas.width, `object`.cy * canvas.height
                    )
                }
                if (zoomRotation != 0f) {
                    if (DEBUG) { // draw pivot offset
                        canvas.drawPoint(
                            `object`.cx * canvas.width + `object`.pivotOffsetX * pixelUnit * scale,
                            `object`.cy * canvas.height + `object`.pivotOffsetY * pixelUnit * scale,
                            getDebugPaint(Color.YELLOW)
                        )
                    }
                    canvas.rotate(
                        zoomRotation,
                        `object`.cx * canvas.width + `object`.pivotOffsetX * pixelUnit * scale,
                        `object`.cy * canvas.height + `object`.pivotOffsetY * pixelUnit * scale
                    )
                }
            }
        }
        when (`object`.type) {
            SvgObject.TYPE_GROUP -> drawGroup(canvas, `object`)
            SvgObject.TYPE_PATH -> drawPath(canvas, `object`, parentGroup)
            SvgObject.TYPE_RECT -> drawRect(canvas, `object`, parentGroup)
            SvgObject.TYPE_CIRCLE, SvgObject.TYPE_ELLIPSE -> drawCircle(
                canvas, `object`, parentGroup
            )

            SvgObject.TYPE_IMAGE -> if (ENABLE_IMAGES) {
                drawImage(canvas, `object`, parentGroup)
            }
        }
        if (canvasTransformed) {
            canvas.restore()
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readGroup(parser: XmlPullParser) {
        val `object` = SvgObject(SvgObject.TYPE_GROUP)
        `object`.children = ArrayList()
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_GROUP)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_GROUP) {
            if (`object`.id == null) {
                Log.w(TAG, "readGroup: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.w(TAG, "readGroup: id '" + `object`.id + "' already exists, skipping...")
                return
            }

            // Save transformation value now (but don't use it, center is not calculated yet)
            // When we continue parsing, the translation value would be lost
            val transformation = parser.getAttributeValue(null, "transform")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                readObject(parser, `object`)
            }
            parseTransformation(transformation, `object`)

            // Compensate rotation of the child center positions
            for (child in `object`.children as ArrayList<SvgObject>) {
                val center = getRotatedPoint(
                    child.cx, child.cy, `object`.rotationX, `object`.rotationY, `object`.rotation
                )
                child.cx = center.x
                child.cy = center.y
            }

            // Calculate group center
            val centersBounds = RectF()
            for (i in (`object`.children as ArrayList<SvgObject>).indices) {
                val child = (`object`.children as ArrayList<SvgObject>).get(i)
                if (i == 0) {
                    centersBounds.offset(child.cx, child.cy)
                } else {
                    centersBounds.union(child.cx, child.cy)
                }
            }
            `object`.cx = centersBounds.centerX()
            `object`.cy = centersBounds.centerY()

            // Pass the distance from group center to all children
            for (child in `object`.children as ArrayList<SvgObject>) {
                child.xDistGroupCenter = (child.cx - `object`.cx) * pixelUnit
                child.yDistGroupCenter = (child.cy - `object`.cy) * pixelUnit
                // Rotate the child around the group center with the negative group rotation angle
                val finalDistance = getRotatedPoint(
                    child.xDistGroupCenter, child.yDistGroupCenter, 0f, 0f, -`object`.rotation
                )
                child.xDistGroupCenter = finalDistance.x
                child.yDistGroupCenter = finalDistance.y
            }

            // Make group center relative
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        objects.add(`object`)
        ids.add(`object`.id)
    }

    private fun drawGroup(canvas: Canvas, `object`: SvgObject) {
        pointF = getFinalCenter(canvas, `object`, null)
        if (DEBUG) { // draw final group center
            val strokeWidth = paintDebug.strokeWidth
            paintDebug.strokeWidth = strokeWidth * 2
            canvas.drawPoint(pointF!!.x, pointF!!.y, getDebugPaint(Color.GREEN))
            paintDebug.strokeWidth = strokeWidth
        }
        `object`.cxFinal = pointF!!.x
        `object`.cyFinal = pointF!!.y
        `object`.childScale = getFinalScale(`object`, null)
        for (child in `object`.children!!) {
            drawObject(canvas, child, `object`)
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPath(parser: XmlPullParser, parentGroup: SvgObject?) {
        val `object` = SvgObject(SvgObject.TYPE_PATH)
        `object`.isInGroup = parentGroup != null
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_PATH)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_PATH) {
            if (`object`.id == null) {
                Log.w(TAG, "readPath: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.e(TAG, "readPath: id '" + `object`.id + "' already exists, skipping...")
                return
            }
            val d = parser.getAttributeValue(null, "d")
            if (d != null && !d.isEmpty()) {
                try {
                    `object`.path = PathParser.createPathFromPathData(d)
                    if (`object`.path == null) {
                        return
                    }
                } catch (e: RuntimeException) {
                    Timber.tag(TAG)
                        .w("readPath: error with legacy parser, trying with alternative...")
                    `object`.path = getPath(d)
                }
            } else {
                return
            }
            val bounds = RectF()
            `object`.path!!.computeBounds(bounds, true)
            `object`.width = bounds.width()
            `object`.height = bounds.height()
            `object`.cx = bounds.centerX()
            `object`.cy = bounds.centerY()
            readStyle(parser, `object`)
            parseTransformation(parser.getAttributeValue(null, "transform"), `object`)
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_PATH)

        // apply display metrics
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(pixelUnit, pixelUnit, `object`.cx, `object`.cy)
        `object`.path!!.transform(scaleMatrix)
        if (!`object`.isInGroup) { // else keep absolute values for later calculation
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        if (parentGroup == null) {
            objects.add(`object`)
            ids.add(`object`.id)
        } else {
            parentGroup.children!!.add(`object`)
        }
    }

    private fun drawPath(canvas: Canvas, `object`: SvgObject, parentGroup: SvgObject?) {
        canvas.save()
        matrixCanvas.reset()

        // TODO: find fix for different behavior of matrix transformation compared to canvas operations
        val isPathIntersected = `object`.willBeIntersected || !`object`.intersections.isEmpty()
        val scale = getFinalScale(`object`, parentGroup)
        pointF = getFinalCenter(canvas, `object`, parentGroup)
        if (DEBUG) { // draw final object center
            canvas.drawPoint(pointF!!.x, pointF!!.y, getDebugPaint(Color.RED))
        }
        val dx: Float = pointF!!.x - `object`.cx * if (`object`.isInGroup) 1F else svgWidth
        val dy: Float = pointF!!.y - `object`.cy * if (`object`.isInGroup) 1F else svgHeight
        val px = if (`object`.isInGroup) parentGroup!!.cxFinal - dx else pointF!!.x - dx
        val py = if (`object`.isInGroup) parentGroup!!.cyFinal - dy else pointF!!.y - dy
        if (!`object`.isInGroup && `object`.willBeIntersected) {
            // store further canvas transformations in pathTransformed for later intersection
            matrixPathTransformation.reset()
            matrixPathTransformation.postTranslate(dx, dy)
            matrixPathTransformation.postScale(scale, scale, px, py)
            `object`.pathTransformed!!.reset()
            `object`.pathTransformed.addPath(`object`.path!!, matrixPathTransformation)
        }
        if (`object`.isInGroup) {
            val elevation = parentGroup!!.elevation
            val xCompensate = (px + dx - pointF!!.x) * (this.scale - 1) * (1 - zoom * elevation)
            val yCompensate = (py + dy - pointF!!.y) * (this.scale - 1) * (1 - zoom * elevation)
            if (isPathIntersected) {
                matrixCanvas.postTranslate(dx + xCompensate, dy + yCompensate)
            } else {
                canvas.translate(dx + xCompensate, dy + yCompensate)
            }
        } else {
            if (isPathIntersected) {
                matrixCanvas.postTranslate(dx, dy)
            } else {
                canvas.translate(dx, dy)
            }
        }
        if (DEBUG) { // draw scaling pivot point
            canvas.drawPoint(px, py, getDebugPaint(Color.BLUE))
        }
        if (isPathIntersected) {
            matrixCanvas.postScale(scale, scale, px, py)
        } else {
            canvas.scale(scale, scale, px, py)
        }
        if (`object`.isInGroup) {
            // fixes child path offset when zoomed out
            // TODO: for scale 1.4-1.7 tiny offset still occurs, find a better fix
            val elevation = parentGroup!!.elevation
            val xCompensate = (px + dx - pointF!!.x) * (1 - (this.scale - 1)) * (zoom * elevation)
            val yCompensate = (py + dy - pointF!!.y) * (1 - (this.scale - 1)) * (zoom * elevation)
            if (isPathIntersected) {
                matrixCanvas.postTranslate(-xCompensate, -yCompensate)
            } else {
                canvas.translate(-xCompensate, -yCompensate)
            }
        }
        if (isPathIntersected) {
            canvas.concat(matrixCanvas)
        }

        // start with fill and repeat with stroke if both are set
        // don't apply scale to stroke width, stroke is already scaled with canvas transformation
        val runs = if (applyPaintStyle(`object`, 1f, false)) 2 else 1
        for (i in 0 until runs) {
            if (i == 1) {
                applyPaintStyle(`object`, 1f, true)
            }
            canvas.drawPath(`object`.path!!, paint)
        }
        canvas.restore()
        if (!`object`.isInGroup && !`object`.intersections.isEmpty()) {
            for (intersection in `object`.intersections) {
                val other = findObjectById(intersection.id)
                if (other != null && other.willBeIntersected && other.pathTransformed != null) {
                    // transform object.path with recent canvas transformations
                    pathTransformed.reset()
                    pathTransformed.addPath(`object`.path!!, matrixCanvas)
                    // intersect transformed object.path with other.pathTransformed
                    pathIntersected.op(pathTransformed, other.pathTransformed, Path.Op.INTERSECT)
                    paintIntersection.color = intersection.color
                    canvas.drawPath(pathIntersected, paintIntersection)
                    if (DEBUG) {
                        canvas.drawPath(other.pathTransformed, getDebugPaint(Color.BLUE))
                        canvas.drawPath(pathTransformed, getDebugPaint(Color.RED))
                    }
                }
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readRect(parser: XmlPullParser, parentGroup: SvgObject?) {
        val `object` = SvgObject(SvgObject.TYPE_RECT)
        `object`.isInGroup = parentGroup != null
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_RECT)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_RECT) {
            if (`object`.id == null) {
                Log.w(TAG, "readRect: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.w(TAG, "readRect: id '" + `object`.id + "' already exists, skipping...")
                return
            }
            `object`.width = parseFloat(parser.getAttributeValue(null, "width"))
            `object`.height = parseFloat(parser.getAttributeValue(null, "height"))
            val x = parseFloat(parser.getAttributeValue(null, "x"))
            val y = parseFloat(parser.getAttributeValue(null, "y"))
            `object`.cx = x + `object`.width / 2
            `object`.cy = y + `object`.height / 2
            `object`.rx = parseFloat(parser.getAttributeValue(null, "rx"))
            `object`.ry = parseFloat(parser.getAttributeValue(null, "ry"))
            readStyle(parser, `object`)
            parseTransformation(parser.getAttributeValue(null, "transform"), `object`)

            // has same size as SVG? Use it as background color and don't use it as object
            if (`object`.width == svgWidth && `object`.height == svgHeight) {
                backgroundColor = `object`.fill
                return
            }
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_RECT)

        // apply display metrics
        `object`.width *= pixelUnit
        `object`.height *= pixelUnit
        `object`.rx *= pixelUnit
        `object`.ry *= pixelUnit
        if (!`object`.isInGroup) { // else keep absolute values for later calculation
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        if (parentGroup == null) {
            objects.add(`object`)
            ids.add(`object`.id)
        } else {
            parentGroup.children!!.add(`object`)
        }
    }

    private fun drawRect(canvas: Canvas, `object`: SvgObject, parentGroup: SvgObject?) {
        val scale = getFinalScale(`object`, parentGroup)
        pointF = getFinalCenter(canvas, `object`, parentGroup)
        rectF[pointF!!.x - `object`.width * scale / 2, pointF!!.y - `object`.height * scale / 2, pointF!!.x + `object`.width * scale / 2] =
            pointF!!.y + `object`.height * scale / 2

        // start with fill and repeat with stroke if both are set
        val runs = if (applyPaintStyle(`object`, scale, false)) 2 else 1
        for (i in 0 until runs) {
            if (i == 1) {
                applyPaintStyle(`object`, scale, true)
            }
            if (`object`.rx == 0f && `object`.ry == 0f) {
                canvas.drawRect(rectF, paint)
            } else {
                val rx = if (`object`.rx != 0f) `object`.rx else `object`.ry
                val ry = if (`object`.ry != 0f) `object`.ry else `object`.rx
                canvas.drawRoundRect(rectF, rx, ry, paint)
            }
        }
        if (DEBUG) { // draw final object center
            canvas.drawPoint(pointF!!.x, pointF!!.y, getDebugPaint(Color.RED))
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCircle(parser: XmlPullParser, parentGroup: SvgObject?) {
        val `object` = SvgObject(SvgObject.TYPE_CIRCLE)
        `object`.isInGroup = parentGroup != null
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_CIRCLE)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_CIRCLE) {
            if (`object`.id == null) {
                Log.w(TAG, "readCircle: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.w(TAG, "readCircle: id '" + `object`.id + "' already exists, skipping...")
                return
            }
            `object`.cx = parseFloat(parser.getAttributeValue(null, "cx"))
            `object`.cy = parseFloat(parser.getAttributeValue(null, "cy"))
            `object`.r = parseFloat(parser.getAttributeValue(null, "r"))
            readStyle(parser, `object`)
            parseTransformation(parser.getAttributeValue(null, "transform"), `object`)
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_CIRCLE)

        // apply display metrics
        if (!`object`.isInGroup) { // else keep absolute values for later calculation
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        `object`.r *= pixelUnit
        if (parentGroup == null) {
            objects.add(`object`)
            ids.add(`object`.id)
        } else {
            parentGroup.children!!.add(`object`)
        }
    }

    private fun drawCircle(canvas: Canvas, `object`: SvgObject, parentGroup: SvgObject?) {
        pointF = getFinalCenter(canvas, `object`, parentGroup)
        val scale = getFinalScale(`object`, parentGroup)

        // start with fill and repeat with stroke if both are set
        val runs = if (applyPaintStyle(`object`, scale, false)) 2 else 1
        for (i in 0 until runs) {
            if (i == 1) {
                applyPaintStyle(`object`, scale, true)
            }
            if (`object`.type == SvgObject.TYPE_CIRCLE || `object`.rx == `object`.ry) {
                val radius = if (`object`.r > 0) `object`.r else `object`.rx
                canvas.drawCircle(pointF!!.x, pointF!!.y, radius * scale, paint)
            } else if (`object`.type == SvgObject.TYPE_ELLIPSE) {
                canvas.drawOval(
                    pointF!!.x - `object`.rx * scale,
                    pointF!!.y - `object`.ry * scale,
                    pointF!!.x + `object`.rx * scale,
                    pointF!!.y + `object`.ry * scale,
                    paint
                )
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readEllipse(parser: XmlPullParser, parentGroup: SvgObject?) {
        val `object` = SvgObject(SvgObject.TYPE_ELLIPSE)
        `object`.isInGroup = parentGroup != null
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_ELLIPSE)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_ELLIPSE) {
            if (`object`.id == null) {
                Log.w(TAG, "readEllipse: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.w(TAG, "readEllipse: id '" + `object`.id + "' already exists, skipping...")
                return
            }
            `object`.cx = parseFloat(parser.getAttributeValue(null, "cx"))
            `object`.cy = parseFloat(parser.getAttributeValue(null, "cy"))
            `object`.rx = parseFloat(parser.getAttributeValue(null, "rx"))
            `object`.ry = parseFloat(parser.getAttributeValue(null, "ry"))
            readStyle(parser, `object`)
            parseTransformation(parser.getAttributeValue(null, "transform"), `object`)
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_ELLIPSE)

        // apply display metrics
        if (!`object`.isInGroup) { // else keep absolute values for later calculation
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        `object`.rx *= pixelUnit
        `object`.ry *= pixelUnit
        if (parentGroup == null) {
            objects.add(`object`)
            ids.add(`object`.id)
        } else {
            parentGroup.children!!.add(`object`)
        }
    }

    // drawEllipse is included in drawCircle
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImage(parser: XmlPullParser, parentGroup: SvgObject?) {
        val `object` = SvgObject(SvgObject.TYPE_IMAGE)
        `object`.isInGroup = parentGroup != null
        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_IMAGE)
        val tag = parser.name
        `object`.id = parser.getAttributeValue(null, "id")
        if (tag == SvgObject.TYPE_IMAGE) {
            if (`object`.id == null) {
                Log.w(TAG, "readImage: id is missing, skipping...")
                return
            } else if (ids.contains(`object`.id)) {
                Log.w(TAG, "readImage: id '" + `object`.id + "' already exists, skipping...")
                return
            }
            `object`.width = parseFloat(parser.getAttributeValue(null, "width"))
            `object`.height = parseFloat(parser.getAttributeValue(null, "height"))
            val x = parseFloat(parser.getAttributeValue(null, "x"))
            val y = parseFloat(parser.getAttributeValue(null, "y"))
            `object`.cx = x + `object`.width / 2
            `object`.cy = y + `object`.height / 2
            readStyle(parser, `object`)
            parseTransformation(parser.getAttributeValue(null, "transform"), `object`)
            var image = parser.getAttributeValue(parser.getNamespace("xlink"), "href")
            if (image != null) {
                image = image.substring(image.indexOf(",") + 1)
                val decoded = Base64.decode(image, Base64.DEFAULT)
                `object`.bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            }
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_IMAGE)

        // apply display metrics
        `object`.width *= pixelUnit
        `object`.height *= pixelUnit
        if (!`object`.isInGroup) { // else keep absolute values for later calculation
            `object`.cx /= svgWidth
            `object`.cy /= svgHeight
        }
        if (parentGroup == null) {
            objects.add(`object`)
            ids.add(`object`.id)
        } else {
            parentGroup.children!!.add(`object`)
        }
    }

    private fun drawImage(canvas: Canvas, `object`: SvgObject, parentGroup: SvgObject?) {
        paint.reset()
        paint.isAntiAlias = true
        val scale = getFinalScale(`object`, parentGroup)
        pointF = getFinalCenter(canvas, `object`, parentGroup)
        rectF[pointF!!.x - `object`.width * scale / 2, pointF!!.y - `object`.height * scale / 2, pointF!!.x + `object`.width * scale / 2] =
            pointF!!.y + `object`.height * scale / 2
        canvas.drawBitmap(`object`.bitmap!!, null, rectF, paint)
    }

    private fun parseTransformation(transformation: String?, `object`: SvgObject) {
        if (transformation == null || transformation.isEmpty()) {
            return
        }
        val transform =
            transformation.split("[ ](?=[^)]*?(?:\\(|$))".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (action in transform) {
            val value = action.substring(action.indexOf("(") + 1, action.indexOf(")"))
            if (action.contains("rotate")) {
                val rotation = value.split("[\\n\\r\\s]+".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                `object`.rotation = rotation[0].toFloat()
                if (rotation.size == 3) {
                    `object`.rotationX = rotation[1].toFloat()
                    `object`.rotationY = rotation[2].toFloat()
                }
                if (`object`.type != SvgObject.TYPE_GROUP) {
                    pointF = getRotatedPoint(
                        `object`.cx,
                        `object`.cy,
                        `object`.rotationX,
                        `object`.rotationY,
                        `object`.rotation
                    )
                    `object`.cx = pointF!!.x
                    `object`.cy = pointF!!.y
                }
            } else if (action.contains("translate")) {
                val translation =
                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (translation.size != 2) {
                    Log.e(TAG, "parseTransformation: translation: value not supported")
                    return
                }
                `object`.translationX = translation[0].toFloat() / svgWidth
                `object`.translationY = translation[1].toFloat() / svgHeight
            } else if (action.contains("scale")) {
                val scale = value.split("[\\n\\r\\s]+".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (scale.size > 1) {
                    Log.e(TAG, "parseTransformation: scale: multiple values are not supported")
                    return
                }
                `object`.scale = scale[0].toFloat()/*if (object.scale != 1) { TODO: at that time the cx and cy values in the original svg size
          object.cx *= object.scale;
          object.cy *= object.scale;
        }*/
            }
        }
    }

    private fun readStyle(parser: XmlPullParser, `object`: SvgObject) {
        `object`.fill = parseColor(parser.getAttributeValue(null, "fill"))
        `object`.stroke = parseColor(parser.getAttributeValue(null, "stroke"))
        `object`.fillOpacity = parseOpacity(
            parser.getAttributeValue(null, "fill-opacity")
        )
        `object`.strokeOpacity = parseOpacity(
            parser.getAttributeValue(null, "stroke-opacity")
        )
        `object`.strokeWidth = parseFloat(parser.getAttributeValue(null, "stroke-width"))
        `object`.strokeLineCap = parser.getAttributeValue(null, "stroke-linecap")
        `object`.strokeLineJoin = parser.getAttributeValue(null, "stroke-linejoin")
    }

    /**
     * @return true if a second draw for a separate stroke style is needed
     */
    private fun applyPaintStyle(
        `object`: SvgObject,
        scale: Float,
        applyStrokeIfBothSet: Boolean,
    ): Boolean {
        paint.reset()
        paint.isAntiAlias = true
        val hasFill = `object`.fill != 0
        val hasStroke = `object`.stroke != 0 && `object`.strokeWidth > 0
        val hasFillAndStroke = hasFill && hasStroke
        if (hasFillAndStroke && applyStrokeIfBothSet || !hasFill && hasStroke) {
            paint.style = Paint.Style.STROKE
            paint.shader = `object`.shader
            paint.setARGB(
                (`object`.strokeOpacity * 255).toInt(),
                Color.red(`object`.stroke),
                Color.green(`object`.stroke),
                Color.blue(`object`.stroke)
            )
            paint.strokeWidth = `object`.strokeWidth * pixelUnit * scale
            if (`object`.strokeLineCap != null) {
                when (`object`.strokeLineCap) {
                    SvgObject.LINE_CAP_BUTT -> paint.strokeCap = Cap.BUTT
                    SvgObject.LINE_CAP_ROUND -> paint.strokeCap = Cap.ROUND
                    SvgObject.LINE_CAP_SQUARE -> paint.strokeCap = Cap.SQUARE
                }
            }
            if (`object`.strokeLineJoin != null) {
                when (`object`.strokeLineJoin) {
                    SvgObject.LINE_JOIN_MITER -> paint.strokeJoin = Paint.Join.MITER
                    SvgObject.LINE_JOIN_ROUND -> paint.strokeJoin = Paint.Join.ROUND
                    SvgObject.LINE_JOIN_BEVEL -> paint.strokeJoin = Paint.Join.BEVEL
                }
            }
        } else if (hasFillAndStroke || hasFill) {
            paint.style = Paint.Style.FILL
            paint.shader = `object`.shader
            paint.color = ColorUtils.setAlphaComponent(
                `object`.fill, (`object`.fillOpacity * 255).toInt()
            )
        }
        if (DEBUG) { // draw semi-translucent for point/pivot debugging
            paint.alpha = 150
        }
        return hasFillAndStroke
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    class SvgObject(val type: String) {
        var id: String? = null
        var isInGroup = false
        var elevation = 0f
        var zoomRotation = 0
        var isRotatable = false
        var pivotOffsetX = 0f
        var pivotOffsetY = 0f

        // GROUP
        var children: MutableList<SvgObject>? = null
        var cxFinal = 0f
        var cyFinal = 0f
        var childScale = 0f

        // offset for each child (set on the child objects)
        var xDistGroupCenter = 0f
        var yDistGroupCenter = 0f

        // STYLE
        var fill = 0
        var stroke = 0
        var fillOpacity = 0f
        var strokeOpacity = 0f
        var strokeLineCap: String? = null
        var strokeLineJoin: String? = null
        var strokeWidth = 0f
        var shader: Shader? = null

        // TRANSFORMATION
        var rotation = 0f
        var rotationX = 0f
        var rotationY = 0f
        var translationX = 0f
        var translationY = 0f
        var scale = 0f

        // PATH
        var path: Path? = null
        var willBeIntersected = false
        val pathTransformed: Path? = Path()
        val intersections: MutableList<PathIntersection> = ArrayList()

        // RECT/IMAGE
        var width = 0f
        var height = 0f
        var rx = 0f
        var ry = 0f
        var bitmap: Bitmap? = null

        // CIRCLE
        var cx = 0f
        var cy = 0f
        var r = 0f
        fun addPathIntersection(id: String, color: String) {
            if (isInGroup || type == TYPE_GROUP) {
                Log.e(TAG, "addPathIntersection: operation does not support groups or children")
                return
            }
            val intersection = PathIntersection(id, color)
            if (!intersections.contains(intersection)) {
                intersections.add(intersection)
            }
        }

        override fun toString(): String {
            return if (type == TYPE_GROUP) {
                "SvgGroup{'" + id + "', children=" + children.toString() + '}'
            } else {
                "SvgObject('$id', '$type')"
            }
        }

        companion object {
            const val TYPE_NONE = "none"
            const val TYPE_GROUP = "g"
            const val TYPE_PATH = "path"
            const val TYPE_RECT = "rect"
            const val TYPE_CIRCLE = "circle"
            const val TYPE_ELLIPSE = "ellipse"
            const val TYPE_IMAGE = "image"

            // stroke line cap
            const val LINE_CAP_BUTT = "butt"
            const val LINE_CAP_ROUND = "round"
            const val LINE_CAP_SQUARE = "square"

            // stroke line join
            const val LINE_JOIN_ROUND = "round"
            const val LINE_JOIN_BEVEL = "bevel"
            const val LINE_JOIN_MITER = "miter"
        }
    }

    private fun parseFloat(value: String?): Float {
        return if (!value.isNullOrEmpty()) {
            try {
                value.toFloat()
            } catch (e: NumberFormatException) {
                0F
            }
        } else {
            0F
        }
    }

    private fun parseOpacity(value: String?): Float {
        return if (!value.isNullOrEmpty()) {
            try {
                value.toFloat()
            } catch (e: NumberFormatException) {
                0F
            }
        } else {
            1F
        }
    }

    private fun getRotatedPoint(x: Float, y: Float, cx: Float, cy: Float, degrees: Float): PointF {
        val radians = Math.toRadians(degrees.toDouble())
        val x1 = x - cx
        val y1 = y - cy
        val x2 = (x1 * Math.cos(radians) - y1 * Math.sin(radians)).toFloat()
        val y2 = (x1 * Math.sin(radians) + y1 * Math.cos(radians)).toFloat()
        return PointF(x2 + cx, y2 + cy)
    }

    private fun getFinalCenter(
        canvas: Canvas,
        `object`: SvgObject,
        parentGroup: SvgObject?,
    ): PointF {
        var cx: Float
        var cy: Float
        if (`object`.isInGroup) {
            cx = parentGroup!!.cxFinal + `object`.xDistGroupCenter * parentGroup.childScale
            cy = parentGroup.cyFinal + `object`.yDistGroupCenter * parentGroup.childScale
        } else {
            cx = `object`.cx * canvas.width + `object`.translationX * canvas.width
            cy = `object`.cy * canvas.height + `object`.translationY * canvas.height
        }
        val cxShifted = cx - offsetX * `object`.elevation
        val cyShifted = cy - offsetY * `object`.elevation

        // We need to compensate the object rotation, else the object would shift in that direction
        // This is caused by the canvas rotation, but that's how objects can be rotated
        val compensated = getRotatedPoint(cxShifted, cyShifted, cx, cy, -`object`.rotation)
        cx = compensated.x
        cy = compensated.y
        val centerX = canvas.width / 2f
        if (cx < centerX) {
            val dist = centerX - cx
            cx += dist * `object`.elevation * zoom
        } else {
            val dist = cx - centerX
            cx -= dist * `object`.elevation * zoom
        }
        val centerY = canvas.height / 2f
        if (cy < centerY) {
            val dist = centerY - cy
            cy += dist * `object`.elevation * zoom
        } else {
            val dist = cy - centerY
            cy -= dist * `object`.elevation * zoom
        }
        return PointF(cx, cy)
    }

    private fun getFinalScale(`object`: SvgObject, parentGroup: SvgObject?): Float {
        return if (`object`.isInGroup) parentGroup!!.childScale else scale - zoom * `object`.elevation
    }

    private fun getDebugPaint(@ColorInt color: Int): Paint {
        paintDebug.color = color
        return paintDebug
    }

    class PathIntersection(val id: String, color: String) {
        val color: Int

        init {
            this.color = parseColor(color)
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o !is PathIntersection) {
                return false
            }
            val that = o
            return color == that.color && id == that.id
        }

        override fun hashCode(): Int {
            return Objects.hash(id, color)
        }
    }

    companion object {
        private val TAG = SvgDrawable::class.java.simpleName
        private const val DEBUG = false
        private const val ENABLE_IMAGES = true
        private fun getPixelUnit(context: Context): Float {
            return dpToPx(context, 1f) * 0.33f
        }

        fun getDefaultScale(context: Context): Float {
            return try {
                val screenWidth = getDisplayWidth(context)
                val screenHeight = getDisplayHeight(context)
                val displayWidth = Math.min(screenWidth, screenHeight).toFloat()
                val circleWidth = 300 * getPixelUnit(context)
                val currentRatio = circleWidth / displayWidth
                val originalRatio = 0.2777f
                val scale = 1 - currentRatio / originalRatio + 1.2f
                BigDecimal.valueOf(scale.toDouble()).setScale(1, RoundingMode.HALF_DOWN).toFloat()
            } catch (e: Exception) {
                1F
            }
        }

        private fun parseColor(value: String?): Int {
            return if (!value.isNullOrEmpty() && value != "#00000000" && value != "none") {
                try {
                    Color.parseColor(value)
                } catch (e: IllegalArgumentException) {
                    if (value.matches("#[a-fA-F0-9]{3}".toRegex())) {
                        val first = value.substring(1, 2)
                        val second = value.substring(2, 3)
                        val third = value.substring(3, 4)
                        val hex = "#$first$first$second$second$third$third"
                        try {
                            Color.parseColor(hex)
                        } catch (exception: IllegalArgumentException) {
                            0
                        }
                    } else {
                        0
                    }
                }
            } else {
                0
            }
        }
    }
}