package com.example.nn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class AccountControllerTest {

    AccountController accountController;

    private final MockMvc mvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    public AccountControllerTest(AccountController accountController, MockMvc mvc) {
        this.accountController = accountController;
        this.mvc = mvc;
    }

    @Test
    void testGetAccountRequest() throws Exception {
        // given
        var uuid = UUID.randomUUID();
        var account = new Account(uuid, "Jan", "Kowalski", new BigDecimal(2000), new BigDecimal(0));

        // when
        when(accountService.getAccount(uuid.toString())).thenReturn(Optional.of(account));

        // then
        mvc.perform(MockMvcRequestBuilders.get("/account/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid.toString())))
                .andExpect(jsonPath("$.name", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.plnBalance", is(2000)))
                .andExpect(jsonPath("$.usdBalance", is(0)));
    }

    @Test
    void testGetAccountNotFound() throws Exception {
        // given
        var uuid = UUID.randomUUID();

        // when
        when(accountService.getAccount(any())).thenReturn(Optional.empty());

        // then
        mvc.perform(MockMvcRequestBuilders.get("/account/" + uuid))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account not found"));
    }

    @Test
    void testCreateAccount() throws Exception {
        // given
        var createAccountRequest = new CreateAccountRequest("Jan", "Kowalski", new BigDecimal(2000));
        var uuid = UUID.randomUUID();
        var expectedAccount = new Account(uuid, "Jan", "Kowalski", new BigDecimal(2000), new BigDecimal(0));

        // when
        when(accountService.createAccount(any())).thenReturn(expectedAccount);

        // then
        mvc.perform(MockMvcRequestBuilders.post("/account")
                        .content(asJsonString(createAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid.toString())))
                .andExpect(jsonPath("$.name", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.plnBalance", is(2000)))
                .andExpect(jsonPath("$.usdBalance", is(0)));
    }

    @Test
    void testCreateAccountMissingProperties() throws Exception {
        // given
        var createAccountRequest = new CreateAccountRequest();

        // when then
        mvc.perform(MockMvcRequestBuilders.post("/account")
                        .content(asJsonString(createAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name is mandatory")))
                .andExpect(jsonPath("$.lastName", is("Lastname is mandatory")))
                .andExpect(jsonPath("$.balance", is("Balance is mandatory")));
    }

    @Test
    void testExchangeCurrency() throws Exception {
        // given
        var uuid = UUID.randomUUID();
        var exchangeRequest = new ExchangeRequest(uuid.toString(), new BigDecimal(100), Currency.PLN);
        var expectedAccount = new Account(uuid, "Jan", "Kowalski", new BigDecimal(1900), new BigDecimal(25));

        // when
        when(accountService.exchange(any(), any(), any())).thenReturn(expectedAccount);

        // then
        mvc.perform(MockMvcRequestBuilders.put("/account/exchange")
                        .content(asJsonString(exchangeRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid.toString())))
                .andExpect(jsonPath("$.name", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.plnBalance", is(1900)))
                .andExpect(jsonPath("$.usdBalance", is(25)));
    }

    @Test
    void testExchangeCurrencyMissingProperties() throws Exception {
        // given
        var exchangeRequest = new ExchangeRequest();

        // when then
        mvc.perform(MockMvcRequestBuilders.put("/account/exchange")
                        .content(asJsonString(exchangeRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uuid", is("uuid is mandatory")))
                .andExpect(jsonPath("$.amount", is("Amount is mandatory")))
                .andExpect(jsonPath("$.from", is("From is mandatory")));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}