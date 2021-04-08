package xyz.mydev.redis.lock.util;

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


  public static Object generateKeyByEl(String expressionString, ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String[] paramNames = NAME_DISCOVERER.getParameterNames(methodSignature.getMethod());
    if (null == paramNames
      || paramNames.length == 0
      || null == expressionString
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
