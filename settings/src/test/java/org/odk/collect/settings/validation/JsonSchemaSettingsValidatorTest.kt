package org.odk.collect.settings.validation

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class JsonSchemaSettingsValidatorTest {
    @Test
    fun `returns true when json is valid based on schema`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid(
                """
                {
                    "foo": false
                }
                """
            ),
            equalTo(true)
        )
    }

    @Test
    fun `returns false when json is invalid based on schema`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid(
                """
                {
                    "foo": "bar"
                }
                """
            ),
            equalTo(false)
        )
    }

    @Test
    fun `returns false when json is invalid`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid("*"),
            equalTo(false)
        )
    }
}

private const val SCHEMA = """
            {
                "${'$'}schema": "https://json-schema.org/draft/2019-09/schema",
                "${'$'}id": "https://example.com/example.schema.json",
                "title": "Schema",
                "type": "object",
                "properties": {
                    "foo": {
                        "type": "boolean"
                    }
                }
            }
            """
