package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExifModels(
    @SerialName("mAreThumbnailStripsConsecutive")
    var mAreThumbnailStripsConsecutive: Boolean,
    @SerialName("mAttributes")
    var mAttributes: List<MAttribute>,
    @SerialName("mAttributesOffsets")
    var mAttributesOffsets: List<Int>,
    @SerialName("mExifByteOrder")
    var mExifByteOrder: MExifByteOrder,
    @SerialName("mFilename")
    var mFilename: String,
    @SerialName("mHasThumbnail")
    var mHasThumbnail: Boolean,
    @SerialName("mHasThumbnailStrips")
    var mHasThumbnailStrips: Boolean,
    @SerialName("mIsExifDataOnly")
    var mIsExifDataOnly: Boolean,
    @SerialName("mMimeType")
    var mMimeType: Int,
    @SerialName("mModified")
    var mModified: Boolean,
    @SerialName("mOffsetToExifData")
    var mOffsetToExifData: Int,
    @SerialName("mOrfMakerNoteOffset")
    var mOrfMakerNoteOffset: Int,
    @SerialName("mOrfThumbnailLength")
    var mOrfThumbnailLength: Int,
    @SerialName("mOrfThumbnailOffset")
    var mOrfThumbnailOffset: Int,
    @SerialName("mSeekableFileDescriptor")
    var mSeekableFileDescriptor: MSeekableFileDescriptor,
    @SerialName("mThumbnailCompression")
    var mThumbnailCompression: Int,
    @SerialName("mThumbnailLength")
    var mThumbnailLength: Int,
    @SerialName("mThumbnailOffset")
    var mThumbnailOffset: Int,
    @SerialName("mXmpIsFromSeparateMarker")
    var mXmpIsFromSeparateMarker: Boolean
) {
    @Serializable
    data class MAttribute(
        @SerialName("ColorSpace")
        var colorSpace: ColorSpace?,
        @SerialName("ImageLength")
        var imageLength: ImageLength?,
        @SerialName("ImageWidth")
        var imageWidth: ImageWidth?,
        @SerialName("LightSource")
        var lightSource: LightSource?,
        @SerialName("Orientation")
        var orientation: Orientation?,
        @SerialName("PixelXDimension")
        var pixelXDimension: PixelXDimension?,
        @SerialName("PixelYDimension")
        var pixelYDimension: PixelYDimension?
    ) {
        @Serializable
        data class ColorSpace(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class ImageLength(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class ImageWidth(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class LightSource(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class Orientation(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class PixelXDimension(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )

        @Serializable
        data class PixelYDimension(
            @SerialName("bytes")
            var bytes: List<Int>,
            @SerialName("bytesOffset")
            var bytesOffset: Int,
            @SerialName("format")
            var format: Int,
            @SerialName("numberOfComponents")
            var numberOfComponents: Int
        )
    }

    @Serializable
    class MExifByteOrder

    @Serializable
    data class MSeekableFileDescriptor(
        @SerialName("descriptor")
        var descriptor: Int
    )
}