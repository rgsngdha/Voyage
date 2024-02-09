package com.manchuan.tools.activity.audio.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Doge
 * @Description 字符串工具类
 * @Date 2020/12/15
 */
public class StringUtil {
    private static final Map<Character, String> cMap = new HashMap<>();

    static {
        cMap.put(' ', "&nbsp;");
        cMap.put('<', "&lt;");
        cMap.put('>', "&gt;");
        cMap.put('\n', "<br>");
    }

    /**
     * 判断字符串是否为 null 或 ""
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * 判断字符串是否不为 null 和 ""
     *
     * @param s
     * @return
     */
    public static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * 判断字符串是否为纯数字
     *
     * @param s
     * @return
     */
    public static boolean isNumber(String s) {
        return StrUtil.isNumeric(s);
    }

    /**
     * 数字位数
     *
     * @param n
     * @return
     */
    public static int bit(int n) {
        return String.valueOf(n).length();
    }

    /**
     * 转为数字
     *
     * @param s
     * @return
     */
    public static int toNumber(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 从某位置插入
     *
     * @param s
     * @return
     */
    public static String insert(String s, int index, String content) {
        StringBuilder sb = new StringBuilder(s);
        sb.insert(index, content);
        return sb.toString();
    }

    /**
     * 缩短字符串
     *
     * @param s
     * @param maxLen
     * @return
     */
    public static String shorten(String s, int maxLen) {
        if (maxLen <= 3 || s == null || s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    /**
     * 缩短字符串中所有连续空格
     *
     * @param s
     * @return
     */
    public static String shortenBlank(String s) {
        if (s == null) return s;
        return s.replaceAll(" +", " ");
    }

    /**
     * 格式化数字使其带中文单位(万、亿等)
     *
     * @param n
     * @return
     */
    public static String formatNumber(long n) {
        return formatNumberWithoutSuffix(n) + " 播放";
    }

    /**
     * 格式化数字使其带中文单位(万、亿等)
     *
     * @param n
     * @return
     */
    public static String formatNumberWithoutSuffix(long n) {
        if (n < 10000) return String.valueOf(n);
        if (n < 100000000) return String.format("%.1f 万", (double) n / 10000).replace(".0", "");
        return String.format("%.1f 亿", (double) n / 100000000).replace(".0", "");
    }

    /**
     * 解析数字（关键词：k、w、千、万、亿）例如 7.6万 -> 76000，1.25k -> 1250
     *
     * @param s
     * @return
     */
    public static long parseNumber(String s) {
        if (s.contains("k") || s.contains("千"))
            return (long) (Double.parseDouble(s.replaceAll("[k千]", "").trim()) * 1000);
        else if (s.contains("w") || s.contains("万"))
            return (long) (Double.parseDouble(s.replaceAll("[w万]", "").trim()) * 10000);
        else if (s.contains("亿")) return (long) (Double.parseDouble(s.replace("亿", "").trim()) * 100000000);
        return Long.parseLong(s.trim());
    }

    /**
     * 生成评分星星字符串
     *
     * @param n
     * @return
     */
    public static String genStar(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < n) sb.append('★');
            else sb.append('☆');
        }
        return sb.toString();
    }

    /**
     * 从元素提取文本，替换 <br><li><p><dd> 为 \n
     *
     * @param parentElement
     * @return
     */
    public static String getPrettyText(Element parentElement) {
        if (parentElement == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Node child : parentElement.childNodes()) {
            if (child instanceof TextNode) {
                sb.append(((TextNode) child).text());
            } else if (child instanceof Element) {
                Element childElement = (Element) child;
                sb.append(getPrettyText(childElement));
                String s = childElement.tag().getName().toLowerCase();
                if ("br".equals(s) || "li".equals(s) || "p".equals(s) || "dd".equals(s)) sb.append("\n");
            }
        }
        return sb.toString();
    }
    /**
     * 去掉字符串中所有 HTML 标签，并将转义后的符号还原
     *
     * @param s
     * @return
     */
    public static String removeHTMLLabel(String s) {
        if (s == null) return s;
        s = s.replaceAll("<br ?/?>", "\n");
        Pattern pattern = Pattern.compile("<[^>]+>");
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&apos;", "'");
    }

    /**
     * url 编码（会处理所有冲突的字符）
     *
     * @param s
     * @return
     */
    public static String urlEncodeAll(String s) {
        return URLUtil.encodeAll(s);
    }

    /**
     * url 编码（处理空白字符）
     *
     * @param s
     * @return
     */
    public static String urlEncodeBlank(String s) {
        return URLUtil.encodeBlank(s);
    }

    /**
     * url 解码
     *
     * @param s
     * @return
     */
    public static String urlDecode(String s) {
        return URLUtil.decode(s);
    }

    /**
     * 返回 s1 与 s2 相似度
     *
     * @param s1
     * @param s2
     * @return
     */
    public static double similar(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        return StrUtil.similar(s1, s2);
    }

    /**
     * 去除歌词字符串中无用的字符
     *
     * @param lrcStr
     * @return
     */
    public static String cleanLrcStr(String lrcStr) {
        return StringUtil.trimStringWith(lrcStr.replaceAll("[\t\r\n]", ""), ' ', ' ', '　');
    }

    /**
     * 去除字符串前后指定字符
     *
     * @param str
     * @param cs
     * @return
     */
    public static String trimStringWith(String str, char... cs) {
        if (str == null) return null;
        char[] chars = str.toCharArray();
        int len = chars.length;
        int st = 0;
        while (st < len && ArrayUtil.in(cs, chars[st])) st++;
        while (st < len && ArrayUtil.in(cs, chars[len - 1])) len--;
        return st > 0 || len < chars.length ? str.substring(st, len) : str;
    }

    /**
     * 用 padStr 左填充字符串 str 到指定长度
     *
     * @param str
     * @param len
     * @param padStr
     * @return
     */
    public static String padPre(String str, int len, String padStr) {
        return StrUtil.padPre(str, len, padStr);
    }

    /**
     * 用 padStr 右填充字符 str 到指定长度
     *
     * @param str
     * @param len
     * @param padChar
     * @return
     */
    public static String padAfter(String str, int len, char padChar) {
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < len) sb.append(padChar);
        return sb.toString();
    }
}
