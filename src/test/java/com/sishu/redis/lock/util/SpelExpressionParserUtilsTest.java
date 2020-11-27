package com.sishu.redis.lock.util;


import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author ZSP
 */
public class SpelExpressionParserUtilsTest {

  private static final SpelExpressionParser PARSER = new SpelExpressionParser();


  public static void main(String[] args) {
    Expression exp = PARSER.parseExpression("'Hello spel'");
    String message = (String) exp.getValue();
    System.out.println(message);


    Expression expression = PARSER.parseExpression("Hello spel2");
    System.out.println(expression.getValue());
  }

}