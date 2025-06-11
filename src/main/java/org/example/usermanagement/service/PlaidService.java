package org.example.usermanagement.service;

import com.plaid.client.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.AccountsGetRequest;
import com.plaid.client.model.AccountsGetResponse;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.Products;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlaidService {

    private final PlaidApi plaidApi;
    private final AccountService accountService;
    private final UserService userService;

    @Value("${plaid.client.id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    @Autowired
    public PlaidService(PlaidApi plaidApi, AccountService accountService, UserService userService) {
        this.plaidApi = plaidApi;
        this.accountService = accountService;
        this.userService = userService;
    }

    public String createLinkToken(String userId, String clientName) throws IOException {
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(userId);

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .clientId(clientId)
                .secret(secret)
                .clientName(clientName)
                .user(user)
                .products(List.of(Products.AUTH, Products.TRANSACTIONS))
                .countryCodes(List.of(CountryCode.US))
                .language("en");

        Response<LinkTokenCreateResponse> response = plaidApi
                .linkTokenCreate(request)
                .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Error creating Plaid link token: " + response.errorBody().string());
        }

        return response.body().getLinkToken();
    }

    public String exchangePublicToken(String publicToken) throws IOException {
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                .clientId(clientId)
                .secret(secret)
                .publicToken(publicToken);

        Response<ItemPublicTokenExchangeResponse> response = plaidApi
                .itemPublicTokenExchange(request)
                .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Error exchanging public token: " + response.errorBody().string());
        }

        return response.body().getAccessToken();
    }

    public List<Map<String, Object>> getPlaidAccounts(String accessToken) throws IOException {
        AccountsGetRequest request = new AccountsGetRequest()
                .clientId(clientId)
                .secret(secret)
                .accessToken(accessToken);

        Response<AccountsGetResponse> response = plaidApi
                .accountsGet(request)
                .execute();

        if (!response.isSuccessful()) {
            throw new IOException("Error getting accounts from Plaid: " + response.errorBody().string());
        }

        return response.body().getAccounts().stream()
                .map(account -> {
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("id", account.getAccountId());
                    accountMap.put("name", account.getName());
                    accountMap.put("type", account.getType());
                    accountMap.put("subtype", account.getSubtype());
                    accountMap.put("mask", account.getMask());

                    AccountBase.Balances balance = account.getBalances();
                    accountMap.put("current", balance.getCurrent());
                    accountMap.put("available", balance.getAvailable());
                    accountMap.put("limit", balance.getLimit());
                    accountMap.put("currency", balance.getIsoCurrencyCode());

                    return accountMap;
                })
                .collect(Collectors.toList());
    }

    public void linkPlaidAccounts(Long userId, String publicToken) throws IOException {
        String accessToken = exchangePublicToken(publicToken);

        List<Map<String, Object>> plaidAccounts = getPlaidAccounts(accessToken);
        User user = userService.findById(userId);

        for (Map<String, Object> plaidAccount : plaidAccounts) {
            String plaidAccountId = (String) plaidAccount.get("id");
            String accountName = (String) plaidAccount.get("name");
            String accountMask = (String) plaidAccount.get("mask");

            String accountNumber = "PLAID-" + accountMask;

            Number currentBalance = (Number) plaidAccount.get("current");
            BigDecimal balance = new BigDecimal(currentBalance.toString());

            String bankName = accountName.split(" ")[0];

            accountService.linkExternalAccount(
                    userId,
                    accessToken,
                    plaidAccountId,
                    accountName,
                    accountNumber,
                    balance,
                    bankName
            );
        }
    }
}
