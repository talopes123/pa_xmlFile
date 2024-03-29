//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {


    val entity1 = Entity("curso",null, "Mestrado em Engenharia Informatica")
    val entity2 = Entity("ano")


    val xml = XMLDocument()

    xml.addEntity(entity1)
    xml.addEntity(entity2)

    xml.removeEntity(entity2)

    print(xml.prettyPrint())

}
