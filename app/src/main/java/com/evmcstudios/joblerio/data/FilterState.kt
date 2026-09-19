package com.evmcstudios.joblerio.data

data class FilterState(
    val radius: Int = 25,
    val sortBy: String = "relevance",
    val categories: Set<Int> = emptySet()
)

object JobCategories {
    data class Category(val id: Int, val name: String)

    val all = listOf(
        Category(1, "Accounting & Finance"),
        Category(2, "Administrative & Office"),
        Category(3, "Advertising & Marketing"),
        Category(4, "Animal Care"),
        Category(5, "Art, Fashion & Design"),
        Category(6, "Business Operations"),
        Category(7, "Cleaning & Facilities"),
        Category(8, "Computer & IT"),
        Category(9, "Construction"),
        Category(10, "Customer Service"),
        Category(11, "Education"),
        Category(12, "Energy, Mining & Natural Resources"),
        Category(13, "Entertainment & Travel"),
        Category(14, "Farming & Outdoor"),
        Category(15, "Government"),
        Category(16, "Healthcare"),
        Category(17, "Hospitality"),
        Category(18, "Human Resources"),
        Category(19, "Installation, Maintenance & Repair"),
        Category(20, "Legal"),
        Category(21, "Manufacturing & Production"),
        Category(22, "Media, Communications & Writing"),
        Category(23, "Operators"),
        Category(24, "Personal Care & Services"),
        Category(25, "Protective Services & Security"),
        Category(26, "Real Estate"),
        Category(27, "Restaurant & Food Services"),
        Category(28, "Retail"),
        Category(29, "Sales"),
        Category(30, "Science"),
        Category(31, "Social Services & Nonprofit"),
        Category(32, "Sports, Fitness & Recreation"),
        Category(33, "Transportation & Logistics"),
        Category(34, "Warehouse"),
        Category(35, "Gig")
    )

    val popular = listOf(8, 16, 33, 29, 10, 28, 6, 21, 19, 34)

    fun getPopularCategories(): List<Category> = all.filter { it.id in popular }

    fun getCategoryParam(ids: Set<Int>): String {
        return ids.joinToString("&") { "cat=$it" }
    }
}
