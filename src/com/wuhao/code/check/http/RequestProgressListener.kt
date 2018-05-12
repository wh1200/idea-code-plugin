/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.http

/**
 * http 请求进度监听器接口
 * Created by 吴昊 on 2018/1/12.
 */
interface RequestProgressListener {

  /**
   * 请求开始时调用一次
   * @param total 发送内容的字节数
   */
  fun start(total: Long)

  /**
   * 请求过程中连续调用
   * @param size 当前发送的字节数
   * @param currentTotal 当前总共发送的字节数
   * @param total 请求内容的总字节数
   */
  fun inProgress(size: Long, currentTotal: Long, total: Long)

  /**
   * 请求结束时调用一次
   */
  fun end()
}
