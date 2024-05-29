import XMLDocument.Companion.mapToXML
@XMLDocument.XmlAdapter(XMLDocument.FUCAdapter::class)
class Plano(
    val curso : String,
    val fucs : List<FUC>
)
@XMLDocument.XmlElementOrder("nome", "ects", "codigo", "avaliacao")
class FUC(
    @XMLDocument.XmlAttribute
    val codigo: String,
    @XMLDocument.XmlName("nome_fuc")
    val nome: String,
    val ects: Double,
    @XMLDocument.XmlExclude
    val observacoes: String,
    val avaliacao: List<ComponenteAvaliacao>
)
class ComponenteAvaliacao(
    @XMLDocument.XmlAttribute
    val nome: String,
    @XMLDocument.XmlAttribute
    @XMLDocument.XmlString(XMLDocument.AddPercentage::class)
    val peso: Int
)



fun main() {

    /*val f = XMLDocument.FUC(
        "M4310", "Programação Avançada", 6.0, "la la...",
        listOf(
            XMLDocument.ComponenteAvaliacao("Quizzes", 20),
            XMLDocument.ComponenteAvaliacao("Projeto", 80)
        )
    )*/


    val ff = Plano(
        "Engenharia Informatica",
        listOf(
            FUC(
                "M4310", "Programação Avançada", 6.0, "la la...",
                listOf(
                    ComponenteAvaliacao("Quizzes", 20),
                    ComponenteAvaliacao("Projeto", 80)
                )
            ),
            FUC(
                "34568", "Programação Avançada", 6.0, "la la...",
                listOf(
                    ComponenteAvaliacao("Dissertacao", 60),
                    ComponenteAvaliacao("Apresentacao", 20),
                    ComponenteAvaliacao("Discussao", 20),
                )
            )
        )
    )
    /*val document = XMLDocument()

    val parent = document.rootElement;

    val entity = XMLDocument.XMLElement("plano")
    val entity2 = XMLDocument.XMLElement("curso")
    val entity3 = XMLDocument.XMLElement("fuc")
    entity.addChild(entity2)
    entity.addChild(entity3)
    entity3.addAttribute("codigo","M4310")
    val entity4 = XMLDocument.XMLElement("nome")
    val entity5 = XMLDocument.XMLElement("ects")
    val entity6 = XMLDocument.XMLElement("avaliacao")
    val entity7 = XMLDocument.XMLElement("componente")
    entity3.addChild(entity4)
    entity3.addChild(entity5)
    entity3.addChild(entity6)
    entity6.addChild(entity7)
    entity7.addAttribute( "nome", "Dissertacao")
    entity7.addAttribute( "peso", "60%")

    parent.addChild(entity)*/

    val xmlElement = mapToXML(ff)
    XMLDocument.FUCAdapter().adapt(xmlElement) // Aplica adaptações
    println(xmlElement.prettyPrint())
}