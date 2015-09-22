package org.jetbrains.ktor.netty

import io.netty.channel.*
import io.netty.handler.codec.http.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.interception.*
import org.jetbrains.ktor.util.*

public class NettyApplicationRequestContext(override val application: Application,
                                     val context: ChannelHandlerContext,
                                     val httpRequest: FullHttpRequest) : ApplicationRequestContext {

    override val attributes = Attributes()
    val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

    override val close = Interceptable0<Unit> {
        if (response.headers.get(HttpHeaders.TransferEncoding) == null
                && response.headers.get(HttpHeaders.ContentLength) == null
                && httpResponse.content().writerIndex() > 0) {
            response.headers.append(HttpHeaders.TransferEncoding, "chunked")
        }
        context.writeAndFlush(httpResponse)
        context.close()
    }

    override val request: ApplicationRequest = NettyApplicationRequest(httpRequest)
    override val response: ApplicationResponse = NettyApplicationResponse(httpResponse)
}