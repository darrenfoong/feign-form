package feign.form.feign.spring;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import feign.MethodMetadata;
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
    String name = ANNOTATION.cast(annotation).name();

    if (emptyToNull(name) == null) {
      name = "payload";
    }

    context.setParameterName(name);

    MethodMetadata data = context.getMethodMetadata();
    data.formParams().add(name);

    return true;
  }
}
