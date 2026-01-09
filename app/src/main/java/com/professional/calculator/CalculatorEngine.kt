package com.professional.calculator

import java.util.Stack
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A self-contained math engine to parse and evaluate mathematical expressions.
 * Handles: +, -, *, /, ^ (power), √ (sqrt), % (percent), ()
 * Respects order of operations (PEMDAS).
 */
object CalculatorEngine {

    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        val postfix = shuntingYard(tokens)
        return evaluatePostfix(postfix)
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var numberBuffer = StringBuilder()

        var i = 0
        while (i < expression.length) {
            val c = expression[i]

            if (c.isDigit() || c == '.') {
                numberBuffer.append(c)
            } else {
                if (numberBuffer.isNotEmpty()) {
                    tokens.add(numberBuffer.toString())
                    numberBuffer = StringBuilder()
                }

                if (c == '√') {
                    tokens.add("sqrt")
                } else if (c == '-' && (tokens.isEmpty() || isOperator(tokens.last()) || tokens.last() == "(")) {
                    // Handle unary minus
                    numberBuffer.append('-')
                } else if (!c.isWhitespace()) {
                    tokens.add(c.toString())
                }
            }
            i++
        }
        if (numberBuffer.isNotEmpty()) {
            tokens.add(numberBuffer.toString())
        }
        return tokens
    }

    private fun isOperator(token: String): Boolean {
        return token in listOf("+", "-", "*", "/", "^", "%", "sqrt")
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/", "%" -> 2
            "^", "sqrt" -> 3
            else -> 0
        }
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val outputQueue = mutableListOf<String>()
        val operatorStack = Stack<String>()

        for (token in tokens) {
            if (token.toDoubleOrNull() != null) {
                outputQueue.add(token)
            } else if (token == "sqrt") {
                operatorStack.push(token)
            } else if (token == "(") {
                operatorStack.push(token)
            } else if (token == ")") {
                while (operatorStack.isNotEmpty() && operatorStack.peek() != "(") {
                    outputQueue.add(operatorStack.pop())
                }
                if (operatorStack.isNotEmpty()) operatorStack.pop() // Remove '('
            } else if (isOperator(token)) {
                while (operatorStack.isNotEmpty() && isOperator(operatorStack.peek()) &&
                    precedence(operatorStack.peek()) >= precedence(token)
                ) {
                    outputQueue.add(operatorStack.pop())
                }
                operatorStack.push(token)
            }
        }

        while (operatorStack.isNotEmpty()) {
            outputQueue.add(operatorStack.pop())
        }

        return outputQueue
    }

    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            if (token.toDoubleOrNull() != null) {
                stack.push(token.toDouble())
            } else {
                if (token == "sqrt") {
                    val a = stack.pop()
                    stack.push(sqrt(a))
                } else {
                    val b = stack.pop()
                    val a = if (stack.isNotEmpty()) stack.pop() else 0.0

                    when (token) {
                        "+" -> stack.push(a + b)
                        "-" -> stack.push(a - b)
                        "*" -> stack.push(a * b)
                        "/" -> stack.push(a / b)
                        "^" -> stack.push(a.pow(b))
                        "%" -> stack.push(a * (b / 100.0)) // Logic: 50 + 10% -> not standard, usually 50 * 0.1
                    }
                }
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else 0.0
    }
}