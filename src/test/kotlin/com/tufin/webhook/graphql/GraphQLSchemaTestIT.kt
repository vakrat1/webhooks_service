package com.tufin.webhook.graphql

import com.tufin.webhook.graphql.input.WebHookInput
import com.tufin.webhook.graphql.output.WebHookOutput
import com.tufin.webhook.model.WebHook
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.validation.ValidationError
import graphql.validation.ValidationErrorType
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
//import org.junit.Test
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

/**
 * Tests in this class are considered as Integrated Tests
 * These tests are using stub GraphQL server that is built based on the webhook.graphqls schema file,
 * and then uses graphQL client to invoke GraphQL queries and mutations
 */
//@Disabled
@Tags(Tag("integration"))
class GraphQLSchemaTestIT {

    private val webhookInput = WebHookInput(
            name = "webhook_test_input",
            eventType = WebHook.EventType.CREATED,
            url = "url_test_input"
    )

    private val webhookOutput1 = WebHookOutput(
            "c7f99d9f-0fc2-4bab-ac64-cfb7328403bb",
            "webhook_test",
            "url_test"
    )

    private val webhookOutput2 = WebHookOutput(
            "c7f99d9f-0fc2-4bab-ac64-cfb7328403aa",
            "webhook_test",
            "url_test"
    )


    private val idToWebHook = mapOf(
            webhookOutput1.id to webhookOutput1,
            webhookOutput2.id to webhookOutput2
    )


    private val schemaFile = ClassPathResource("webhook.graphqls")
    private val typeDefinitionRegistry: TypeDefinitionRegistry = SchemaParser().parse(schemaFile.file)
    private var schemaGenerator = SchemaGenerator()

    @Test
    fun getWebhookById_success() {
        val graphQL = createGraphQL(newRuntimeWiring()
            .type("Query") { builder ->
                builder.dataFetcher(
                    "getWebHook",
                        { idToWebHook[it.getArgument<String>("id")] })
            }
            .build())

        val executionResult = graphQL.execute(
                """{
                   getWebHook(id:"c7f99d9f-0fc2-4bab-ac64-cfb7328403aa"){
                        id
                        name
                        url
                      } 
                }"""
        )

        executionResult.errors.isEmpty().`should be true`()
        executionResult.getData<Any>().toString().`should be equal to`(
                "{getWebHook={id=c7f99d9f-0fc2-4bab-ac64-cfb7328403aa, name=webhook_test, url=url_test}}"
        )
    }

    @Test
    fun getAllWebHooks_success() {
        val graphQL = createGraphQL(newRuntimeWiring()
                .type("Query") { builder ->
                    builder.dataFetcher(
                            "getAllWebHooks", {setOf(webhookOutput1, webhookOutput2)})
                }
                .build())

        val executionResult = graphQL.execute(
                """{
                   getAllWebHooks{
                        id
                        name
                        url
                      } 
                }"""
        )

        executionResult.errors.isEmpty().`should be true`()
        executionResult.getData<Any>().toString().`should be equal to`(
                "{getAllWebHooks=[{id=c7f99d9f-0fc2-4bab-ac64-cfb7328403bb, name=webhook_test, url=url_test}, {id=c7f99d9f-0fc2-4bab-ac64-cfb7328403aa, name=webhook_test, url=url_test}]}"
        )
    }


    @Test
    fun registerWebHook_success() {
        val graphQL = createGraphQL(newRuntimeWiring()
                .type("Mutation") { builder -> builder.dataFetcher("registerWebHook", { webhookOutput1 }) }
                .build())

        val executionResult = graphQL.execute(
                """mutation{
                  registerWebHook(webHookInput: 
                    {name: "name", eventType: CREATED, url: "url"}){
                        id
                        name
                        url
                  }
                }"""
        )

        executionResult.errors.isEmpty().`should be true`()
        executionResult.getData<Any>().toString().`should be equal to`(
                "{registerWebHook={id=c7f99d9f-0fc2-4bab-ac64-cfb7328403bb, name=webhook_test, url=url_test}}")

    }


    @Test
    fun updateWebHook_success() {
        val graphQL = createGraphQL(newRuntimeWiring()
                .type("Mutation") { builder ->
                    builder.dataFetcher("updateWebHook", { true}) }
                .build())

        val executionResult = graphQL.execute(
                """mutation{
                  updateWebHook(
                    uid: "fa1220fe-a84a-48b2-9158-e50340ced6f6webHookInput",
                    webHookInput: {name: "name", eventType: CREATED, url: "url"})
                }"""
        )

        executionResult.errors.isEmpty().`should be true`()
        executionResult.getData<Any>().toString().`should be equal to`(
                "{updateWebHook=true}")

    }


    @Test
    fun deleteWebhook_success() {
        val graphQL = createGraphQL(newRuntimeWiring()
                .type("Mutation") { builder ->
                    builder.dataFetcher("deleteWebhook", { true}) }
                .build())

        val executionResult = graphQL.execute(
                """mutation{
                  deleteWebhook(uid: "90ae6d6d-ba67-482b-a2e5-a0ac364ff9a7")
                }"""
        )

        executionResult.errors.isEmpty().`should be true`()
        executionResult.getData<Any>().toString().`should be equal to`(
                "{deleteWebhook=true}")

    }




    private fun createGraphQL(): GraphQL {
        return createGraphQL(newRuntimeWiring().build())
    }

    private fun createGraphQL(runtimeWiring: RuntimeWiring): GraphQL {
        val graphQLSchema: GraphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        return GraphQL.newGraphQL(graphQLSchema).build()
    }

    private fun assertValidationError(
        executionResult: ExecutionResult,
        missingField: String,
        errorType: ValidationErrorType
    ) {
        executionResult.errors.size.`should be`(1)
        executionResult.errors[0] `should be instance of` (ValidationError::class)
        (executionResult.errors[0] as ValidationError).validationErrorType.`should be`(errorType)
        (executionResult.errors[0] as ValidationError).description shouldContain missingField
    }
}