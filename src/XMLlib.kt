//documentação
//texto pode ter atributos
//blindar ou ter texto ou ter tag


// Classe para representar o documento XML
class XMLDocument {
    val rootElement = XMLElement("root")
    fun prettyPrint(): String {
        return rootElement.prettyPrint()
    }

    // Classe para representar um elemento XML
    class XMLElement(var name: String) {
        val attributes = mutableMapOf<String, String>()
        val children = mutableListOf<XMLElement>()
        val texto: String? = null


        fun addChild(xmlelement: XMLElement) {
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

        private fun sanitize(input: String): String { //remover caracteres especiais
            return input.replace(Regex("[<>&\"']"), "")
        }

        fun removeAttribute(name: String) {
            attributes.remove(name)
        }

        fun prettyPrint(indentation: String = ""): String {
            val sb = StringBuilder()
            sb.append("$indentation<$name")
            for ((attr, value) in attributes) {
                sb.append(" $attr=\"$value\"")
            }
            if (children.isEmpty() && texto.isNullOrEmpty()) {
                sb.append("/>")
            } else {
                sb.append(">\n")
                for (child in children) {
                    sb.append(child.prettyPrint("$indentation  "))
                }
                texto?.let { sb.append("$indentation  $it\n") }
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
                if (element.children.any { it.name == entityName }) {
                    element.children.removeIf { it.name == entityName }
                }
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
        val parts = expression.split('/')
        var elements = listOf(rootElement)
        for (part in parts) {
            println("Processing part: $part")
            println("Current elements: $elements")
            elements = elements.flatMap { it.children.filter { child -> child.name == part } }
            println("Filtered elements: $elements")
        }
        return elements
    }

    companion object {
        fun createXmlHeader(): String {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        }
    }
}
