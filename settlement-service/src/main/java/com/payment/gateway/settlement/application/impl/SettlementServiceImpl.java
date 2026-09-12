package com.payment.gateway.settlement.application.impl;

import com.payment.gateway.common.events.PaymentSucceededEvent;
import com.payment.gateway.common.utils.IdGenerator;
import com.payment.gateway.settlement.application.service.SettlementService;
import com.payment.gateway.settlement.domain.entity.Settlement;
import com.payment.gateway.settlement.domain.entity.SettlementTransaction;
import com.payment.gateway.settlement.domain.enums.SettlementStatus;
import com.payment.gateway.settlement.domain.repository.SettlementRepository;
import com.payment.gateway.settlement.domain.repository.SettlementTransactionRepository;
import com.payment.gateway.settlement.infrastructure.s3.S3ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final S3ReportService s3ReportService;

    @Override
    @Transactional
    public void recordSuccessfulPayment(PaymentSucceededEvent event) {
        LocalDate today = LocalDate.now();

        Settlement settlement = settlementRepository
                .findByMerchantIdAndSettlementDateAndCurrency(
                        event.getMerchantId(), today, event.getCurrency())
                .orElseGet(() -> createNewSettlement(event.getMerchantId(), today, event.getCurrency()));

        settlement.setTotalAmount(settlement.getTotalAmount().add(event.getAmount()));
        settlement.setTransactionCount(settlement.getTransactionCount() + 1);

        try {
            settlement = settlementRepository.save(settlement);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent settlement creation detected for merchant: {} date: {}, retrying",
                    event.getMerchantId(), today);
            settlement = settlementRepository
                    .findByMerchantIdAndSettlementDateAndCurrency(
                            event.getMerchantId(), today, event.getCurrency())
                    .orElseThrow();
            settlement.setTotalAmount(settlement.getTotalAmount().add(event.getAmount()));
            settlement.setTransactionCount(settlement.getTransactionCount() + 1);
            settlementRepository.save(settlement);
        }

        SettlementTransaction tx = SettlementTransaction.builder()
                .id(IdGenerator.generate())
                .settlement(settlement)
                .transactionId(event.getTransactionId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .processorRef(event.getProcessorRef())
                .build();

        settlementTransactionRepository.save(tx);
        log.info("Recorded payment {} in settlement for merchant: {} date: {}",
                event.getTransactionId(), event.getMerchantId(), today);
    }

    @Override
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void exportPendingSettlements() {
        List<Settlement> pending = settlementRepository.findByStatus(SettlementStatus.PENDING);

        if (pending.isEmpty()) {
            log.info("No pending settlements to export");
            return;
        }

        log.info("Exporting {} pending settlements", pending.size());

        for (Settlement settlement : pending) {
            try {
                List<SettlementTransaction> transactions =
                        settlementTransactionRepository.findBySettlementId(settlement.getId());

                String csvContent = buildCsv(settlement, transactions);
                String s3Key = buildS3Key(settlement);

                s3ReportService.uploadReport(s3Key, csvContent);

                settlement.setS3ReportKey(s3Key);
                settlement.setStatus(SettlementStatus.EXPORTED);
                settlementRepository.save(settlement);

                log.info("Settlement exported for merchant: {} date: {} key: {}",
                        settlement.getMerchantId(), settlement.getSettlementDate(), s3Key);

            } catch (Exception e) {
                log.error("Failed to export settlement: {}", settlement.getId(), e);
            }
        }
    }

    private Settlement createNewSettlement(String merchantId, LocalDate date, String currency) {
        Settlement settlement = Settlement.builder()
                .id(IdGenerator.generate())
                .merchantId(merchantId)
                .settlementDate(date)
                .totalAmount(BigDecimal.ZERO)
                .transactionCount(0)
                .currency(currency)
                .status(SettlementStatus.PENDING)
                .build();
        return settlementRepository.save(settlement);
    }

    private String buildCsv(Settlement settlement, List<SettlementTransaction> transactions) {
        StringBuilder csv = new StringBuilder();
        csv.append("transaction_id,amount,currency,processor_ref,created_at\n");
        for (SettlementTransaction tx : transactions) {
            csv.append(tx.getTransactionId()).append(",")
               .append(tx.getAmount()).append(",")
               .append(tx.getCurrency()).append(",")
               .append(tx.getProcessorRef() != null ? tx.getProcessorRef() : "").append(",")
               .append(tx.getCreatedAt()).append("\n");
        }
        csv.append("\nSummary\n");
        csv.append("Total Amount,").append(settlement.getTotalAmount()).append("\n");
        csv.append("Transaction Count,").append(settlement.getTransactionCount()).append("\n");
        csv.append("Settlement Date,").append(settlement.getSettlementDate()).append("\n");
        return csv.toString();
    }

    private String buildS3Key(Settlement settlement) {
        return String.format("settlements/%s/%s/settlement_%s.csv",
                settlement.getMerchantId(),
                settlement.getSettlementDate(),
                settlement.getCurrency());
    }
}
