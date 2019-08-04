package feign.form.spring;

import static feign.form.ContentProcessor.CRLF;
import static feign.form.util.PojoUtil.isUserPojo;

import org.springframework.web.multipart.MultipartFile;

import feign.codec.EncodeException;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;

import lombok.val;

public class PojoJsonWriter extends AbstractWriter {
  @Override
  public boolean isApplicable(Object object) {
    return isUserPojo(object) && !(object instanceof MultipartFile) && !(object instanceof MultipartFile[]);
  }

  @Override
  public void write (Output output, String key, Object object) throws EncodeException {
    val string = new StringBuilder()
        .append("Content-Disposition: form-data; name=\"").append(key).append('"').append(CRLF)
        .append("Content-Type: application/json charset=").append(output.getCharset().name()).append(CRLF)
        .append(CRLF)
        .append("{}")
        .toString();

    output.write(string);
  }
}
