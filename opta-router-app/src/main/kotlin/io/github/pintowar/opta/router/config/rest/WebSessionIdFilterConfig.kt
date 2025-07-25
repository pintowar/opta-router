package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.config.ConfigData
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Profile(ConfigData.REST_PROFILE)
class WebSessionIdFilterConfig : WebFilter {
    /**
     * Filters the web exchange to add the web session ID to the attributes.
     *
     * @param exchange The server web exchange.
     * @param chain The web filter chain.
     * @return A `Mono<Void>` that completes when the filter is applied.
     */
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        exchange.session.subscribe { it.attributes[ConfigData.WEBSESSION_ID] = it.id }
        return chain.filter(exchange)
    }
}