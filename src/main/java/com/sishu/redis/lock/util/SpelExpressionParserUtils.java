package com.sishu.redis.lock.util;

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
  private static final SpelExpressionParser PARSER = new SpelExpressionParser();
  /**
   * 用于获取方法参数定义名字.
   */
  private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

  /**
   * 解析标识。
   * 当 expressionString 以 # 开始时表名需要进行解析
   */
  private static final String DEFAULT_SPEL_FLAG_CHAR = "#";

  public static Object generateKeyByEl(String expressionString, ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String[] paramNames = NAME_DISCOVERER.getParameterNames(methodSignature.getMethod());
    if (null == paramNames
      || paramNames.length == 0
      || expressionString == null
      || expressionString.length() == 1
      || !expressionString.startsWith(DEFAULT_SPEL_FLAG_CHAR)
    ) {
      return expressionString;
    }
    Expression expression = PARSER.parseExpression(expressionString);
    EvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
      context.setVariable(paramNames[i], args[i]);
    }
    return Objects.requireNonNull(expression.getValue(context), "key not be null");
  }
}
