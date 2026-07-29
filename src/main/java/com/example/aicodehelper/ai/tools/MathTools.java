package com.example.aicodehelper.ai.tools;

import dev.langchain4j.agent.tool.Tool;

public class MathTools {

    @Tool("Sums 2 given numbers")
    double sum(double a , double b) {
        return a + b;
    }

    @Tool("Returns a square root of a given number")
    double squareRoot(double x) {
        return Math.sqrt(x);
    }
}
