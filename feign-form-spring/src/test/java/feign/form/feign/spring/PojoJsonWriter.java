package feign.form.feign.spring;

import static feign.form.ContentProcessor.CRLF;
import static feign.form.util.PojoUtil.isUserPojo;

import org.springframework.web.multipart.MultipartFile;

import feign.codec.EncodeException;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;

import lombok.val;

import java.io.IOException;

public abstract class PojoJsonWriter extends AbstractWriter {
  @Override
  public boolean isApplicable(Object object) {
    return isUserPojo(object);
  }

  @Override
  public void write (Output output, String key, Object object) throws EncodeException {
    try {
      val string = new StringBuilder()
          .append("Content-Disposition: form-data; name=\"").append(key).append('"').append(CRLF)
          .append("Content-Type: text/plain; charset=").append(output.getCharset().name()).append(CRLF)
          .append(CRLF)
          .append(convertToJsonString(object))
          .toString();

      output.write(string);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  protected abstract String convertToJsonString(Object object) throws IOException;
}
