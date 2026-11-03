package com.wardround.app.data

/**
 * 内置常用药字典（外科常用）。
 * 首次启动时若 drugs 表为空则写入。
 */
object DrugSeed {
    data class Seed(val name: String, val dose: String, val unit: String, val usage: String)

    private val seeds = listOf(
        // 抗生素
        Seed("头孢呋辛", "1.5", "g", "bid"),
        Seed("头孢曲松", "2", "g", "qd"),
        Seed("头孢他啶", "2", "g", "q8h"),
        Seed("头孢唑林", "2", "g", "q8h"),
        Seed("哌拉西林他唑巴坦", "4.5", "g", "q8h"),
        Seed("阿莫西林克拉维酸钾", "1.2", "g", "q8h"),
        Seed("左氧氟沙星", "0.5", "g", "qd"),
        Seed("甲硝唑", "0.5", "g", "q12h"),
        Seed("美罗培南", "1", "g", "q8h"),
        Seed("万古霉素", "1", "g", "q12h"),
        Seed("庆大霉素", "80", "mg", "qd"),
        // 抑酸护胃
        Seed("奥美拉唑", "40", "mg", "qd"),
        Seed("泮托拉唑", "40", "mg", "qd"),
        Seed("兰索拉唑", "30", "mg", "qd"),
        Seed("雷贝拉唑", "20", "mg", "qd"),
        Seed("西咪替丁", "0.4", "g", "bid"),
        // 止吐促动力
        Seed("甲氧氯普胺", "10", "mg", "tid"),
        Seed("多潘立酮", "10", "mg", "tid"),
        // 镇痛
        Seed("曲马多", "100", "mg", "prn"),
        Seed("布桂嗪", "100", "mg", "prn"),
        Seed("氟比洛芬酯", "50", "mg", "bid"),
        Seed("帕瑞昔布", "40", "mg", "bid"),
        // 解痉
        Seed("山莨菪碱", "10", "mg", "tid"),
        Seed("间苯三酚", "80", "mg", "qd"),
        // 激素
        Seed("地塞米松", "5", "mg", "qd"),
        Seed("甲泼尼龙", "40", "mg", "qd"),
        // 维生素与电解质
        Seed("维生素C", "2", "g", "qd"),
        Seed("维生素B6", "0.2", "g", "qd"),
        Seed("氯化钾", "1", "g", "qd"),
        Seed("葡萄糖酸钙", "1", "g", "qd"),
        Seed("硫酸镁", "2.5", "g", "qd"),
        // 止血
        Seed("氨甲环酸", "0.5", "g", "bid"),
        Seed("酚磺乙胺", "0.5", "g", "bid"),
        Seed("维生素K1", "10", "mg", "qd"),
        // 抗凝
        Seed("低分子肝素", "4000", "U", "qd"),
        Seed("那屈肝素", "4100", "U", "qd"),
        // 化痰呼吸
        Seed("氨溴索", "30", "mg", "bid"),
        Seed("多索茶碱", "0.2", "g", "bid"),
        Seed("布地奈德", "1", "mg", "bid"),
        // 强心利尿升压
        Seed("呋塞米", "20", "mg", "qd"),
        Seed("多巴胺", "20", "mg", "qd"),
        Seed("去甲肾上腺素", "2", "mg", "qd"),
        Seed("米力农", "5", "mg", "qd")
    )

    fun list(): List<Drug> = seeds.map {
        Drug(
            name = it.name,
            initials = DiagnosisList.pinyinInitials(it.name),
            defaultDose = it.dose,
            defaultUnit = it.unit,
            defaultUsage = it.usage
        )
    }
}
