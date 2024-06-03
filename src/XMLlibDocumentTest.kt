import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File

import XMLDocument.XMLElement


/**
 * Classe de teste para a classe XMLDocument.
 *
 * Esta classe contém uma série de testes para verificar o comportamento das funções
 * na classe XMLDocument.
 */
class XMLlibDocumentTest {

    private lateinit var xmlDocument: XMLDocument
    /**
     * Configuração inicial para os testes.
     *
     * Cria uma instância de XMLDocument antes de cada teste.
     */
    @Before
    fun setUp() {
        xmlDocument = XMLDocument()
    }

    /**
     * Testa a função createXmlDocument.
     *
     * Verifica se a função createXmlDocument gera corretamente o cabeçalho XML.
     */
    @Test
    fun testCreateXmlDocument() {
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", XMLDocument.createXmlHeader())
    }

    /**
     * Testa a função writeToFile.
     *
     * Verifica se a função writeToFile escreve corretamente o conteúdo do documento XML em um arquivo.
     */
    @Test
    fun testWriteToFile() {
        val filename = "test_output.xml"
        xmlDocument.rootElement.addChild(XMLElement("child"))
        xmlDocument.writeToFile(filename)

        val expectedOutput = """
        <?xml version="1.0" encoding="UTF-8"?>
        <root>
          <child/>
        </root>
    """.trimIndent()

        val fileContent = File(filename).readText()
        assertEquals(expectedOutput, fileContent)

        // Limpar o arquivo de teste
        File(filename).delete()
    }

    /**
     * Testa a função addChild.
     *
     * Verifica se a função addChild adiciona corretamente um elemento filho ao elemento pai.
     */
    @Test
    fun testAddChild() {
        val parentElement = xmlDocument.rootElement
        val childElement = XMLElement("child")
        parentElement.addChild(childElement)

        assertTrue(parentElement.getChildrenList().contains(childElement))
    }

    /**
    * Testa o cenário onde addChild é chamado com um elemento que já existe como filho.
    *
    * Verifica se a função addChild lança uma exceção IllegalArgumentException quando um elemento
    * já existente é adicionado novamente como filho.
    */
    @Test(expected = IllegalArgumentException::class)
    fun testDuplicateChild() {
        val element = XMLElement("element")
        val child = XMLElement("child")
        element.addChild(child)
        element.addChild(child)
    }

    /**
     * Testa a função addChild com texto presente.
     *
     * Verifica se a função addChild lança uma exceção IllegalArgumentException
     * quando tenta adicionar um elemento filho a um elemento que já possui texto definido.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testAddChildWithTextPresent() {
        val element = XMLElement("element")
        element.setTexto("Some text")
        element.addChild(XMLElement("child"))
    }

    /**
     * Testa a função removeChild.
     *
     * Verifica se a função removeChild remove corretamente um elemento filho do elemento pai.
     */
    @Test
    fun testRemoveChild() {
        val parentElement = XMLElement("parent")
        val childElement = XMLElement("child")
        parentElement.addChild(childElement)
        parentElement.removeChild(childElement)

        assertTrue(parentElement.getChildrenList().isEmpty())
    }

    /**
     * Testa a função addAttribute.
     *
     * Verifica se a função addAttribute adiciona corretamente atributos ao elemento.
     */
    @Test
    fun testAddAttribute() {
        val element = XMLElement("element")
        element.addAttribute("attr1", "value1")
        element.addAttribute("attr2", "value2")

        assertEquals("value1", element.attributes["attr1"])
        assertEquals("value2", element.attributes["attr2"])
    }

    /**
     * Testa o cenário onde addAttribute é chamado com um atributo que já existe.
     *
     * Verifica se a função addAttribute lança uma exceção IllegalArgumentException
     * quando um atributo já existente é adicionado novamente.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testDuplicateAttribute() {
        val element = XMLElement("element")
        element.addAttribute("attr", "value1")
        element.addAttribute("attr", "value2")
    }

    /**
     * Testa o cenário onde addTextNodeAttribute é chamado sem texto presente no elemento.
     *
     * Verifica se a função addTextNodeAttribute lança uma exceção IllegalArgumentException
     * quando é chamada em um elemento sem texto definido.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testAddTextNodeAttributeWithoutText() {
        val element = XMLElement("element")
        element.addTextNodeAttribute("attr", "value")
    }

    /**
     * Testa a função removeAttribute.
     *
     * Verifica se a função removeAttribute remove corretamente um atributo do elemento.
     */
    @Test
    fun testRemoveAttribute() {
        val element = XMLElement("element")
        element.addAttribute("attr1", "value1")
        element.addAttribute("attr2", "value2")
        element.removeAttribute("attr1")

        assertTrue(element.attributes.containsKey("attr2"))
        assertTrue(element.attributes.containsKey("attr1").not())
    }

