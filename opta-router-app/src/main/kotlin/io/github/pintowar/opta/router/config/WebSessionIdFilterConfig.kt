package io.github.pintowar.opta.router.config

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class WebSessionIdFilterConfig : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        exchange.session.subscribe { it.attributes["websession-id"] = it.id }
        return chain.filter(exchange)
    }
}