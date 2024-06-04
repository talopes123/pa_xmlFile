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


val adapter = XMLDocument.FUCAdapter(XMLDocument.AdapterConfig(

    removeDuplicates = true,
    addDefaultAttributes = null,
    sortChildrenByName = false,
    renameComponents = true,
    tagRenames = mapOf("componenteavaliacao" to "componente")
))


fun main() {

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

    val document = XMLDocument()
    val xmlElement = mapToXML(ff)
    adapter.adapt(xmlElement) // Aplica adaptações
    //println(xmlElement.prettyPrint())
    document.rootElement.addChild(xmlElement)
    val ficheiro = "output.xml"
    document.writeToFile(ficheiro)
}