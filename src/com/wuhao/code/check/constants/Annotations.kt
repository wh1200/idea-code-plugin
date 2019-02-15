package com.wuhao.code.check.constants

/**
 *
 * Created by 吴昊 on 2019/2/15.
 *
 * @author 吴昊
 * @since
 */
object Annotations {

  const val FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient"
  const val FEIGN_CLIENT_CONFIGURATION = "org.springframework.cloud.openfeign.FeignAutoConfiguration"
  const val MYBATIS_CONFIGURATION = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"
  const val SWAGGER_API = "io.swagger.annotations.Api"
  const val SWAGGER_API_OPERATION = "io.swagger.annotations.ApiOperation"
  const val DELETE_MAPPING: String = "org.springframework.web.bind.annotation.DeleteMapping"
  const val GET_MAPPING: String = "org.springframework.web.bind.annotation.GetMapping"
  const val IBATIS_MAPPER = "org.apache.ibatis.annotations.Mapper"
  const val POST_MAPPING: String = "org.springframework.web.bind.annotation.PostMapping"
  const val PUT_MAPPING: String = "org.springframework.web.bind.annotation.PutMapping"
  const val REQUEST_MAPPING: String = "org.springframework.web.bind.annotation.RequestMapping"
  const val REST_CONTROLLER: String = "org.springframework.web.bind.annotation.RestController"

}
