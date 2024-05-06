/**
 * tcp穿透处理包
 * <p><b>
 * =============<br/>
 * 穿透处理流程<br/>
 * =============<br/>
 * </b></p>
 * <ol>
 *   <li><b>服务端接收tcp连接请求</b><br>
 *   向客户端发送{@link top.soy_bottle.knet.mappers.tcp.ServerNewTcpConnectionPacket}<br>
 *   此时服务器初始化成功<br>
 *   <b><i><h2>当前状态:等待客户端连接成功消息</i></b></h2>
 * <p>
 *   </li>
 *   <li><b>客户端发起向目标的连接</b><br>
 *    连接成功之后，向服务端发送{@link top.soy_bottle.knet.mappers.tcp.ClientNewTcpConnectionPacket}<br>
 *    已告知服务器连接完成
 *    <b><i><h2>当前状态:连接完成，双方可以自由发送消息</h2></i></b>
 *   </li>
 * <p>
 *   <li><b>完成连接</b><br>
 *     当客户端完成连接之后，客户端可以向服务端发送{@link top.soy_bottle.knet.mappers.tcp.ClientTcpDataPacket}<br>
 *     表示目标到用户的数据。<br>
 *     服务端可以向客户端发送{@link top.soy_bottle.knet.mappers.tcp.ServerTcpDataPacket}<br>
 *     表示用户到目标的数据<br>
 *   </li>
 * <p>
 * </ol>
 */
package top.soy_bottle.knet.mappers.tcp;