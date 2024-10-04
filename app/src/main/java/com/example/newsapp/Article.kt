import java.io.Serializable

data class Article(
    var title: String = "",
    var content: String = "",
    var description: String = "", // Add description
    var imageUrl: String = "",
    var author: String = "",
    var date: String = "",
    var category: String = ""
) : Serializable
