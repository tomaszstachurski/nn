package com.example.nn;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity getAccount(@PathVariable String uuid) {
        if (accountService.getAccount(uuid).isPresent()) {
            return ResponseEntity.ok(accountService.getAccount(uuid).get());
        }
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping()
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        return ResponseEntity.ok(
                accountService.createAccount(createAccountRequest));
    }

    @PutMapping("/exchange")
    public ResponseEntity exchange(@Valid @RequestBody ExchangeRequest exchangeRequest) {
        try {
            return ResponseEntity.ok(
                    accountService.exchange(exchangeRequest.getUuid(),
                            exchangeRequest.getAmount(),
                            exchangeRequest.getFrom()));
        } catch (ChangeSetPersister.NotFoundException e) {
            return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
