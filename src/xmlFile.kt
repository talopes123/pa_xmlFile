
class XMLDocument(){

    val entities = mutableListOf<Entity>()
    fun prettyPrint() : String {
        var pretty : String? = ""

        for(element in entities){
            pretty += "<" + element.name + ">" + element.text + "</" + element.name + ">" +  "\n"
        }

        return pretty.toString();
    }

    fun addEntity(entity: Entity){
        entities.add(entity)
    }

    fun removeEntity(entity: Entity){
        entities.remove(entity)
    }

}

sealed interface  XMLElement {
    val name: String
    val parent: Entity?
}

 data class Entity(
     override val name: String,
     override val parent: Entity? = null,
     val text : String? = ""
 ) : XMLElement {
}

