package com.example.nn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AccountServiceTest {

    private final AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    public AccountServiceTest(AccountService accountService) {
        this.accountService = accountService;
    }

    @Test
    void testGetAccount() {
        // given
        var uuid = UUID.randomUUID();
        var expectedAccount = new Account(uuid, "Jan", "Kowalski", new BigDecimal(2000), new BigDecimal(0));

        when(accountRepository.findById(any())).thenReturn(Optional.of(expectedAccount));

        // when
        var result = accountService.getAccount(uuid.toString());

        // then
        result.ifPresent(account -> assertThat(account.getId()).isEqualTo(uuid));
    }

    @Test
    void testCreateAccount() {
        // given
        var uuid = UUID.randomUUID();
        var name = "Jan";
        var lastName = "Kowalski";
        var plnBalance = new BigDecimal(2000);
        var usdBalance = new BigDecimal(0);
        var createAccountRequest = new CreateAccountRequest(name, lastName, plnBalance);
        var expectedAccount = new Account(uuid, name, lastName, plnBalance, usdBalance);

        when(accountRepository.save(any())).thenReturn(expectedAccount);

        // when
        var result = accountService.createAccount(createAccountRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(uuid);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getPlnBalance()).isEqualTo(plnBalance);
        assertThat(result.getUsdBalance()).isEqualTo(usdBalance);
    }

    @Test
    void testExchangeFromPln() throws ChangeSetPersister.NotFoundException {
        // given
        var uuid = UUID.randomUUID();
        var name = "Jan";
        var lastName = "Kowalski";
        var plnBalance = new BigDecimal(2000);
        var usdBalance = new BigDecimal(0);
        var accountBefore = new Account(uuid, name, lastName, plnBalance, usdBalance);

        var plnBalanceAfter = new BigDecimal(1900);
        var usdBalanceAfter = new BigDecimal(25);
        var accountAfter = new Account(uuid, name, lastName, plnBalanceAfter, usdBalanceAfter);

        RatesResponse.Rate rate = new RatesResponse.Rate(new BigDecimal(4), LocalDate.now());
        var ratesResponse = new RatesResponse(List.of(rate));

        var amount = new BigDecimal(100);
        var from = Currency.PLN;
        var exchangeRequest = new ExchangeRequest(uuid.toString(), amount, from);

        when(accountRepository.findById(any())).thenReturn(Optional.of(accountBefore));
        when(accountRepository.save(any())).thenReturn(accountAfter);
        when(restTemplate.getForObject(any(), any())).thenReturn(ratesResponse);

        // when
        var result = accountService.exchange(uuid.toString(), amount, from);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(uuid);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getPlnBalance()).isEqualTo(plnBalanceAfter);
        assertThat(result.getUsdBalance()).isEqualTo(usdBalanceAfter);
    }

    @Test
    void testExchangeFromUsd() throws ChangeSetPersister.NotFoundException {
        // given
        var uuid = UUID.randomUUID();
        var name = "Jan";
        var lastName = "Kowalski";
        var plnBalance = new BigDecimal(1900);
        var usdBalance = new BigDecimal(25);
        var accountBefore = new Account(uuid, name, lastName, plnBalance, usdBalance);

        var plnBalanceAfter = new BigDecimal(2000);
        var usdBalanceAfter = new BigDecimal(0);
        var accountAfter = new Account(uuid, name, lastName, plnBalanceAfter, usdBalanceAfter);

        RatesResponse.Rate rate = new RatesResponse.Rate(new BigDecimal(4), LocalDate.now());
        var ratesResponse = new RatesResponse(List.of(rate));

        var amount = new BigDecimal(25);
        var from = Currency.USD;
        var exchangeRequest = new ExchangeRequest(uuid.toString(), amount, from);

        when(accountRepository.findById(any())).thenReturn(Optional.of(accountBefore));
        when(accountRepository.save(any())).thenReturn(accountAfter);
        when(restTemplate.getForObject(any(), any())).thenReturn(ratesResponse);

        // when
        var result = accountService.exchange(uuid.toString(), amount, from);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(uuid);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getPlnBalance()).isEqualTo(plnBalanceAfter);
        assertThat(result.getUsdBalance()).isEqualTo(usdBalanceAfter);
    }

    @Test
    void testExchangeFromPlnNotEnoughMoney() throws ChangeSetPersister.NotFoundException {
        // given
        var uuid = UUID.randomUUID();
        var name = "Jan";
        var lastName = "Kowalski";
        var plnBalance = new BigDecimal(50);
        var usdBalance = new BigDecimal(0);
        var accountBefore = new Account(uuid, name, lastName, plnBalance, usdBalance);

        var plnBalanceAfter = new BigDecimal(50);
        var usdBalanceAfter = new BigDecimal(0);
        var accountAfter = new Account(uuid, name, lastName, plnBalanceAfter, usdBalanceAfter);

        RatesResponse.Rate rate = new RatesResponse.Rate(new BigDecimal(4), LocalDate.now());
        var ratesResponse = new RatesResponse(List.of(rate));

        var amount = new BigDecimal(100);
        var from = Currency.PLN;
        var exchangeRequest = new ExchangeRequest(uuid.toString(), amount, from);

        when(accountRepository.findById(any())).thenReturn(Optional.of(accountBefore));
        when(accountRepository.save(any())).thenReturn(accountAfter);
        when(restTemplate.getForObject(any(), any())).thenReturn(ratesResponse);

        // when
        var result = accountService.exchange(uuid.toString(), amount, from);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(uuid);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getPlnBalance()).isEqualTo(plnBalanceAfter);
        assertThat(result.getUsdBalance()).isEqualTo(usdBalanceAfter);
    }

}