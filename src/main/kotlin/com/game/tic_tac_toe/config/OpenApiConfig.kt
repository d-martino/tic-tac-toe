import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Tic Tac Toe Game API",
        version = "1.0",
        description = "REST API for managing games of Tic Tac Toe"
    )
)
class OpenApiConfig {
    @Bean
    fun apiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("tic-tac-toe") // The name of the API group
            .pathsToMatch("/api/**") // Only match paths under `/api/**`
            .build()
    }
}
