package com.example.nn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private static final int ROUND_SCALE = 2;

    private final AccountRepository accountRepository;

    String path = "https://api.nbp.pl/api/exchangerates/rates/a/usd/?format=json";

    private RatesResponse ratesResponse;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getAccount(String uuid) {
        return accountRepository.findById(UUID.fromString(uuid));
    }

    public Account createAccount(CreateAccountRequest createAccountRequest) {
        var account = new Account();
        account.setName(createAccountRequest.getName());
        account.setLastName(createAccountRequest.getLastName());
        account.setPlnBalance(createAccountRequest.getBalance());

        return accountRepository.save(account);
    }

    public Account exchange(String uuid, BigDecimal amount, Currency from) throws ChangeSetPersister.NotFoundException {
        Account account = getAccount(uuid).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (isEnoughMoney(account, amount, from)) {

            if (ratesResponse == null || ratesResponse.getRate()
                    .stream()
                    .findFirst()
                    .map(RatesResponse.Rate::getEffectiveDate)
                    .orElse(LocalDate.EPOCH)
                    .isBefore(LocalDate.now())) {
                getRating();
            }

            calculate(account, amount, from, ratesResponse.getRate()
                    .stream()
                    .findFirst()
                    .map(RatesResponse.Rate::getMid)
                    .orElse(new BigDecimal(1)));
        }

        return accountRepository.save(account);
    }

    private void getRating() {
        var restTemplate = new RestTemplate();
        this.ratesResponse = restTemplate.getForObject(path, RatesResponse.class);
    }

    private boolean isEnoughMoney(Account account, BigDecimal amount, Currency currency) {
        if (Currency.PLN.name().equalsIgnoreCase(currency.name())) {
            return account.getPlnBalance().compareTo(amount) >= 0;
        }

        if (Currency.USD.name().equalsIgnoreCase(currency.name())) {
            return account.getUsdBalance().compareTo(amount) >= 0;
        }

        return false;
    }

    private void calculate(Account account, BigDecimal amount, Currency from, BigDecimal rate) {
        if (Currency.PLN.name().equalsIgnoreCase(from.name())) {
            BigDecimal moneyToExchange = amount.divide(rate, ROUND_SCALE, RoundingMode.DOWN);
            account.setPlnBalance(account.getPlnBalance().subtract(amount));
            account.setUsdBalance(account.getUsdBalance().add(moneyToExchange));
        }

        if (Currency.USD.name().equalsIgnoreCase(from.name())) {
            BigDecimal moneyToExchange = amount.multiply(rate);
            account.setPlnBalance(account.getPlnBalance().add(moneyToExchange));
            account.setUsdBalance(account.getUsdBalance().subtract(amount));
        }
    }
}