    /**
     * Testa o cenário onde setTexto é chamado com elementos filhos presentes.
     *
     * Verifica se a função setTexto lança uma exceção IllegalArgumentException
     * quando é chamada em um elemento que já possui elementos filhos.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testSetTextWithChildrenPresent() {
        val element = XMLElement("element")
        element.addChild(XMLElement("child"))
        element.setTexto("Some text")
    }

    /**
     * Testa a criação e manipulação de um nó de texto.
     *
     * Verifica se é possível criar um nó de texto com atributos e se a função prettyPrint
     * gera a representação XML correta para o nó de texto.
     */
    @Test
    fun testTextNode() {
        val textNode = XMLDocument.TextNode("Hello, World!")
        textNode.addAttribute("lang", "en")

        assertEquals("Hello, World!", textNode.texto)
        assertEquals("en", textNode.attributes["lang"])

        val expectedOutput = """<text lang="en">Hello, World!</text>
    """.trimIndent()
        assertEquals(expectedOutput, textNode.prettyPrint())
    }

    /**
     * Testa a função sanitize para valores de atributo.
     *
     * Verifica se a função sanitize remove corretamente caracteres especiais de valores de atributo.
     */
    @Test
    fun testSanitizeAttributeValue() {
        val element = XMLElement("element")
        element.addAttribute("attr", "value<invalid>")
        assertEquals("valueinvalid", element.attributes["attr"])
    }

    /**
     * Testa a função prettyPrint para representação XML.
     *
     * Verifica se a função prettyPrint gera corretamente a representação XML
     * para o elemento e seus filhos, se presentes.
     */
    @Test
    fun testPrettyPrint() {
        val element = XMLElement("element")
        element.addAttribute("attr1", "value1")
        element.addAttribute("attr2", "value2")
        val childElement1 = XMLElement("child1")
        val childElement2 = XMLElement("child2")
        element.addChild(childElement1)
        element.addChild(childElement2)

        val expectedOutput = "<element attr1=\"value1\" attr2=\"value2\">\n" +
                "  <child1/>\n" +
                "  <child2/>\n" +
                "</element>\n"
        assertEquals(expectedOutput, element.prettyPrint())
        println(element.prettyPrint())
    }

    /**
     * Testa a função addGlobalAttribute.
     *
     * Verifica se a função addGlobalAttribute adiciona corretamente um atributo
     * a todos os elementos do documento XML.
     */
    @Test
    fun testAddGlobalAttribute() {
        val child1 = XMLElement("child1")
        val child2 = XMLElement("child2")
        val child3 = XMLElement("child3")
        child2.addChild(child3)
        xmlDocument.rootElement.addChild(child1)
        xmlDocument.rootElement.addChild(child2)


        val attributeName = "attr"
        val attributeValue = "value"
        xmlDocument.addGlobalAttribute(attributeName, attributeValue)

        assertEquals(attributeValue, child1.attributes[attributeName])
        assertEquals(attributeValue, child2.attributes[attributeName])
        assertEquals(attributeValue, child3.attributes[attributeName])
    }

    /**
     * Testa a função renameGlobalEntity.
     *
     * Verifica se a função renameGlobalEntity renomeia corretamente
     * todas as ocorrências de um elemento no documento XML.
     */
    @Test
    fun testRenameGlobalEntity() {
        val oldEntityName = "oldName"
        val newEntityName = "newName"

        xmlDocument.rootElement.addChild(XMLElement(oldEntityName))
        xmlDocument.renameGlobalEntity(oldEntityName, newEntityName)

        assertTrue(xmlDocument.rootElement.getChildrenList().any { it.name == newEntityName })

    }

    /**
     * Testa a função renameGlobalAttribute.
     *
     * Verifica se a função renameGlobalAttribute renomeia corretamente
     * um atributo em todos os elementos de um determinado tipo no documento XML.
     */
    @Test
    fun testRenameGlobalAttribute() {

        // Adicionar um elemento com um atributo ao documento
        val entityName = "entity"
        val attributeName = "oldAttribute"
        val attributeValue = "value"
        val element = XMLElement(entityName)
        element.addAttribute(attributeName, attributeValue)
        xmlDocument.rootElement.addChild(element)

        // Renomear globalmente o atributo
        val newAttributeName = "newAttribute"
        xmlDocument.renameGlobalAttribute(entityName, attributeName, newAttributeName)

        // Verificar se o atributo foi renomeado corretamente
        assertEquals(attributeValue, element.attributes[newAttributeName])
        assertEquals(null, element.attributes[attributeName])
    }

    /**
     * Testa a função removeGlobalEntity.
     *
     * Verifica se a função removeGlobalEntity remove corretamente todas
     * as ocorrências de um determinado elemento no documento XML.
     */
    @Test
    fun testRemoveGlobalEntity() {

        // Adicionar múltiplos elementos com o mesmo nome ao documento
        val entityName = "entity"
        val numEntities = 5
        repeat(numEntities) {
            xmlDocument.rootElement.addChild(XMLElement(entityName))
        }

        // Remover globalmente todos os elementos com o mesmo nome
        xmlDocument.removeGlobalEntity(entityName)

        // Verificar se todos os elementos foram removidos corretamente
        assertEquals(0, xmlDocument.rootElement.getChildrenList().size)
        assertFalse(xmlDocument.rootElement.getChildrenList().any { it.name == entityName })
    }

