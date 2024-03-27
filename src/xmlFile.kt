
class XMLElement (val entity : Entity){
}

 sealed interface Entity {
     val name : String
     val parent: Entity?
}

