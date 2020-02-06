package com.sishu.redis.lock.util;

import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Objects;

/**
 * @author ZSP
 */
public class SpelExpressionParserUtils {
  /**
   * 用于SpEL表达式解析.
   */
  private static SpelExpressionParser parser = new SpelExpressionParser();
  /**
   * 用于获取方法参数定义名字.
   */
  private static DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

  public static Object generateKeyByEl(String expressionString, ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
    if (ObjectUtils.isEmpty(paramNames)) {
      return expressionString;
    }
    Expression expression = parser.parseExpression(expressionString);
    EvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
      context.setVariable(paramNames[i], args[i]);
    }
    return Objects.requireNonNull(expression.getValue(context));
  }
}
