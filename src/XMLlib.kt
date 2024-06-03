import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Classe principal para manipulação de documentos XML.
 * Permite a construção, manipulação e serialização de documentos XML.
 */
class XMLDocument {
    /**
     * Elemento raiz do documento XML.
     */
    val rootElement = XMLElement("root")

    /**
     * Retorna uma representação formatada do documento XML.
     * @return Uma string contendo a representação formatada do documento XML.
     */
    fun prettyPrint(): String {
        return rootElement.prettyPrint()
    }

    /**
     * Escreve o conteúdo do documento XML em um arquivo.
     * @param filename O nome do arquivo onde o conteúdo XML será escrito.
     */
    fun writeToFile(filename: String) {
        val xmlHeader = createXmlHeader()
        val xmlContent = prettyPrint().trim()
        val xmlString = "$xmlHeader\n$xmlContent"
        File(filename).writeText(xmlString)
    }

    /**
     * Classe que representa um elemento XML.
     * Permite a adição de atributos, elementos filhos e texto.
     */
    /**
     * Classe que representa um elemento XML.
     * Permite a adição de atributos, elementos filhos e texto.
     * @property name O nome do elemento XML.
     * @property parent O elemento pai deste elemento XML, se existir. Null se for o elemento raiz.
     */
    class XMLElement(var name: String, var parent: XMLElement? = null) {
        /**
         * Mapa de atributos deste elemento XML, onde a chave é o nome do atributo e o valor é o seu valor.
         */
        val attributes = mutableMapOf<String, String>()

        /**
         * Lista de elementos filhos deste elemento XML.
         */
        val children = mutableListOf<XMLElement>()

        /**
         * Nó de texto associado a este elemento XML, se existir.
         */
        private var textNode: TextNode? = null

        /**
         * Adiciona um elemento filho a este elemento XML.
         * @param xmlelement O elemento filho a ser adicionado.
         * @throws IllegalArgumentException Se já existir um nó de texto associado a este elemento.
         */

        /**
        * Adiciona um elemento filho a este elemento XML.
        * @param xmlelement O elemento filho a ser adicionado.
        * @throws IllegalArgumentException Se um texto já estiver presente neste elemento.
        * @throws IllegalArgumentException Se o elemento filho já existir na lista de elementos filhos.
        */
        fun addChild(xmlelement: XMLElement) {
            if (textNode != null) {
                throw IllegalArgumentException("Não é possível adicionar um elemento filho quando o texto está presente.")
            }
            if (children.contains(xmlelement)) {
                throw IllegalArgumentException("Elemento já existe")
            } else {
                xmlelement.parent = this
                children.add(xmlelement)
            }
        }


        /**
         * Remove um elemento filho deste elemento XML.
         * @param child O elemento filho a ser removido.
         */
        fun removeChild(child: XMLElement) {
            children.remove(child)
        }

        /**
         * Adiciona um atributo a este elemento XML.
         * @param name O nome do atributo a ser adicionado.
         * @param value O valor do atributo a ser adicionado.
         * @throws IllegalArgumentException Se o nome do atributo estiver vazio ou contiver caracteres inválidos.
         * @throws IllegalArgumentException Se o atributo já existir neste elemento.
         */
        fun addAttribute(name: String, value: String) {
            val sanitizedKey = sanitize(name)
            val sanitizedValue = sanitize(value)

            if (sanitizedKey.isEmpty()) {
                throw IllegalArgumentException("O nome do atributo não pode estar vazio ou conter caracteres inválidos")
            }
            if (attributes.containsKey(sanitizedKey)) {
                throw IllegalArgumentException("Atributo já existe")
            } else {
                attributes[sanitizedKey] = sanitizedValue
            }
        }

        private fun sanitize(input: String): String { // remover caracteres especiais
            return input.replace(Regex("[<>&\"']"), "")
        }

        /**
         * Remove um atributo deste elemento XML.
         * @param name O nome do atributo a ser removido.
         */
        fun removeAttribute(name: String) {
            attributes.remove(name)
        }

        /**
         * Atualiza o valor de um atributo deste elemento XML.
         * @param name O nome do atributo a ser atualizado.
         * @param value O novo valor do atributo.
         * @throws IllegalArgumentException Se o atributo não existir neste elemento.
         */
        fun updateAttribute(name: String, value: String) {
            val sanitizedKey = sanitize(name)
            val sanitizedValue = sanitize(value)

            if (attributes.containsKey(sanitizedKey)) {
                attributes[sanitizedKey] = sanitizedValue
            } else {
                throw IllegalArgumentException("Atributo '$sanitizedKey' não existe")
            }
        }

