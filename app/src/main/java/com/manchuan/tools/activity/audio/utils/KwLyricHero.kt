package com.manchuan.tools.activity.audio.utils

import com.drake.net.utils.runMain
import com.manchuan.tools.activity.audio.model.LyricEntity
import com.manchuan.tools.extensions.loge
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Arrays
import kotlin.concurrent.thread
import kotlin.math.abs

class KwLyricHero private constructor() {
    // 歌词 API (酷我)
    //    private final String LYRIC_KW_API = "http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=%s&httpsStatus=1";
    private val LYRIC_KW_API = "http://newlyric.kuwo.cn/newlyric.lrc?"

    fun fillLrc(
        musicInfo: String,
        success: (lyric: String, translate: String) -> Unit,
    ): LyricEntity? {
        val id: String = musicInfo
        var lyric = ""
        var translate = ""
        // 请求参数加密
        val keyBytes = "yeelion".toByteArray(StandardCharsets.UTF_8)
        val keyLen = keyBytes.size
        var params = "user=12345,web,web,web&requester=localhost&req=1&rid=MUSIC_$id&lrcx=1"
        val paramsBytes = params.toByteArray(StandardCharsets.UTF_8)
        val paramsLen = paramsBytes.size
        var output = ByteArray(paramsLen)
        var i = 0
        while (i < paramsLen) {
            var j = 0
            while (j < keyLen && i < paramsLen) {
                output[i] = (keyBytes[j].toInt() xor paramsBytes[i].toInt()).toByte()
                i++
                j++
            }
        }
        params = CryptoUtil.base64Encode(output)
        thread {
            val client = OkHttpClient()
            val request = Request.Builder().url(LYRIC_KW_API + params).build()
            val call = client.newCall(request)
            val response = call.execute()//execute方法会阻塞在这里，必须等到服务器响应，得到response才会执行下面的代码
            val bodyByteArray = response.body?.bytes()
            if ("tp=content" != bodyByteArray?.let { String(it, 0, 10) }) return@thread
            val index = ArrayUtil.indexOf(
                bodyByteArray, "\r\n\r\n".toByteArray(StandardCharsets.UTF_8)
            ) + 4
            val nBytes = Arrays.copyOfRange(bodyByteArray, index, bodyByteArray.size)
            val lrcData = CryptoUtil.decompress(nBytes)
            // 无 lrcx 参数时，此处直接获得 lrc 歌词
//        String lrcStr = new String(lrcData, Charset.forName("gb18030"));
            val lrcDataStr = String(lrcData, StandardCharsets.UTF_8)
            val lrcBytes = CryptoUtil.base64DecodeToBytes(lrcDataStr)
            val lrcLen = lrcBytes.size
            output = ByteArray(lrcLen)
            i = 0
            while (i < lrcLen) {
                var j = 0
                while (j < keyLen && i < lrcLen) {
                    output[i] = (lrcBytes[i].toInt() xor keyBytes[j].toInt()).toByte()
                    i++
                    j++
                }
            }
            var lrcStr = String(output, Charset.forName("gb18030"))

            // 解析酷我的偏移值
            var offset = 1
            var offset2 = 1
            val kuwoValStr = RegexUtil.getGroup1("\\[kuwo:(\\d+)\\]", lrcStr)
            if (StringUtil.notEmpty(kuwoValStr)) {
                val kuwoVal = kuwoValStr.toInt(8)
                offset = kuwoVal / 10
                offset2 = kuwoVal % 10
            }
            // 解析逐字歌词
            val lineTimeExp = "\\[\\d+:\\d+(?:[.:]\\d+)?\\]"
            val lsp = lrcStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var sb = StringBuilder()
            for (l in lsp) {
                val s1List = RegexUtil.findAllGroup1("<(\\d+),-?\\d+>", l)
                if (s1List.isEmpty()) sb.append(l)
                else {
                    val s2List = RegexUtil.findAllGroup1("<\\d+,(-?\\d+)>", l)
                    // 行时间
                    val lineTimeStr = RegexUtil.getGroup0(lineTimeExp, l)
                    sb.append(lineTimeStr)
                    val sp = ArrayUtil.removeFirstEmpty(
                        l.replaceFirst(lineTimeExp.toRegex(), "").split("<\\d+,-?\\d+>".toRegex())
                            .toTypedArray()
                    )
                    var k = 0
                    val s = s1List.size
                    while (k < s) {
                        val n1 = s1List[k].toInt()
                        val n2 = s2List[k].toInt()
                        val wordStartTime = abs(((n1 + n2) / (offset * 2)).toDouble()).toInt()
                        val wordDuration = abs(((n1 - n2) / (offset2 * 2)).toDouble()).toInt()
                        sb.append("<").append(wordStartTime).append(",").append(wordDuration)
                            .append(">").append(sp[k])
                        k++
                    }
                }
                sb.append("\n")
            }
            lrcStr = sb.toString()

            // 分离歌词和翻译
            val sp = lrcStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            sb = StringBuilder()
            var hasTrans = false
            val s = sp.size
            for (j in 0 until s) {
                val sentence = sp[j]
                val nextSentence = if (j + 1 < s) sp[j + 1] else null
                // 歌词中带有翻译时，最后一句是翻译直接跳过
                if (hasTrans && StringUtil.isEmpty(nextSentence)) break
                val time = RegexUtil.getGroup0(lineTimeExp, sentence)
                if (StringUtil.isEmpty(time)) {
                    sb.append(sentence).append("\n")
                    continue
                }
                var nextTime: String? = null
                if (StringUtil.notEmpty(nextSentence)) nextTime =
                    RegexUtil.getGroup0(lineTimeExp, nextSentence)
                // 歌词中带有翻译，有多个 time 相同的歌词时取不重复的第二个
                if (time != nextTime) sb.append(sentence).append("\n")
                else hasTrans = true
            }
            //musicInfo.setLrc(sb.toString())
            loge(
                "常规歌词", sb.toString().replace("<\\d+,-?\\d+>".toRegex(), "")
            )
            lyric = sb.toString().replace("<\\d+,-?\\d+>".toRegex(), "")
            sb = StringBuilder()
            hasTrans = false
            var lastTime: String? = null
            i = 0
            while (i < s) {
                val sentence = sp[i]
                val nextSentence = if (i + 1 < s) sp[i + 1] else null
                val time = RegexUtil.getGroup0(lineTimeExp, sentence)
                if (StringUtil.isEmpty(time)) {
                    i++
                    continue
                }
                var nextTime: String? = null
                if (StringUtil.notEmpty(nextSentence)) nextTime =
                    RegexUtil.getGroup0(lineTimeExp, nextSentence)
                // 歌词中带有翻译，有多个 time 相同的歌词时取重复的第一个；最后一句也是翻译
                if (hasTrans && nextTime == null || time == nextTime) {
                    sb.append(lastTime)
                    sb.append(sentence.replaceFirst(lineTimeExp.toRegex(), ""))
                    sb.append("\n")
                    hasTrans = true
                }
                lastTime = time
                i++
            }
            // 去除翻译中无用的逐字时间轴
            translate = sb.toString().replace("<\\d+,\\d+>".toRegex(), "")
            loge("翻译歌词", sb.toString().replace("<\\d+,\\d+>".toRegex(), ""))
            runMain {
                success.invoke(lyric, translate)
            }
        }
        //musicInfo.setTrans(sb.toString().replace("<\\d+,\\d+>".toRegex(), ""))

        //            String lrcBody = SdkCommon.kwRequest(String.format(LYRIC_KW_API, id))
//                    .executeAsync()
//                    .body();
//            JSONObject data = JSONObject.parseObject(lrcBody).getJSONObject("data");
//            if (JsonUtil.isEmpty(data)) {
//                musicInfo.setLrc(null);
//                musicInfo.setTrans(null);
//                return;
//            }
//            // 酷我歌词返回的是数组，需要先处理成字符串！
//            // lrclist 可能是数组也可能为 null ！
//            JSONArray lrcArray = data.getJSONArray("lrclist");
//            if (JsonUtil.notEmpty(lrcArray)) {
//                StringBuilder sb = new StringBuilder();
//                boolean hasTrans = false;
//                for (int i = 0, len = lrcArray.size(); i < len; i++) {
//                    JSONObject sentenceJson = lrcArray.getJSONObject(i);
//                    JSONObject nextSentenceJson = i + 1 < len ? lrcArray.getJSONObject(i + 1) : null;
//                    // 歌词中带有翻译时，最后一句是翻译直接跳过
//                    if (hasTrans && JsonUtil.isEmpty(nextSentenceJson)) break;
//                    String time = TimeUtil.formatToLrcTime(sentenceJson.getDouble("time"));
//                    String nextTime = null;
//                    if (JsonUtil.notEmpty(nextSentenceJson))
//                        nextTime = TimeUtil.formatToLrcTime(nextSentenceJson.getDouble("time"));
//                    // 歌词中带有翻译，有多个 time 相同的歌词时取不重复的第二个
//                    if (!time.equals(nextTime)) {
//                        sb.append(time);
//                        String lineLyric = StringUtil.removeHTMLLabel(sentenceJson.getString("lineLyric"));
//                        sb.append(lineLyric);
//                        sb.append("\n");
//                    } else hasTrans = true;
//                }
//                musicInfo.setLrc(sb.toString());
//            } else musicInfo.setLrc(null);
//
//            // 酷我歌词返回的是数组，需要先处理成字符串！
//            // lrclist 可能是数组也可能为 null ！
//            if (JsonUtil.notEmpty(lrcArray)) {
//                StringBuilder sb = new StringBuilder();
//                boolean hasTrans = false;
//                String lastTime = null;
//                for (int i = 0, len = lrcArray.size(); i < len; i++) {
//                    JSONObject sentenceJson = lrcArray.getJSONObject(i);
//                    JSONObject nextSentenceJson = i + 1 < len ? lrcArray.getJSONObject(i + 1) : null;
//                    String time = TimeUtil.formatToLrcTime(sentenceJson.getDouble("time"));
//                    String nextTime = null;
//                    if (JsonUtil.notEmpty(nextSentenceJson))
//                        nextTime = TimeUtil.formatToLrcTime(nextSentenceJson.getDouble("time"));
//                    // 歌词中带有翻译，有多个 time 相同的歌词时取重复的第一个；最后一句也是翻译
//                    if (hasTrans && nextTime == null || time.equals(nextTime)) {
//                        sb.append(lastTime);
//                        String lineLyric = StringUtil.removeHTMLLabel(sentenceJson.getString("lineLyric"));
//                        sb.append(lineLyric);
//                        sb.append("\n");
//                        hasTrans = true;
//                    }
//                    lastTime = time;
//                }
//                musicInfo.setTrans(sb.toString());
//            } else musicInfo.setTrans(null);
        return LyricEntity(lyric, translate)
    }

    companion object {
        var instance: KwLyricHero? = null
            get() {
                if (field == null) field = KwLyricHero()
                return field
            }
            private set
    }
}