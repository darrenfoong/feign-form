/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package feign.form.feign.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.form.MultipartFormContentProcessor;
import lombok.val;

import java.io.IOException;
import java.lang.reflect.Type;

import static feign.form.ContentType.MULTIPART;

public class SpringJsonFormEncoder extends FormEncoder {

  /**
   * Constructor with the default Feign's encoder as a delegate.
   */
  public SpringJsonFormEncoder() {
    this(new Default());
  }

  /**
   * Constructor with specified delegate encoder.
   *
   * @param delegate  delegate encoder, if this encoder couldn't encode object.
   */
  public SpringJsonFormEncoder(Encoder delegate) {
    super(delegate);

    val processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
    processor.addFirstWriter(new PojoJsonWriter() {
      private ObjectMapper objectMapper = new ObjectMapper();

      @Override
      protected String convertToJsonString(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
      }
    });
  }

  @Override
  public void encode (Object object, Type bodyType, RequestTemplate template) throws EncodeException {
    super.encode(object, bodyType, template);
  }
}
