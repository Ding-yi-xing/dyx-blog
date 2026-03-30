package com.dyx.blog.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * XSS 过滤工具类。
 * 使用 Jsoup 对 HTML 内容进行清洗，防止跨站脚本攻击。
 */
public class XssUtil {

    /**
     * 清洗文本内容，移除所有 HTML 标签。
     * 适用于留言、标题等纯文本字段。
     *
     * @param content 待清洗的内容。
     * @return 清洗后的纯文本。
     */
    public static String cleanText(String content) {
        if (content == null) {
            return null;
        }
        return Jsoup.clean(content, Safelist.none());
    }

    /**
     * 清洗富文本内容，保留安全的 HTML 标签（如 p, b, i, img 等）。
     * 适用于博客文章、动态内容等。
     *
     * @param html 待清洗的 HTML 内容。
     * @return 清洗后的安全 HTML。
     */
    public static String cleanHtml(String html) {
        if (html == null) {
            return null;
        }
        // 使用 relaxed 白名单，允许基本的富文本标签和属性
        Safelist safelist = Safelist.relaxed()
                .addAttributes("img", "align", "alt", "height", "src", "title", "width")
                .addProtocols("img", "src", "http", "https", "data");
        
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", safelist, outputSettings);
    }
}
