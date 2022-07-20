/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

/**
 * @author Clinton Begin
 */
/**
 * 动态SQL源码
 *
 */
public class DynamicSqlSource implements SqlSource {

  private Configuration configuration;
  private SqlNode rootSqlNode;

  // DynamicSqlSource是在Mapper节点解析阶段创建的，作为属性放入MapperStatement对象中，而MapperStatement也是在Mapper解析阶段创建并放入Configuration对象中的
  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  // 得到绑定的SQL，流程：先构建一个动态上下文context，用来存储解析后的sql。
  // 然后调用Mapper解析阶段构建的动态sql对应的SqlNode实现类的apply()方法，将动态sql解析出来的sql放入context中($(..)参数在这里解析)。
  // 之后通过SqlSourceBuilder构建一个StaticSqlSource对象(#(..)在这里解析)，然后通过StaticSqlSource.parse()将所有相关参数放入新创建的BoundSql对象中，然后返回这个BoundSql对象。
  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    //生成一个动态上下文
    DynamicContext context = new DynamicContext(configuration, parameterObject);
	//这里SqlNode.apply只是将${}这种参数替换掉，并没有替换#{}这种参数
    // 调用MixedSqlNode.apply()方法，方法内部是对通过动态sql节点构建的对象进行调用，将其解析成具体的sql语句，解析出的sql语句放入context中
    rootSqlNode.apply(context);
	//调用SqlSourceBuilder
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
	//SqlSourceBuilder.parse,注意这里返回的是StaticSqlSource,解析完了就把那些参数（#{..}）都替换成?了，也就是最基本的JDBC的SQL写法
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
	//看似是又去递归调用SqlSource.getBoundSql，其实因为是StaticSqlSource，所以没问题，不是递归调用
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
  }

}
