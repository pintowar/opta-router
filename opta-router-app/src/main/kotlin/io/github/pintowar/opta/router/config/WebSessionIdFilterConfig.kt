package io.github.pintowar.opta.router.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
class WebSessionIdFilterConfig : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        exchange.session.subscribe {
            logger.debug { "Filter on ${exchange.request.path} with session-id ${it.id}" }
            it.attributes[ConfigData.WEBSESSION_ID] = it.id
        }
        return chain.filter(exchange)
    }
}