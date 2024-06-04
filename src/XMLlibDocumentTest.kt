import XMLDocument.Companion.mapToXML
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
     * Atualiza o valor de um atributo deste elemento XML.
     *
     * Se o atributo não existir, uma exceção `IllegalArgumentException` será lançada.
     *
     * @param name O nome do atributo a ser atualizado. O nome será sanitizado para remover caracteres inválidos.
     * @param value O novo valor do atributo. O valor será sanitizado para remover caracteres inválidos.
     * @throws IllegalArgumentException Se o atributo não existir neste elemento.
     */
    @Test
    fun testUpdateAttribute() {
        val doc = XMLDocument()
        val root = doc.rootElement
        root.addAttribute("version", "1.0")
        assert(root.attributes["version"] == "1.0") { "Erro: o atributo 'version' deveria ser '1.0'" }
        root.updateAttribute("version", "2.0")
        assert(root.attributes["version"] == "2.0") { "Erro: o atributo 'version' deveria ser '2.0'" }
        try {
            root.updateAttribute("encoding", "UTF-8")
            assert(false) { "Erro: a exceção deveria ter sido lançada para um atributo inexistente" }
        } catch (e: IllegalArgumentException) {
            assert(e.message == "Atributo 'encoding' não existe") { "Erro: a mensagem da exceção não está correta" }
        }
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
    /*@Test
    fun testTextNode() {
        val textNode = XMLDocument.TextNode("Hello, World!")
        textNode.addAttribute("lang", "en")

        assertEquals("Hello, World!", textNode.texto)
        assertEquals("en", textNode.attributes["lang"])

        val expectedOutput = """<text lang="en">Hello, World!</text>
    """.trimIndent()
        assertEquals(expectedOutput, textNode.prettyPrint())
    }*/

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
        // Criando elementos
        val root = XMLDocument.XMLElement("root")
        root.addAttribute("id", "1")

        val child1 = XMLDocument.XMLElement("child")
        child1.addAttribute("name", "Child1")
        root.addChild(child1)

        val subChild = XMLDocument.XMLElement("subchild")
        subChild.setTexto("Some text")
        child1.addChild(subChild)

        // Preparando o resultado esperado
        val expected = """
            <root id="1">
             <child name="Child1">
              <subchild> Some text </subchild>
             </child>
            </root>
            
            """.trimIndent()

        // Executando o prettyPrint
        val result = root.prettyPrint()

        // Comparando o resultado com o esperado
        assertEquals(expected, result)
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

        // Criar um elemento raiz
        val element = XMLElement("componenteavaliacao")
        element.addAttribute("attr", "value")
        val child1 = XMLElement("child1")
        val child2 = XMLElement("child2")
        element.addChild(child1)
        element.addChild(child2)


        // Configuração do adaptador com renomeação personalizada
        val config = XMLDocument.AdapterConfig(
            renameComponents = true,
            removeDuplicates = true,
            addDefaultAttributes = null,
            sortChildrenByName = false,
            tagRenames = mapOf("componenteavaliacao" to "componente") // Renomear "componenteavaliacao" para "componente"
        )
        val adapter = XMLDocument.FUCAdapter(config)

        // Aplicar o adaptador ao elemento
        adapter.adapt(element)

        // Teste se o nome do elemento foi corretamente atualizado para 'componente'
        assertEquals("componente", element.name)

        // Teste se o número de elementos filhos é correto
        assertEquals(2, element.children.size)

        // Teste se o valor do atributo 'attr' é mantido
        assertEquals("value", element.attributes["attr"])
    }

    @Test
    fun testXmlAttributeAnnotationAddsAttribute() {
        val obj = object {
            @XMLDocument.XmlAttribute
            val id = "12345"
        }

        val element = mapToXML(obj)
        assertTrue(element.attributes.containsKey("id"))
        assertEquals("12345", element.attributes["id"])
    }

    /**
     * Testa a anotação @XmlName para renomear o nome do elemento XML.
     *
     * Verifica se a anotação @XmlName renomeia corretamente o nome do elemento
     * gerado a partir de um objeto ao mapear para XML.
     */


/**
 * Testa a anotação @XmlName para renomear o nome do elemento XML.
 */
    @Test
    fun testXmlNameAnnotationChangesElementName() {
        val obj = object {
            @XMLDocument.XmlName("CustomName")
            val testProperty = "TestValue"
        }

        // Mapeando para XML
        val element = mapToXML(obj)


        // Vamos verificar a existência da anotação em uma das propriedades.
        assertTrue("O elemento deve ter pelo menos um filho com nome 'CustomName'",
            element.children.any { it.name == "CustomName" })
    }

    @Test
    fun testXmlElementOrderDefinesChildOrder() {
        @XMLDocument.XmlElementOrder("ects", "nome", "codigo")
        class TestClass {
            val codigo = "001"
            val nome = "Curso"
            val ects = "6.0"
        }

        val obj = TestClass()
        // Asegura-se que o mapToXML está no contexto correto
        val element = XMLDocument.mapToXML(obj)
        val expectedOrder = listOf("ects", "nome", "codigo")
        val actualOrder = element.children.map { it.name }

        assertEquals(expectedOrder, actualOrder)
    }

    /**
     * Testa a anotação @XmlExclude para excluir um atributo do elemento XML.
     *
     * Verifica se a anotação @XmlExclude exclui corretamente um atributo
     * específico do elemento gerado a partir de um objeto ao mapear para XML.
     */
    @Test
    fun testXmlExcludeAnnotation() {
        // Definindo uma classe local para o teste com a anotação @XmlExclude
        class TestClass {
            var included = "Should be in XML"

            @XMLDocument.XmlExclude
            var excluded = "Should not be in XML"
        }

        // Criando um objeto da classe de teste
        val obj = TestClass()

        // Convertendo o objeto para XML
        val element = XMLDocument.mapToXML(obj)

        // Assegura que o texto que não deveria ser excluído está presente
        assertTrue("Included text should be present in the XML",
            element.children.any { it.getTexto() == obj.included })

        // Assegura que o texto que deveria ser excluído não está presente
        assertFalse("Excluded text should not be present in the XML",
            element.children.any { it.getTexto() == obj.excluded })
    }
}