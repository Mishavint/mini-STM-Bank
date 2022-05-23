package bank.restmodule.controller

import bank.restmodule.datasource.MainUserDataSource
import bank.restmodule.model.UserToChange
import bank.restmodule.model.UserToCreate
import bank.restmodule.model.UserToShow
import bank.restmodule.model.UserToTransfer
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@SpringBootTest
@AutoConfigureMockMvc

@Qualifier
class ApiControllerTests @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
) {

    private val baseUrl = "/api"
    private val executor: ExecutorService = Executors.newFixedThreadPool(2)

    @Nested
    @DisplayName("GET /api/{accountNumber}")
    inner class GetUserTests {
        @Test
        @DirtiesContext
        fun `should return bank with the given account number`() {
            // when/then
            val performGetRequest = mockMvc.get("$baseUrl/1") {
                contentType(MediaType.APPLICATION_JSON)
                content = "1234"
            }
                .andDo {
                    print()
                }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.name") { value("defaultName") }
                    jsonPath("$.moneyAmount") { value(1000000.00) }
                }
                .andReturn().response.contentAsString[10]

            // performGetRequest is 11 symbol in response body, we check that it is equal to symbol '1'
            Assertions.assertEquals(performGetRequest, '1')
        }

        @Test
        @DirtiesContext
        fun `should return NOT FOUND if the account number doesn't not exist`() {
            // given
            val accountNumber = -1

            // when/then
            mockMvc.get("$baseUrl/$accountNumber") {
                contentType(MediaType.APPLICATION_JSON)
                content = "Not real password"
            }
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    @DisplayName("Post /api")
    inner class PostNewUserTests {

        /* We know that in start of application there is one account, so we can find one or two others by
        numbers {1} and {2} */
        @Test
        @DirtiesContext
        fun `should add the new user`() {
            // given
            val newUser = UserToCreate("qwerty", "qwerty")
            val newUserGet = UserToShow(2, "qwerty", 0)

            // when
            val performPostRequest = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newUser)
            }

            // then
            performPostRequest
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(newUser))
                    }
                }

            mockMvc.get("$baseUrl/2") {
                contentType(MediaType.APPLICATION_JSON)
                content = "qwerty"
            }
                .andExpect { content { json(objectMapper.writeValueAsString(newUserGet)) } }

        }

        @Test
        @DirtiesContext
        fun `should add two users with different numbers in few threads`() {
            // given
            val newUser = UserToCreate("qwe", "asd")

            val newUserGet1 = UserToShow(2, "qwe", 0)
            val newUserGet2 = UserToShow(3, "qwe", 0)

            // when
            (0..1).map {
                executor.submit(Callable {
                    val performPostRequest1 = mockMvc.post(baseUrl) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(newUser)
                    }
                        .andDo { print() }
                        .andExpect {
                            status { isOk() }
                            content {
                                contentType(MediaType.APPLICATION_JSON)
                                json(objectMapper.writeValueAsString(newUser))
                            }
                        }
                })
            }.forEach { it.get() }

            // then

            mockMvc.get("$baseUrl/2") {
                contentType(MediaType.APPLICATION_JSON)
                content = "asd"
            }.andExpect { content { json(objectMapper.writeValueAsString(newUserGet1)) } }

            mockMvc.get("$baseUrl/3") {
                contentType(MediaType.APPLICATION_JSON)
                content = "asd"
            }.andExpect { content { json(objectMapper.writeValueAsString(newUserGet2)) } }

        }
    }

    @Nested
    @DisplayName("Patch /api/")
    inner class PatchUsersTests {
        @Test
        @DirtiesContext
        fun `should update an existing user`() {
            // given
            val updateUser = UserToChange(5, "1234")
            val updatedUser = UserToShow(1, "defaultName", 1_000_005)

            // when
            val performPathRequest = mockMvc.patch("$baseUrl/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updateUser)
            }

            // then
            performPathRequest
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content {
                        MediaType.APPLICATION_JSON
                        json(objectMapper.writeValueAsString(updatedUser))
                    }
                }

            mockMvc.get("$baseUrl/1") {
                contentType(MediaType.APPLICATION_JSON)
                content = "1234"
            }
                .andExpect { content { json(objectMapper.writeValueAsString(updatedUser)) } }
        }

        @Test
        @DirtiesContext
        fun `should return NOT FOUND if the user number doesn't exist`() {
            // given
            val updateUser = UserToChange(5, "1234")

            // when
            val performPathRequest = mockMvc.patch("$baseUrl/-1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updateUser)
            }

            // then
            performPathRequest
                .andDo { print() }
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @DirtiesContext
        fun `should update two times in two threads`() {
            // given
            val updateUser = UserToChange(5, "1234")
            val updatedUser = UserToShow(1, "defaultName", 1_000_010)

            // when
            (0..1).map {
                executor.submit(Callable {
                    mockMvc.patch("$baseUrl/1") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(updateUser)
                    }.andDo { print() }
                        .andExpect {
                            status { isOk() }
                        }
                })
            }.forEach { it.get() }

            // then
            mockMvc.get("$baseUrl/1") {
                contentType(MediaType.APPLICATION_JSON)
                content = "1234"
            }
                .andExpect { content { json(objectMapper.writeValueAsString(updatedUser)) } }
        }

        @Test
        @DirtiesContext
        fun `should return BAD REQUEST if after changing amount it will be negative money amount`() {
            // given
            val updateUser = UserToChange(-1_000_005, "1234")
            val updatedUser = UserToShow(1, "defaultName", 1_000_000)

            // when
            val performPathRequest = mockMvc.patch("$baseUrl/1") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updateUser)
            }

            // then
            performPathRequest
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                }

            mockMvc.get("$baseUrl/1") {
                contentType(MediaType.APPLICATION_JSON)
                content = "1234"
            }
                .andExpect { content { json(objectMapper.writeValueAsString(updatedUser)) } }
        }
    }

    @Nested
    @DisplayName("Patch /api")
    inner class TransferTests {

        // Using classInstance in this test was since mockMvc can not take newTransfer value as JSON
        @Test
        @DirtiesContext
        fun `should successfully transfer money`() {
            // given
            val classInstance = MainUserDataSource()

            val newUser = UserToCreate("qwerty", "qwerty")
            val newTransfer = UserToTransfer(1, 2, 5, "1234")

            classInstance.createUser(newUser)

            // when
            classInstance.transfer(newTransfer)

            // then
            val firstUser = classInstance.retrieveUser(1, "1234")
            Assertions.assertEquals(999_995, firstUser.moneyAmount)
            val secondUser = classInstance.retrieveUser(2, "qwerty")
            Assertions.assertEquals(5, secondUser.moneyAmount)
          }
    }
}