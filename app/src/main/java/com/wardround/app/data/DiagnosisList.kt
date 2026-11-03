package com.wardround.app.data

import android.content.Context
import net.sourceforge.pinyin4j.PinyinHelper

/**
 * ICD-11 (MMS) 中文诊断库。
 *
 * 数据来源：WHO ICD-11 2024-01 官方发布文件
 * SimpleTabulation-ICD-11-MMS-zh（已剔除扩展码 X 码，仅保留疾病类目）。
 * 完整细目共 17,822 条类目，含编码、中文名称与拼音首字母。
 */
object DiagnosisList {

    data class Entry(
        val code: String,
        val title: String,
        val initials: String
    )

    @Volatile
    private var cachedEntries: List<Entry>? = null

    /** 从 assets 中懒加载 ICD-11 中文细目（仅加载一次）。 */
    fun entries(context: Context): List<Entry> {
        cachedEntries?.let { return it }
        synchronized(this) {
            cachedEntries?.let { return it }
            val list = parseAsset(context)
            cachedEntries = list
            return list
        }
    }

    private fun parseAsset(context: Context): List<Entry> {
        val result = ArrayList<Entry>(18000)
        context.assets.open("icd11_mms_zh.tsv").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                if (line.isEmpty()) return@forEach
                val parts = line.split('\t')
                if (parts.size >= 3) {
                    result.add(Entry(parts[0], parts[1], parts[2]))
                }
            }
        }
        return result
    }

    /**
     * 搜索诊断。优先级：
     * 1) 名称以关键词开头；2) 名称包含关键词（汉字模糊）；
     * 3) 拼音首字母以关键词开头；4) 拼音首字母包含关键词。
     */
    fun search(context: Context, query: String): List<Entry> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val all = entries(context)
        val qUpper = q.uppercase()

        val starts = ArrayList<Entry>()
        val contains = ArrayList<Entry>()
        val initialsStarts = ArrayList<Entry>()
        val initialsContains = ArrayList<Entry>()

        for (e in all) {
            when {
                e.title.startsWith(q, ignoreCase = true) -> starts.add(e)
                e.title.contains(q, ignoreCase = true) -> contains.add(e)
                e.initials.startsWith(qUpper) -> initialsStarts.add(e)
                e.initials.contains(qUpper) -> initialsContains.add(e)
            }
        }

        val result = ArrayList<Entry>(40)
        result.addAll(starts)
        result.addAll(contains)
        result.addAll(initialsStarts)
        result.addAll(initialsContains)
        return result.take(40)
    }

    private val initialsCache = HashMap<String, String>()

    /** 计算汉字字符串的拼音首字母（用于自定义诊断、药品等）。 */
    fun pinyinInitials(text: String): String {
        return initialsCache.getOrPut(text) {
            val sb = StringBuilder()
            text.forEach { c ->
                if (c.code > 127) {
                    try {
                        val arr = PinyinHelper.toHanyuPinyinStringArray(c)
                        if (arr != null && arr.isNotEmpty()) sb.append(arr[0][0].uppercaseChar())
                    } catch (e: Exception) {
                    }
                } else {
                    sb.append(c.uppercaseChar())
                }
            }
            sb.toString()
        }
    }
}