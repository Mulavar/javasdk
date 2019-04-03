package cn.hyperchain.sdk;

import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.common.utils.Decoder;
import cn.hyperchain.sdk.exception.RequestException;
import cn.hyperchain.sdk.hvm.StudentInvoke;
import cn.hyperchain.sdk.provider.DefaultHttpProvider;
import cn.hyperchain.sdk.provider.ProviderManager;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.service.AccountService;
import cn.hyperchain.sdk.service.ContractService;
import cn.hyperchain.sdk.service.ServiceManager;
import cn.hyperchain.sdk.transaction.Transaction;
import org.junit.Test;

public class HVMTest {

    public static String DEFAULT_URL = "localhost:9999";

    @Test
    public void testHVM() throws RequestException {
        // 1. build provider manager
        DefaultHttpProvider defaultHttpProvider = new DefaultHttpProvider.Builder().setUrl(DEFAULT_URL).build();
        ProviderManager providerManager = ProviderManager.createManager(defaultHttpProvider);

        // 2. build service
        ContractService contractService = ServiceManager.getContractService(providerManager);
        AccountService accountService = ServiceManager.getAccountService(providerManager);
        // 3. build transaction
        Account account = accountService.genECAccount();
        Transaction transaction = new Transaction.HVMBuilder(account.getAddress()).deploy("hvm-jar/hvmbasic-1.0.0-student.jar").build();
        transaction.sign(account);
        // 4. get request
        ReceiptResponse receiptResponse = contractService.deploy(transaction).send().polling();
        // 5. polling && get result && decode result
        System.out.println("合约地址: " + receiptResponse.getContractAddress());
        System.out.println("部署返回(未解码): " + receiptResponse.getRet());
        System.out.println("部署返回(解码)：" + Decoder.decodeHVM(receiptResponse.getRet(), String.class));
        // 6. invoke
        account = accountService.genSM2Account();
        Transaction transaction1 = new Transaction.HVMBuilder(account.getAddress()).invoke(receiptResponse.getContractAddress(), new StudentInvoke()).build();
        transaction1.sign(account);
        // 7. request
        ReceiptResponse receiptResponse1 = contractService.invoke(transaction1).send().polling();
        // 8. get result & decode result
        System.out.println("调用返回(未解码): " + receiptResponse1.getRet());
        System.out.println("调用返回(解码)：" + Decoder.decodeHVM(receiptResponse1.getRet(), String.class));
    }
}
