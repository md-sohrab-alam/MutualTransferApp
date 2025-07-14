data class Teacher(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val subject: String = "",
    val post: String = "",
    val district: String = "",
    val block: String = "",
    val schoolName: String = "",
    val preferredDistrict: String = "",
    val preferredBlock: String = "",
    val contact: Contact = Contact(),
    val timestamp: Long? = null
)

data class Contact(
    val email: String = "",
    val phone: String = ""
)
