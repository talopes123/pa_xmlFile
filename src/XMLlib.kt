import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

//documentação

class XMLDocument {
    val rootElement = XMLElement("root")

    fun prettyPrint(): String {
        return rootElement.prettyPrint()
    }

    // Classe para representar um elemento XML
    class XMLElement(var name: String) {
        val attributes = mutableMapOf<String, String>()
        val children = mutableListOf<XMLElement>()
        private var textNode: TextNode? = null

        fun addChild(xmlelement: XMLElement) {
            if (textNode != null) {
                throw IllegalArgumentException("Não é possível adicionar um elemento filho quando o texto está presente.")
            }
            if (children.contains(xmlelement)) {
                throw IllegalArgumentException("Elemento já existe")
            } else {
                children.add(xmlelement)
            }
        }

        fun removeChild(child: XMLElement) {
            children.remove(child)
        }

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

        fun removeAttribute(name: String) {
            attributes.remove(name)
        }

        fun setTexto(texto: String) {
            if (children.isNotEmpty()) {
                throw IllegalArgumentException("Não é possível adicionar texto quando elementos filhos estão presentes.")
            }
            textNode = TextNode(texto)
        }

        fun getTexto(): String? {
            return textNode?.texto
        }

        fun addTextNodeAttribute(name: String, value: String) {
            textNode?.addAttribute(name, value) ?: throw IllegalArgumentException("Texto não definido para este elemento.")
        }

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

        fun accept(visitor: Visitor) {
            visitor.visit(this)
            for (child in children) {
                child.accept(visitor)
            }
        }

        fun getChildrenList(): List<XMLElement> {
            return children.toList() // Retorna uma cópia da lista para evitar modificações externas
        }
    }

    // Classe para representar texto com atributos
    class TextNode(var texto: String) {
        val attributes = mutableMapOf<String, String>()

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

        fun removeAttribute(name: String) {
            attributes.remove(name)
        }

        private fun sanitize(input: String): String { // remover caracteres especiais
            return input.replace(Regex("[<>&\"']"), "")
        }

        fun prettyPrint(indentation: String = ""): String {
            val sb = StringBuilder()
            sb.append("$indentation<text")
            for ((attr, value) in attributes) {
                sb.append(" $attr=\"$value\"")
            }
            sb.append(">$texto</text>\n")
            return sb.toString()
        }
    }

    // Interface para o Visitor
    interface Visitor {
        fun visit(element: XMLElement)
    }

    // Adiciona atributos globalmente ao documento
    fun addGlobalAttribute(attributeName: String, value: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                element.addAttribute(attributeName, value)
            }
        })
    }

    // Renomeia entidades globalmente no documento
    fun renameGlobalEntity(oldName: String, newName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                if (element.name == oldName) {
                    element.name = newName
                }
            }
        })
    }

    // Renomeia atributos globalmente no documento
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

    // Remove entidades globalmente no documento
    fun removeGlobalEntity(entityName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                val childrenToRemove = element.getChildrenList().filter { it.name == entityName }
                childrenToRemove.forEach { element.removeChild(it) }
            }
        })
    }

    // Remove atributos globalmente no documento
    fun removeGlobalAttribute(entityName: String, attributeName: String) {
        rootElement.accept(object : Visitor {
            override fun visit(element: XMLElement) {
                if (element.name == entityName) {
                    element.removeAttribute(attributeName)
                }
            }
        })
    }

    // Micro-XPath
    fun queryXPath(expression: String): List<XMLElement> {
        // Divide a expressão em partes usando '/' como delimitador
        val parts = expression.split('/').filter { it.isNotBlank() }

        // Começa com o elemento raiz
        var elements = listOf(rootElement)

        // Percorre cada parte da expressão XPath
        for (part in parts) {
            // Imprime o estado atual dos elementos
            println("Part: $part")
            println("Current elements: $elements")

            // Atualiza a lista de elementos com os filhos que correspondem ao nome da parte atual
            elements = elements.flatMap { element ->
                element.getChildrenList().filter { it.name == part }
            }

            // Imprime os resultados da filtragem
            println("Filtered elements: $elements")
        }

        return elements
    }
    @Target(AnnotationTarget.PROPERTY)
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


    interface Transformer {
        fun transform(value: Any): String
    }

    interface XMLAdapter {
        fun adapt(element: XMLElement)
    }

    class AddPercentage : Transformer {
        override fun transform(value: Any): String = "$value%"
    }

    class FUCAdapter : XMLAdapter {
        override fun adapt(element: XMLElement) {
            // Reordenar elementos filhos por algum critério específico, por exemplo, por nome
            //element.children.sortBy { child -> child.name }

            /*// Adicionar um sufixo aos valores de todos os atributos
            element.attributes.forEach { (key, value) ->
                element.attributes[key] = "$value%"
            }*/

            /*// Injeção de um atributo padrão
            val defaultAttributeName = "id"
            val defaultAttributeValue = "20"
            element.attributes[defaultAttributeName] = defaultAttributeValue*/

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

    companion object {
        fun createXmlHeader(): String {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        }
        fun mapToXML(obj: Any): XMLElement {
            val kClass = obj::class
            val xmlName = kClass.simpleName!!.lowercase(Locale.getDefault()) // Converte tod o nome da classe para minuscula
            val element = XMLElement(xmlName)

            val orderAnnotation = kClass.findAnnotation<XmlElementOrder>()
            val orderedProps = orderAnnotation?.order?.toList() ?: emptyList()

            // Percorre as propriedades na ordem da declaração
            kClass.memberProperties
                .sortedBy { prop -> orderedProps.indexOf(prop.name).takeIf { it != -1 } ?: Int.MAX_VALUE }
                .forEach { prop ->
                    prop.isAccessible = true  // Acede a propriedade, mesmo que seja privada
                    var value = prop.call(obj)


                    if (prop.findAnnotation<XmlExclude>() != null) {
                        return@forEach  // Ignora as propriedades marcadas com @XmlExclude
                    }

                    if (prop.findAnnotation<XmlString>() != null) {
                        val transformerClass = prop.findAnnotation<XmlString>()!!.transformer
                        val transformer = transformerClass.objectInstance ?: transformerClass.createInstance()
                        value = transformer.transform(value ?: "")
                    }

                    // Verifica se há customização do nome do elemento ou atributo
                    val customName = prop.findAnnotation<XmlName>()?.value ?: prop.name

                    if (prop.findAnnotation<XmlAttribute>() != null && value != null) {
                        // Adiciona como atributo
                        element.attributes[customName] = value.toString()
                    } else {
                        // Determina como adicionar o elemento filho ou texto
                        when (value) {
                            is List<*> -> value.filterNotNull().forEach { childObj ->
                                element.children.add(mapToXML(childObj))
                            }
                            is String, is Number, is Boolean -> {
                                val childElement = XMLElement(customName)
                                childElement.setTexto(value.toString())
                                element.children.add(childElement)
                            }
                            else -> if (value != null) element.children.add(mapToXML(value))
                        }
                    }
                }

            return element
        }
    }
}