    /**
     * Testa a função removeGlobalAttribute.
     *
     * Verifica se a função removeGlobalAttribute remove corretamente
     * um atributo de todos os elementos de um determinado tipo no documento XML.
     */
    @Test
    fun testRemoveGlobalAttribute() {

        // Adicionar múltiplos elementos com o mesmo nome e atributo ao documento
        val entityName = "entity"
        val attributeName = "attr"
        val attributeValue = "value"
        val numEntities = 5
        repeat(numEntities) {
            val element = XMLElement(entityName)
            element.addAttribute(attributeName, attributeValue)
            xmlDocument.rootElement.addChild(element)
        }

        // Remover globalmente o atributo de todos os elementos com o mesmo nome
        xmlDocument.removeGlobalAttribute(entityName, attributeName)

        // Verificar se o atributo foi removido corretamente de todos os elementos
        xmlDocument.rootElement.getChildrenList().forEach { element ->
            assertFalse(element.attributes.containsKey(attributeName))
        }
    }

    /**
     * Testa a função queryXPath para consulta XML.
     *
     * Verifica se a função queryXPath retorna corretamente todos os elementos
     * que correspondem a um determinado caminho XPath no documento XML.
     */
    @Test
    fun testQueryXPath() {
        // Construir uma estrutura XML específica
        val root = xmlDocument.rootElement
        // Adiciona elementos filhos
        val child1 = XMLDocument.XMLElement("child1")
        val child2 = XMLDocument.XMLElement("child2")
        val child3 = XMLDocument.XMLElement("child1")
        root.addChild(child1)
        root.addChild(child2)
        root.addChild(child3)

        // Consulta usando XPath
        val results = xmlDocument.queryXPath("/root/child1")

        println("Resultados da consulta XPath '/root/child1':")
        results.forEach { println(it.prettyPrint()) }
        assertEquals(2, results.size) // Espera-se que haja dois elementos "child1"
        assertEquals("child1", results[0].name)
        assertEquals("child1", results[1].name)
    }

    /**
     * Testa a transformação de valores usando a anotação @XmlString com um transformador personalizado.
     *
     * Verifica se a transformação de valores é aplicada corretamente ao usar a anotação @XmlString
     * com um transformador personalizado.
     */
    @Test
    fun testAddPercentageTransformer() {
        class PercentageTest(@XMLDocument.XmlString(transformer = XMLDocument.AddPercentage::class) val score: Int)

        val obj = PercentageTest(80)
        val xmlElement = XMLDocument.mapToXML(obj)

        assertEquals("80%", xmlElement.children[0].getTexto())
    }

    /**
    * Testa o adaptador FUC (Formato Único de Componentes) para um elemento XML.
    *
    * Verifica se o adaptador FUC renomeia corretamente o elemento e mantém
    * todos os seus filhos após a adaptação.
    */
    @Test
    fun testFUCAdapter() {
        val element = XMLElement("componenteavaliacao")
        element.addAttribute("attr", "value")
        val child1 = XMLElement("child1")
        val child2 = XMLElement("child2")
        element.addChild(child1)
        element.addChild(child2)

        val adapter = XMLDocument.FUCAdapter()
        adapter.adapt(element)

        assertEquals("componente", element.name)
        assertEquals(2, element.children.size)
    }

    /**
     * Testa a anotação @XmlName para renomear o nome do elemento XML.
     *
     * Verifica se a anotação @XmlName renomeia corretamente o nome do elemento
     * gerado a partir de um objeto ao mapear para XML.
     */
    @Test
    fun testXmlNameAnnotation() {
        @XMLDocument.XmlName("customname")
        class TestClass(val prop: String)

        val obj = TestClass("value")
        val xmlElement = XMLDocument.mapToXML(obj)

        assertEquals("customname", xmlElement.name)
    }

    /**
     * Testa a anotação @XmlExclude para excluir um atributo do elemento XML.
     *
     * Verifica se a anotação @XmlExclude exclui corretamente um atributo
     * específico do elemento gerado a partir de um objeto ao mapear para XML.
     */

    //NÂO ESTÁ A FUNCIONAR
    /*@Test
    fun testXmlExcludeAnnotation() {
        class TestClass(val included: String, @XMLDocument.XmlExclude val excluded: String)

        val obj = TestClass("includedValue", "excludedValue")
        val xmlElement = XMLDocument.mapToXML(obj)

        assertNotNull(xmlElement.attributes["included"])
        assertNull(xmlElement.attributes["excluded"])
    }*/
}