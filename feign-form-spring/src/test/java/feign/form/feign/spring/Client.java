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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import feign.Contract;
import feign.Logger;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.val;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.annotation.PathVariableParameterProcessor;
import org.springframework.cloud.openfeign.annotation.QueryMapParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestHeaderParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestParamParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Artem Labazin
 */
@FeignClient(
    name = "multipart-support-service",
    url = "http://localhost:8080",
    configuration = Client.ClientConfiguration.class
)
public interface Client {

  @RequestMapping(
      value = "/multipart/upload1/{folder}",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload1 (@PathVariable("folder") String folder,
                  @RequestPart("file") MultipartFile file,
                  @RequestParam(value = "message", required = false) String message);

  @RequestMapping(
      value = "/multipart/upload2/{folder}",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload2 (@RequestBody MultipartFile file,
                  @PathVariable("folder") String folder,
                  @RequestParam(value = "message", required = false) String message);

  @RequestMapping(
      value = "/multipart/upload3/{folder}",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload3 (@RequestBody MultipartFile file,
                  @PathVariable("folder") String folder,
                  @RequestParam(value = "message", required = false) String message);

  @RequestMapping(
      path = "/multipart/upload4/{id}",
      method = POST,
      produces = APPLICATION_JSON_VALUE
  )
  String upload4 (@PathVariable("id") String id,
                  @RequestBody Map<Object, Object> map,
                  @RequestParam("userName") String userName);

  @RequestMapping(
      path = "/multipart/upload5",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  Response upload5 (Dto dto);

  @RequestMapping(
      path = "/multipart/upload6",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload6Multiple (@RequestPart("popa1") MultipartFile file1, @RequestPart("popa2") MultipartFile file2);

  @RequestMapping(
      path = "/multipart/upload6",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload6Array (@RequestPart("files") MultipartFile[] files);

  @RequestMapping(
      path = "/multipart/upload6",
      method = POST,
      consumes = MULTIPART_FORM_DATA_VALUE
  )
  String upload6Collection (@RequestPart("files") List<MultipartFile> files);

  class ClientConfiguration {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Encoder feignEncoder () {
      return new SpringFormEncoder(new SpringEncoder(messageConverters)) {
        @Override
        public void encode (Object object, Type bodyType, RequestTemplate template) throws EncodeException {
          val contentTypeHeaders = template.headers().get("Content-Type");
          String contentType = contentTypeHeaders != null ? new ArrayList<>(contentTypeHeaders).get(0) : "";
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
      };
    }

    @Bean
    public Contract customSpringMvcContract() {
      List<AnnotatedParameterProcessor> annotatedArgumentResolvers = new ArrayList<>();

      annotatedArgumentResolvers.add(new PathVariableParameterProcessor());
      annotatedArgumentResolvers.add(new RequestParamParameterProcessor());
      annotatedArgumentResolvers.add(new RequestHeaderParameterProcessor());
      annotatedArgumentResolvers.add(new QueryMapParameterProcessor());
      annotatedArgumentResolvers.add(new RequestPartParameterProcessor());

      return new SpringMvcContract(annotatedArgumentResolvers);
    }

    @Bean
    public Logger.Level feignLogger () {
      return Logger.Level.FULL;
    }
  }
}
