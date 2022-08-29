package calculator

import java.math.BigInteger
import kotlin.math.pow

fun main() {
    val test = SmartCalculator()
    test.run()
}

class SmartCalculator {

    val variables: MutableMap<String, BigInteger> = mutableMapOf()

    fun run() {
        while (true) {
            val input = readln().trimIndent()
            if (input.isEmpty()) continue
            if (input.first() == '/') {
                when (input) {
                    "/help" -> println("The program calculates sequence of numbers and variables\nSupported operators: +, -, /, *, ^\nenter \\exit to stop program")
                    "/exit" -> {
                        println("Bye!")
                        break
                    }
                    else -> println("Unknown command")
                }
            } else validateInput(input)
        }
    }

    class Stack {

        val stackOperator: MutableList<Any> = mutableListOf()

        fun last() = stackOperator.last()

        fun isStackEmpty() = stackOperator.isEmpty()

        fun size(stackOperator: MutableList<Any>) = stackOperator.size

        fun push(item: Any): Any {
            stackOperator.add(item)
            return item
        }

        fun pop(): Any? {
            val item = stackOperator.lastOrNull()
            if (stackOperator.isNotEmpty()) {
                stackOperator.removeAt(stackOperator.size - 1)
            }
            return item
        }

        override fun toString(): String = stackOperator.toString()
    }

    fun Char.contains(regex: Regex): Boolean {
        return this.toString().contains(regex)
    }

    fun convertToPostfix(input: String) {
        val inputString = input
        val stack = Stack()
        var postfixString = StringBuilder()
        for (ch in inputString) {
            if (ch == ' ') {
                postfixString.append(ch)
                continue
            }
            if (ch == '(') stack.push(ch)
            if (ch == ')') {
                while (true) {
                    val item = stack.pop()
                    if (item == '(') break
                    postfixString.append(item)
                }
            }
            if (ch.isLetterOrDigit()) postfixString.append(ch)
            if (ch.contains("""[\+\-\*\/\^]""".toRegex())) {
                while (true) {
                    if (stack.isStackEmpty() || stack.last() == '(') {
                        stack.push(ch)
                        break
                    } else {
                        val currentPrec = getPrec(ch)
                        val lastPrec = getPrec(stack.last() as Char)
                        if (currentPrec <= lastPrec) {
                            postfixString.append(stack.pop())
                        } else {
                            stack.push(ch)
                            break
                        }
                    }

                }
            }
        }
        while (!stack.isStackEmpty()) {
            postfixString.append(stack.pop())
            postfixString.append(" ")
        }
        val inputWithSpaces = addSpaces(postfixString)
        calculateResult(inputWithSpaces)

    }

    fun addSpaces(input: StringBuilder): String {
        val inputWithSpaces = input.toString()
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("/", " / ")
            .replace("*", " * ")
            .replace("^", " ^ ")
        return inputWithSpaces
    }

    fun calculateResult(input: String) {
        val listOfExpression = input.split(" ").filter { it != "" }
        val stack = Stack()
        for (element in listOfExpression) {
            if (element.matches("""([0-9]+)""".toRegex())) stack.push(element)
            if (element.matches("""([a-zA-Z]+)""".toRegex())) stack.push(variables.getValue(element))
            if (element.contains("""[\+\-\*\/\^]""".toRegex())) {
                val x = stack.pop().toString().toBigInteger()
                val y = stack.pop().toString().toBigInteger()
                var result = when (element) {
                    "+" -> plus(x, y)
                    "-" -> minus(x, y)
                    "*" -> pow(x, y)
                    "/" -> div(x, y)
                    "^" -> stepen(x, y)
                    else -> 0
                }
                stack.push(result)
            }

        }
        println(stack.last())
    }

    fun plus(x: BigInteger, y: BigInteger) = x + y

    fun minus(x: BigInteger, y: BigInteger) = y - x

    fun pow(x: BigInteger, y: BigInteger) = x * y

    fun div(x: BigInteger, y: BigInteger) = y / x

    fun stepen(x: BigInteger, y: BigInteger) = y.toDouble().pow(x.toDouble()).toInt()


    fun getPrec(ch: Char): Int {
        return when (ch) {
            '+', '-' -> 1
            '*', '/' -> 2
            '^' -> 3
            else -> 0
        }
    }

    fun validatePlusMinus(input: String): String {
        var newInput = input.filter { it != ' ' }
        while (newInput.contains("""[+-]{2,}""".toRegex())) {
            var newString = StringBuilder()
            for (element in newInput) {
                if (element == '+') {
                    if (newString.last() == '+') continue else newString.append('+')
                } else if (element == '-') {
                    if (newString.last() == '-') {
                        newString.deleteCharAt(newString.length - 1)
                        newString.append('+')
                    } else if (newString.last() == '+') {
                        newString.deleteCharAt(newString.length - 1)
                        newString.append('-')
                    } else newString.append('-')
                } else newString.append(element)
            }
            return addSpaces(newString)
        }
        return input
    }

    fun validateInput(userInput: String) {

        val regex = Regex("""(\.*\d+\s+\d+\.*)|([\+\-\*\/\^\(]\s*$)|([\*\/]\s*[\*\/]+)""")
        val regexIdentifier = Regex("""([a-zA-Z]\d.+=?)|[а-яА-Я]+""")
        val regexAssignment = Regex("""[a-zA-Z]+\s*=\s*.+""")
        val regexValidVariable = Regex("""(-?[a-zA-Z]+)|-?\d+""")
        val isParEqual = userInput.count { it == '(' } == userInput.count { it == ')' }
        val soloDigit = Regex("""^[\+\-]\s*\d+$""")

        if (userInput.contains(soloDigit)) {
            println(userInput)
            return
        }

        var input = userInput
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("/", " / ")
            .replace("*", " * ")
            .replace("^", " ^ ")
        if (input.contains(regex) || !isParEqual) {
            println("Invalid expression")
        } else if (input.contains(regexIdentifier)) {
            println("Invalid identifier")
        } else if (input.contains(regexAssignment)) {
            val keyValue = input.filter { it != ' ' }.split(" ", "=")
            if (keyValue.size != 2) {
                println("Invalid assignment")
            } else {
                if (keyValue[1].matches(regexValidVariable)) {
                    if (keyValue[1].matches("""[a-zA-Z]+""".toRegex())) {
                        if (keyValue[1] in variables.keys) {
                            variables.put(keyValue[0], variables.getValue(keyValue[1]))
                        } else println("Unknown variable")
                    } else {
                        variables.put(keyValue[0], keyValue[1].toBigInteger())
                    }
                } else println("Invalid assignment")
            }
        } else if (input.matches("""[a-zA-Z]+""".toRegex()) && input !in variables.keys) {
            println("Unknown variable")
        } else {
            val BLYAT = validatePlusMinus(input)
            convertToPostfix(BLYAT)
        }
    }
}