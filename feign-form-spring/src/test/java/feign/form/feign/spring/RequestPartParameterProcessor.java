package feign.form.feign.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.web.bind.annotation.RequestPart;

public class RequestPartParameterProcessor implements AnnotatedParameterProcessor {
  private static final Class<RequestPart> ANNOTATION = RequestPart.class;

  @Override
  public Class<? extends Annotation> getAnnotationType() {
    return ANNOTATION;
  }

  @Override
  public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {
    return true;
  }
}
