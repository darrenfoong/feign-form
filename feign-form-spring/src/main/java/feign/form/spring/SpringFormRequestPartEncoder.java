package feign.form.spring;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.val;

public class SpringFormRequestPartEncoder extends SpringFormEncoder {
  public SpringFormRequestPartEncoder () {
    this(new Encoder.Default());
  }

  public SpringFormRequestPartEncoder (Encoder delegate) {
    super(delegate);
  }

  @Override
  public void encode (Object object, Type bodyType, RequestTemplate template) throws EncodeException {
    val contentTypeHeaders = template.headers().get("Content-Type");
    String contentType = contentTypeHeaders != null ? new ArrayList<String>(contentTypeHeaders).get(0) : "";
    String typeName = bodyType.getTypeName();

    if (contentType.equals("multipart/form-data") && typeName.startsWith("java.util.Map")) {
      val data = new HashMap<String, Object>();
      val map = (Map<String, Object>) object;

      for (val entry : map.entrySet()) {
        if (entry.getValue() instanceof MultipartFile[]) {
          val files = (MultipartFile[]) entry.getValue();

          for (val file : files) {
            data.put(file.getName(), file);
          }
        } else if (isMultipartFileCollection(entry.getValue())) {
          val iterable = (Iterable<?>) entry.getValue();

          for (val item : iterable) {
            val file = (MultipartFile) item;
            data.put(file.getName(), file);
          }
        } else if (entry.getValue() instanceof MultipartFile) {
          val file = (MultipartFile) entry.getValue();
          data.put(file.getName(), file);
        } else {
          // TODO Use proper serializer
          data.put(entry.getKey(), entry.getValue().toString());
        }
      }

      super.encode(data, MAP_STRING_WILDCARD, template);
    } else {
      super.encode(object, bodyType, template);
    }
  }

  private boolean isMultipartFileCollection (Object object) {
    if (!(object instanceof Iterable)) {
      return false;
    }
    val iterable = (Iterable<?>) object;
    val iterator = iterable.iterator();
    return iterator.hasNext() && iterator.next() instanceof MultipartFile;
  }
}