        /**
         * Define o texto associado a este elemento XML.
         * @param texto O texto a ser definido.
         * @throws IllegalArgumentException Se elementos filhos já estiverem presentes neste elemento.
         */
        fun setTexto(texto: String) {
            if (children.isNotEmpty()) {
                throw IllegalArgumentException("Não é possível adicionar texto quando elementos filhos estão presentes.")
            }
            textNode = TextNode(texto)
        }

        /**
         * Obtém o texto associado a este elemento XML, se existir.
         * @return O texto associado a este elemento XML, ou null se não houver texto.
         */
        fun getTexto(): String? {
            return textNode?.texto
        }

        /**
        * Adiciona um atributo ao nó de texto associado a este elemento XML.
        * @param name O nome do atributo a ser adicionado.
        * @param value O valor do atributo a ser adicionado.
        * @throws IllegalArgumentException Se não houver texto associado a este elemento XML.
        */
        fun addTextNodeAttribute(name: String, value: String) {
            textNode?.addAttribute(name, value) ?: throw IllegalArgumentException("Texto não definido para este elemento.")
        }

        /**
        * Gera uma representação em string formatada deste elemento XML.
        * @param indentation A string de recuo usada para formatar a saída.
        * @return A representação em string formatada deste elemento XML.
        */
        fun prettyPrint(indentation: String = ""): String {
            val sb = StringBuilder()
            sb.append("$indentation<$name")
            for ((attr, value) in attributes) {
                sb.append(" $attr=\"$value\"")
            }
            if (children.isEmpty() && textNode == null) {
                sb.append("/>")
            } else {
                sb.append(">\n")
                textNode?.let { sb.append(it.prettyPrint("$indentation  ")) }
                for (child in children) {
                    sb.append(child.prettyPrint("$indentation  "))
                }
                sb.append("$indentation</$name>")
            }
            sb.append("\n")
            return sb.toString()
        }

        /**
         * Aceita um visitante e invoca o método visit no visitante para este elemento XML e todos os seus elementos filhos.
         * @param visitor O visitante a ser aceito.
         */
        fun accept(visitor: Visitor) {
            visitor.visit(this)
            for (child in children) {
                child.accept(visitor)
            }
        }

        /**
         * Obtém uma cópia da lista de elementos filhos deste elemento XML.
         * @return Uma lista contendo os elementos filhos deste elemento XML.
         */
        fun getChildrenList(): List<XMLElement> {
            return children.toList() // Retorna uma cópia da lista para evitar modificações externas
        }
    }

    /**
     * Classe que representa um nó de texto com atributos.
     * Permite a adição de atributos ao texto.
     * @property texto O conteúdo do nó de texto.
     */
    class TextNode(var texto: String) {
        /**
         * Mapa de atributos deste nó de texto, onde a chave é o nome do atributo e o valor é o seu valor.
         */
        val attributes = mutableMapOf<String, String>()

        /**
         * Adiciona um atributo .
         * @param name O nome do atributo a ser adicionado.
         * @param value O valor do atributo a ser adicionado.
         */
        fun addAttribute(name: String, value: String) {
            val sanitizedKey = sanitize(name)
            val sanitizedValue = sanitize(value)

            if (sanitizedKey.isEmpty()) {
                throw IllegalArgumentException("O nome do atributo não pode estar vazio ou conter caracteres inválidos")
            }
            if (attributes.containsKey(sanitizedKey)) {
                throw IllegalArgumentException("Atributo já existe")
            } else {
                attributes[sanitizedKey] = sanitizedValue
            }
        }

        /**
         * Remove um atributo deste nó de texto.
         * @param name O nome do atributo a ser removido.
         */
        fun removeAttribute(name: String) {
            attributes.remove(name)
        }

        /**
         * Remove caracteres especiais de uma string.
         * @param input A string a ser sanitizada.
         * @return A string sanitizada, sem caracteres especiais.
         */
        private fun sanitize(input: String): String { // remover caracteres especiais
            return input.replace(Regex("[^A-Za-z0-9_\\-]"), "")
        }

        /**
         * Gera uma representação em string formatada deste nó de texto.
         * @param indentation A string de recuo usada para formatar a saída.
         * @return A representação em string formatada deste nó de texto.
         */
        fun prettyPrint(indentation: String = ""): String {
            val sb = StringBuilder()
            sb.append("$indentation<text")
            for ((attr, value) in attributes) {
                sb.append(" $attr=\"$value\"")
            }
            sb.append(">$texto</text>")
            return sb.toString()
        }
    }

