/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
/**
 * 拦截器链
 *
 */
public class InterceptorChain {

  //内部就是一个拦截器的List
  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  // Mybatis内部有四处接入了Plugin，分别是Executor，ParameterHandler，ResultSetHandler，StatementHandler
  // 在植入Plugin的位置，会先调用当前pluginAll()方法，然后再将创建的对象(target)返回
  // 注：Mybatis的分页功能就是通过这个实现的，具体的处理在StatementHandler里，对sql进行了额外的处理
  public Object pluginAll(Object target) {
    //循环调用每个Interceptor.plugin方法
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
