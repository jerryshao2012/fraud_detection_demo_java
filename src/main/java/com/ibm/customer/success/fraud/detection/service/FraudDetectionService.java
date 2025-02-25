package com.ibm.customer.success.fraud.detection.service;

import com.ibm.customer.success.fraud.detection.model.FraudReport;
import com.ibm.customer.success.fraud.detection.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

// POJO for LLM's response
class LLMResponse {
    private String response;

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}

@Service
public class FraudDetectionService {

    // ----------------------------------------------------- Instance Variables

    /**
     * Logger settings
     */
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    private final List<Transaction> historicalTransactions = new ArrayList<>();

    @Value("${llm.model}")
    private String llmModel;

    @Value("${llm.endpoint.url}")
    private String llmEndpointUrl;

    // ----------------------------------------------------- Private Methods

    private List<Transaction> findSimilarTransactions(Transaction newTx) {
        List<Transaction> similar = new ArrayList<>();
        String newTokenized = tokenize(newTx.toString());
        for (Transaction tx : historicalTransactions) {
            String tokenized = tokenize(tx.toString());
            if (jaccardSimilarity(newTokenized, tokenized) > 0.2) {
                similar.add(tx);
            }
        }
        return similar;
    }

    private double jaccardSimilarity(String a, String b) {
        Set<String> setA = new HashSet<>(Arrays.asList(a.split(" ")));
        Set<String> setB = new HashSet<>(Arrays.asList(b.split(" ")));
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private String tokenize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    private String generateLLMReport(Transaction tx, List<Transaction> history) {
        // Build prompt
        String prompt = String.format("""
                        Analyze Transaction: %s
                        Historical Patterns:
                        %s
                        Recommended Actions:""",
                tx.toString(),
                history.stream().map(Transaction::toString).collect(Collectors.joining("\n"))
        );
        WebClient webClient = WebClient.create(llmEndpointUrl);
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", prompt);
        request.put("model", llmModel); // Use a valid model name
        request.put("stream", false);

        logger.debug("llmEndpointUrl: {}", llmEndpointUrl);
        logger.debug("prompt: {}", prompt);
        logger.debug("model: {}", llmModel);

        try {
            LLMResponse response = webClient.post()
                    .uri("/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(LLMResponse.class)
                    .block();
            // {"model":"llama3.2","created_at":"2025-02-24T21:45:21.343879Z","response":"Analysis of the Transaction:\\n\\n**Transaction Details**\\n\\n* `transactionId`: null (missing identifier)\\n* `amount`: 8000.0 (significant amount, potentially suspicious for a single transaction)\\n* `location`: 'Sydney, Australia' (clear geographic location, could be legitimate or used to mask IP address)\\n* `recipient`: 'New Vendor Co.' (potential red flag, as the recipient is not well-known and may be a new entity)\\n* `device`: 'Unrecognized Device XY-12' (unknown device, potentially used for malicious activities)\\n* `transactionType`: 'Transfer' (clear transaction type, could be legitimate or part of a phishing attempt)\\n\\n**Historical Patterns**\\n\\nAlthough no specific historical patterns are provided, we can analyze the given data to make some inferences:\\n\\n1. **Lack of transaction ID**: The absence of a unique transaction ID raises concerns about the legitimacy of the transaction.\\n2. **Large amount**: An amount of 8000.0 is significant and may indicate an attempt to launder money or conduct a large-scale financial transaction without proper authorization.\\n3. **Unknown device**: The use of an unrecognized device could suggest that the recipient is trying to hide their identity or mask malicious activity.\\n4. **New vendor**: The recipient's name, 'New Vendor Co.', is not well-known and may be used as a front for illicit activities.\\n\\n**Recommended Actions**\\n\\nBased on these observations, recommended actions might include:\\n\\n1. **Verify recipient information**: Research the recipient company to determine its legitimacy and ensure it is not a known entity involved in money laundering or other illicit activities.\\n2. **Investigate device usage**: Attempt to identify the device used by the sender and investigate any potential connections to malicious activity or suspicious behavior.\\n3. **Monitor transaction history**: Keep an eye on future transactions involving this recipient and device to detect any patterns of suspicious behavior.\\n4. **Report to authorities (if necessary)**: If the analysis suggests that the transaction may be related to money laundering or other illicit activities, report it to the relevant authorities.\\n\\nKeep in mind that these actions should only be taken after conducting a thorough investigation and verifying the findings with additional data and analysis.","done":true,"done_reason":"stop","context":[128006,9125,128007,271,38766,1303,33025,2696,25,6790,220,2366,18,271,128009,128006,882,128007,271,2127,56956,18351,25,18351,90,13838,769,1151,2994,518,3392,28,4728,15,13,15,11,3813,1151,35767,19316,11,8494,518,22458,1151,3648,46236,3623,16045,3756,1151,1844,47167,14227,58419,12,717,518,7901,941,1151,22737,16823,50083,950,63823,1473,57627,27820,25,128009,128006,78191,128007,271,27671,315,279,18351,1473,334,8230,12589,57277,9,1595,13838,769,45722,854,320,31716,13110,340,9,1595,6173,45722,220,4728,15,13,15,320,91645,3392,11,13893,32427,369,264,3254,7901,340,9,1595,2588,45722,364,35767,19316,11,8494,6,320,7574,46139,3813,11,1436,387,23583,477,1511,311,7056,6933,2686,340,9,1595,43710,45722,364,3648,46236,3623,3238,320,93036,2579,5292,11,439,279,22458,374,539,1664,22015,323,1253,387,264,502,5502,340,9,1595,6239,45722,364,1844,47167,14227,58419,12,717,6,320,16476,3756,11,13893,1511,369,39270,7640,340,9,1595,13838,941,45722,364,22737,6,320,7574,7901,955,11,1436,387,23583,477,961,315,264,99197,4879,696,334,50083,950,63823,57277,16179,912,3230,13970,12912,527,3984,11,584,649,24564,279,2728,828,311,1304,1063,304,5006,1473,16,13,3146,43,474,315,7901,3110,96618,578,19821,315,264,5016,7901,3110,25930,10742,922,279,57008,315,279,7901,627,17,13,3146,35353,3392,96618,1556,3392,315,220,4728,15,13,15,374,5199,323,1253,13519,459,4879,311,54138,3300,477,6929,264,3544,13230,6020,7901,2085,6300,24645,627,18,13,3146,14109,3756,96618,578,1005,315,459,97239,3756,1436,4284,430,279,22458,374,4560,311,10477,872,9764,477,7056,39270,5820,627,19,13,3146,3648,21390,96618,578,22458,596,836,11,364,3648,46236,3623,16045,374,539,1664,22015,323,1253,387,1511,439,264,4156,369,59229,7640,382,334,57627,27820,57277,29815,389,1521,24654,11,11349,6299,2643,2997,1473,16,13,3146,33727,22458,2038,96618,8483,279,22458,2883,311,8417,1202,57008,323,6106,433,374,539,264,3967,5502,6532,304,3300,64402,477,1023,59229,7640,627,17,13,3146,34976,65056,3756,10648,96618,44617,311,10765,279,3756,1511,555,279,4750,323,19874,904,4754,13537,311,39270,5820,477,32427,7865,627,18,13,3146,31198,7901,3925,96618,13969,459,8071,389,3938,14463,16239,420,22458,323,3756,311,11388,904,12912,315,32427,7865,627,19,13,3146,10577,311,11527,320,333,5995,33395,25,1442,279,6492,13533,430,279,7901,1253,387,5552,311,3300,64402,477,1023,59229,7640,11,1934,433,311,279,9959,11527,382,19999,304,4059,430,1521,6299,1288,1193,387,4529,1306,31474,264,17879,8990,323,69963,279,14955,449,5217,828,323,6492,13],"total_duration":7177243500,"load_duration":829654041,"prompt_eval_count":77,"prompt_eval_duration":1180000000,"eval_count":444,"eval_duration":5165000000}
            logger.debug("response: {}", response);

            if (response != null && response.getResponse() != null) {
                return response.getResponse();
            } else {
                throw new RuntimeException("Empty response from LLM.");
            }
        } catch (WebClientResponseException e) {
            throw new RuntimeException("LLM API error: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------- Protected Methods

    // ----------------------------------------------------- Public Methods

    public List<Transaction> addHistoricalTransaction(Transaction transaction) {
        historicalTransactions.add(transaction);
        return historicalTransactions;
    }

    public List<Transaction> addHistoricalTransactions(List<Transaction> transactions) {
        if (transactions != null) {
            historicalTransactions.addAll(transactions);
        }
        return historicalTransactions;
    }

    public void resetHistoricalTransactionsForTesting() {
        historicalTransactions.clear();
    }

    public List<Transaction> getHistoricalTransactions() {
        return historicalTransactions;
    }

    public FraudReport analyzeTransaction(Transaction newTransaction) {
        List<Transaction> similar = findSimilarTransactions(newTransaction);
        String recommendation = generateLLMReport(newTransaction, similar);
        return new FraudReport(newTransaction, similar, recommendation);
    }

}