    /**
     * Interface que define o método de visita para um elemento XML.
     */
    interface Visitor {
        fun visit(element: XMLElement)
    }

    /**
     * Adiciona um atributo globalmente a todos os elementos do documento XML.
     * @param attributeName O nome do atributo a ser adicionado.
     * @param value O valor do atributo a ser adicionado.
     */
    fun addGlobalAttribute(attributeName: String, value: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                element.addAttribute(attributeName, value)
            }
        })
    }

    /**
     * Renomeia uma entidade globalmente em todos os elementos do documento XML.
     * @param oldName O nome antigo da entidade a ser renomeada.
     * @param newName O novo nome da entidade.
     */
    fun renameGlobalEntity(oldName: String, newName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                if (element.name == oldName) {
                    element.name = newName
                }
            }
        })
    }

    /**
     * Renomeia um atributo globalmente em todos os elementos do documento XML.
     * @param entityName O nome da entidade cujo atributo será renomeado.
     * @param oldAttributeName O nome antigo do atributo a ser renomeado.
     * @param newAttributeName O novo nome do atributo.
     */
    fun renameGlobalAttribute(entityName: String, oldAttributeName: String, newAttributeName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                if (element.name == entityName) {
                    val value = element.attributes.remove(oldAttributeName)
                    if (value != null) {
                        element.addAttribute(newAttributeName, value)
                    }
                }
            }
        })
    }

    /**
     * Remove uma entidade globalmente de todos os elementos do documento XML.
     * @param entityName O nome da entidade a ser removida.
     */
    fun removeGlobalEntity(entityName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                val childrenToRemove = element.getChildrenList().filter { it.name == entityName }
                childrenToRemove.forEach { element.removeChild(it) }
            }
        })
    }

    /**
     * Remove um atributo globalmente de todos os elementos do documento XML.
     * @param entityName O nome da entidade cujo atributo será removido.
     * @param attributeName O nome do atributo a ser removido.
     */
    fun removeGlobalAttribute(entityName: String, attributeName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                if (element.name == entityName) {
                    element.removeAttribute(attributeName)
                }
            }
        })
    }

    /**
     * Executa uma consulta XPath no documento XML e retorna uma lista de elementos correspondentes à expressão XPath.
     * @param expression A expressão XPath a ser avaliada.
     * @return Uma lista de elementos correspondentes à expressão XPath.
     */
    fun queryXPath(expression: String): List<XMLElement> {
        // Divide a expressão em partes usando '/' como delimitador
        val parts = expression.split('/').filter { it.isNotBlank() }

        // Começa com o elemento raiz
        var elements = listOf(rootElement)

        // Percorre cada parte da expressão XPath
        for (part in parts) {

            // Lista temporária para armazenar os elementos encontrados
            val filteredElements = mutableListOf<XMLElement>()

            // Percorre os elementos atuais em busca dos elementos com o nome correspondente à parte da expressão XPath
            for (element in elements) {
                findElementsByName(element, part, filteredElements)
            }

            // Atualiza a lista de elementos com os elementos encontrados
            elements = filteredElements

            // Imprime os resultados da filtragem
            println("Filtered elements: $elements")
        }

        return elements
    }

    private fun findElementsByName(element: XMLElement, name: String, result: MutableList<XMLElement>) {
        // Verifica se o elemento atual possui o nome correspondente à parte da expressão XPath
        if (element.name == name) {
            result.add(element)
        }

        // Busca recursivamente nos filhos do elemento atual
        for (child in element.children) {
            findElementsByName(child, name, result)
        }
    }
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlName(val value: String)

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlAttribute

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlExclude

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlString(val transformer: KClass<out Transformer>)

    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlAdapter(val adapter: KClass<out XMLAdapter>)


    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlElementText

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class XmlElementOrder(vararg val order: String)

    /**
     * Interface que define um transformador para valores.
     * Os transformadores podem ser usados para modificar os valores antes de serem escritos como texto em elementos XML.
     */
    interface Transformer {
        /**
         * Método para transformar um valor em uma representação de string.
         * @param value O valor a ser transformado.
         * @return A representação de string transformada do valor.
         */
        fun transform(value: Any): String
    }

    /**
     * Interface que define um adaptador XML.
     * Os adaptadores XML podem ser usados para aplicar modificações específicas nos elementos XML.
     */
    interface XMLAdapter {
        /**
         * Método para adaptar um elemento XML.
         * @param element O elemento XML a ser adaptado.
         */
        fun adapt(element: XMLDocument.XMLElement)
    }

    /**
     * Classe que implementa um transformador para adicionar um sinal de porcentagem a um valor.
     * Por exemplo, pode ser usado para transformar um valor inteiro em uma string com o símbolo de porcentagem.
     */
    class AddPercentage : Transformer {
        /**
         * Método para transformar um valor adicionando um sinal de porcentagem.
         * @param value O valor a ser transformado.
         * @return A representação de string do valor com um sinal de porcentagem adicionado.
         */
        override fun transform(value: Any): String = "$value%"
    }

    /**
     * Classe que implementa um adaptador XML específico para elementos FUC.
     * Realiza várias modificações nos elementos FUC, como reordenação, adição de atributos, remoção de duplicados, etc.
     */
    class FUCAdapter : XMLAdapter {
        /**
         * Método para adaptar um elemento FUC.
         * Aplica várias modificações no elemento FUC, como reordenação de elementos, adição de atributos, remoção de duplicados, etc.
         * @param element O elemento FUC a ser adaptado.
         */
        override fun adapt(element: XMLElement) {
            // Reordenar elementos filhos por algum critério específico, por exemplo, por nome
            element.children.sortBy { child -> child.name }

            // Adicionar um sufixo aos valores de todos os atributos
            element.attributes.forEach { (key, value) ->
                element.attributes[key] = "$value%"
            }

            // Injeção de um atributo padrão
            val defaultAttributeName = "id"
            val defaultAttributeValue = "20"
            element.attributes[defaultAttributeName] = defaultAttributeValue

            // Remoção de duplicados
            val uniqueChildren = element.getChildrenList().distinctBy { child -> child.name + child.attributes.values.joinToString() }
            element.children.clear()
            element.children.addAll(uniqueChildren)

            // Modificar nomes de tags baseado em condições específicas
            if (element.name == "componenteavaliacao") {
                element.name = "componente"
            }

            // Iterar sobre elementos filhos para aplicar a adaptação de forma recursiva
            element.getChildrenList().forEach { child ->
                adapt(child)  // Aplicar a adaptação nos filhos
            }
        }
    }

    /**
     * Companion Object que fornece métodos utilitários para manipulação XML.
     */
    companion object {

        /**
         * Cria o cabeçalho XML.
         * @return O cabeçalho XML.
         */
        fun createXmlHeader(): String {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        }

        /**
         * Mapeia um objeto para um elemento XML.
         * @param obj O objeto a ser mapeado para XML.
         * @return O elemento XML correspondente ao objeto.
         */
        fun mapToXML(obj: Any): XMLElement {
            val kClass = obj::class
            val xmlName = kClass.findAnnotation<XmlName>()?.value ?: kClass.simpleName!!.lowercase(Locale.getDefault())
            val element = XMLElement(xmlName)

            // Percorre as propriedades da classe
            kClass.memberProperties.forEach { prop ->
                prop.isAccessible = true  // Permite o acesso à propriedade, mesmo que seja privada
                val value = prop.call(obj)

                if (value != null && prop.findAnnotation<XmlExclude>() == null) {
                    // Verifica se a propriedade não possui a anotação @XmlExclude
                    println("Processing property: ${prop.name}, value: $value")

                    val customName = prop.findAnnotation<XmlName>()?.value ?: prop.name

                    if (prop.findAnnotation<XmlAttribute>() != null) {
                        // Adiciona como atributo
                        println("Adding property $customName as attribute with value: $value")
                        element.attributes[customName] = value.toString()
                    } else {
                        // Adiciona como elemento filho ou texto
                        val childElement = XMLElement(customName)
                        if (prop.findAnnotation<XmlString>() != null) {
                            // Verifica se a propriedade possui a anotação @XmlString
                            val transformer = prop.findAnnotation<XmlString>()?.transformer?.createInstance()
                            val transformedValue = transformer?.transform(value) ?: value.toString()
                            childElement.setTexto(transformedValue)
                            println("Adding property $customName as text element with transformed value: $transformedValue")
                        } else {
                            childElement.setTexto(value.toString())
                            println("Adding property $customName as text element with value: $value")
                        }
                        element.children.add(childElement)
                    }
                } else {
                    println("Ignoring property marked with @XmlExclude: ${prop.name}")
                }
            }

            return element
        }
    }
}



