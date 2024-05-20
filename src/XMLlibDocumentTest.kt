import org.junit.Test
import XMLDocument.XMLElement
import org.junit.Assert.*
import org.junit.Before

class XMLlibDocumentTest {

    private lateinit var xmlDocument: XMLDocument

    @Before
    fun setUp() {
        xmlDocument = XMLDocument()
    }


    @Test
    fun testCreateXmlDocument() {
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", XMLDocument.createXmlHeader())
    }

    @Test
    fun testAddChild() {
        val parentElement = xmlDocument.rootElement
        val childElement = XMLElement("child")
        parentElement.addChild(childElement)

        assertTrue(parentElement.children.contains(childElement))
    }

    @Test
    fun testRemoveChild() {
        val parentElement = XMLElement("parent")
        val childElement = XMLElement("child")
        parentElement.addChild(childElement)
        parentElement.removeChild(childElement)

        assertTrue(parentElement.children.isEmpty())
    }

    @Test
    fun testAddAttribute() {
        val element = XMLElement("element")
        element.addAttribute("attr1", "value1")
        element.addAttribute("attr2", "value2")

        assertEquals("value1", element.attributes["attr1"])
        assertEquals("value2", element.attributes["attr2"])
    }

    @Test
    fun testRemoveAttribute() {
        val element = XMLElement("element")
        element.addAttribute("attr1", "value1")
        element.addAttribute("attr2", "value2")
        element.removeAttribute("attr1")

        assertTrue(element.attributes.containsKey("attr2"))
        assertTrue(element.attributes.containsKey("attr1").not())
    }

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

    @Test
    fun testRenameGlobalEntity() {
        val oldEntityName = "oldName"
        val newEntityName = "newName"

        xmlDocument.rootElement.addChild(XMLElement(oldEntityName))
        xmlDocument.renameGlobalEntity(oldEntityName, newEntityName)

        assertTrue(xmlDocument.rootElement.children.any { it.name == newEntityName })

    }

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
        assertEquals(0, xmlDocument.rootElement.children.size)
        assertFalse(xmlDocument.rootElement.children.any { it.name == entityName })
    }

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
        xmlDocument.rootElement.children.forEach { element ->
            assertFalse(element.attributes.containsKey(attributeName))
        }
    }

    @Test
    fun testQueryXPath() {


        // Construir uma estrutura XML específica
        val root = xmlDocument.rootElement
        val child1 = XMLElement("child1")
        val child2 = XMLElement("child2")
        val subchild1 = XMLElement("subchild1")
        val subchild2 = XMLElement("subchild2")
        child1.addChild(subchild1)
        child2.addChild(subchild2)
        root.addChild(child1)
        root.addChild(child2)

        // Consultar elementos com expressões XPath
        val result1 = xmlDocument.queryXPath("/root/child1")
        val result2 = xmlDocument.queryXPath("/root/child2/subchild2")

        // Verificar se os resultados estão corretos
        assertEquals(1, result1.size)
        assertEquals(child1, result1.first())

        assertEquals(1, result2.size)
        assertEquals(subchild2, result2.first())
    }

}