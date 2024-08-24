package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.config.ConfigData
import io.r2dbc.spi.ConnectionFactory
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.ExecuteListenerProvider
import org.jooq.SQLDialect
import org.jooq.TransactionProvider
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultExecuteListenerProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.autoconfigure.jooq.ExceptionTranslatorExecuteListener
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@Profile(ConfigData.REST_PROFILE)
@EnableConfigurationProperties(FlywayProperties::class)
class DatabaseConfig(
    @Value("\${spring.jooq.sql-dialect}") private val sqlDialect: SQLDialect,
    @Value("\${spring.jooq.bind-offset-date-time-type}") private val bindOffsetDateTimeType: Boolean,
    private val cfi: ConnectionFactory
) {

    @Bean
    @ConditionalOnBean(PlatformTransactionManager::class)
    @ConditionalOnMissingBean(
        TransactionProvider::class
    )
    fun transactionProvider(txManager: PlatformTransactionManager): SpringTransactionProvider {
        return SpringTransactionProvider(txManager)
    }

    @Bean
    @Order(0)
    fun jooqExceptionTranslatorExecuteListenerProvider(
        exceptionTranslatorExecuteListener: ExceptionTranslatorExecuteListener
    ): DefaultExecuteListenerProvider {
        return DefaultExecuteListenerProvider(exceptionTranslatorExecuteListener)
    }

    @Bean
    @ConditionalOnMissingBean(ExceptionTranslatorExecuteListener::class)
    fun jooqExceptionTranslator(): ExceptionTranslatorExecuteListener {
        return ExceptionTranslatorExecuteListener.DEFAULT
    }

    @Bean
    fun jooqDslContext(executeListenerProviders: ObjectProvider<ExecuteListenerProvider>): DSLContext =
        DSL.using(TransactionAwareConnectionFactoryProxy(cfi))
            .configuration()
            .set(sqlDialect)
            .set(*executeListenerProviders.orderedStream().toList().toTypedArray())
            .derive(Settings().withBindOffsetDateTimeType(bindOffsetDateTimeType))
            .dsl()

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "spring.flyway", name = ["enabled"], havingValue = "true", matchIfMissing = false)
    fun flyway(flywayProperties: FlywayProperties): Flyway =
        Flyway.configure()
            .dataSource(
                flywayProperties.url,
                flywayProperties.user,
                flywayProperties.password
            )
            .locations(*flywayProperties.locations.toTypedArray())
            .baselineOnMigrate(true)
            .load()
}