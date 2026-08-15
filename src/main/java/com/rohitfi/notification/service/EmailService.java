package com.rohitfi.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async // Runs in a background thread!
    public void sendWelcomeEmail(String toEmail, String mobile) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Ro💳Hit📈Fi — Your Digital Banking Journey 🚀");

            String htmlContent = "<!doctype html>"
                    + "<html><head>"
                    + "<meta charset='utf-8'/>"
                    + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                    + "<style>"
                    + "  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; color: #111827; background:#f3f4f6; margin:0; padding:20px; }"
                    + "  .wrap { max-width:680px; margin:0 auto; }"
                    + "  .card { background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 6px 18px rgba(15,23,42,0.06); }"
                    + "  .header { padding:20px 24px; background:linear-gradient(90deg,#0b5cff 0%,#00c4ff 100%); color:#fff; display:flex; align-items:center; gap:12px; }"
                    + "  .brand { font-weight:700; font-size:20px; letter-spacing:0.2px; }"
                    + "  .body { padding:22px; }"
                    + "  .muted { color:#6b7280; font-size:13px; }"
                    + "  .notice { background:#fff7ed; border-left:4px solid #f59e0b; padding:12px; border-radius:6px; margin:14px 0; color:#92400e; }"
                    + "  .cta { display:inline-block; margin-top:12px; padding:10px 14px; background:#0b5cff; color:#fff; border-radius:8px; text-decoration:none; font-weight:600; }"
                    + "  .footer { padding:16px 22px; background:#f9fafb; color:#6b7280; font-size:12px; }"
                    + "  .small { font-size:13px; color:#374151; }"
                    + "  @media (max-width:480px){ .header{padding:16px} .body{padding:16px} }"
                    + "</style>"
                    + "</head><body>"
                    + "<div class='wrap'>"
                    + "  <div class='card'>"
                    + "    <div class='header'>"
                    + "      <div style='font-size:28px;'>💳📈</div>"
                    + "      <div>"
                    + "        <div class='brand'>Ro<span style='color:#ffd166;'>💳</span>Hit<span style='color:#ffd166;'>📈</span>Fi</div>"
                    + "        <div style='font-size:12px; opacity:0.95;'>Secure · Fast · Modern</div>"
                    + "      </div>"
                    + "    </div>"
                    + "    <div class='body'>"
                    + "      <h3 style='margin:0 0 8px 0;'>Welcome to Ro<span style='color:#0b5cff;'>💳</span>Hit<span style='color:#0b5cff;'>📈</span>Fi 👋</h3>"
                    + "      <p class='small'>Hi,</p>"
                    + "      <p class='small'>🎉 Thank you for joining <strong>RoHitFi Digital Banking</strong>. Your account has been successfully created and linked to the mobile number <strong>+91 " + mobile + "</strong>.</p>"
                    + "      <div class='notice'>"
                    + "        <strong>Security</strong> — Your session tokens (JWT) are valid for <strong>24 hours</strong>. Never share your credentials or OTPs with anyone."
                    + "      </div>"
                    + "      <p class='small'>You can access your dashboard to explore features like instant transfers, bill payments, and investments. If you need help, our in-app support is available 24/7.</p>"
                    + "      <a class='cta' href='#' target='_blank' rel='noopener'>Open Dashboard</a>"
                    + "      <p style='margin-top:18px;' class='muted'>Warm regards,<br/><strong>The RoHitFi Security Team</strong></p>"
                    + "    </div>"
                    + "    <div class='footer'>"
                    + "      <div class='muted'>This is an automated message. Please do not reply to this email.</div>"
                    + "    </div>"
                    + "  </div>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Welcome email sent asynchronously to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", toEmail, e);
        }
    }

    @Async // Runs in a background thread!
    public void sendTransactionReceipt(String toEmail, String refNo, BigDecimal amount, String type, BigDecimal balanceAfter) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Ro💳Hit📈Fi Transaction Alert 🔔 — Ref: " + refNo);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
            String color = type.equalsIgnoreCase("CREDIT") ? "#16a34a" : "#dc2626"; // green for credit, red for debit

            String htmlContent = "<!doctype html>"
                    + "<html><head>"
                    + "<meta charset='utf-8'/>"
                    + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                    + "<style>"
                    + "  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; color: #111827; background:#f3f4f6; margin:0; padding:20px; }"
                    + "  .wrap { max-width:720px; margin:0 auto; }"
                    + "  .card { background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 6px 18px rgba(15,23,42,0.06); }"
                    + "  .header { padding:18px 22px; background:linear-gradient(90deg,#0b5cff 0%,#00c4ff 100%); color:#fff; display:flex; align-items:center; gap:12px; }"
                    + "  .title { font-size:18px; font-weight:700; }"
                    + "  .body { padding:20px; }"
                    + "  .table { width:100%; border-collapse:collapse; margin-top:12px; }"
                    + "  .table td { padding:10px 8px; border-bottom:1px solid #f1f5f9; }"
                    + "  .label { color:#6b7280; width:40%; }"
                    + "  .value { font-weight:700; }"
                    + "  .amount { color:" + color + "; font-size:16px; font-weight:800; }"
                    + "  .notice { margin-top:14px; color:#374151; }"
                    + "  .footer { padding:14px 20px; background:#f9fafb; color:#6b7280; font-size:12px; }"
                    + "  @media (max-width:480px){ .header{padding:14px} .body{padding:14px} }"
                    + "</style>"
                    + "</head><body>"
                    + "<div class='wrap'>"
                    + "  <div class='card'>"
                    + "    <div class='header'>"
                    + "      <div style='font-size:26px;'>🔔</div>"
                    + "      <div>"
                    + "        <div class='title'>Transaction Alert</div>"
                    + "        <div style='font-size:12px; opacity:0.95;'>Reference: <strong>" + refNo + "</strong></div>"
                    + "      </div>"
                    + "    </div>"
                    + "    <div class='body'>"
                    + "      <p style='margin:0 0 8px 0;'>A transaction was processed on your RoHitFi account.</p>"
                    + "      <table class='table'>"
                    + "        <tr><td class='label'>Reference No</td><td class='value'>" + refNo + "</td></tr>"
                    + "        <tr><td class='label'>Date & Time</td><td class='value'>" + dateStr + "</td></tr>"
                    + "        <tr><td class='label'>Type</td><td class='value'>" + type + "</td></tr>"
                    + "        <tr><td class='label'>Amount</td><td class='value amount'>₹ " + amount + "</td></tr>"
                    + "        <tr><td class='label'>Available Balance</td><td class='value'>₹ " + balanceAfter + "</td></tr>"
                    + "      </table>"
                    + "      <p class='notice'>If you did not authorize this transaction, please block your card immediately via the dashboard or contact support.</p>"
                    + "    </div>"
                    + "    <div class='footer'>"
                    + "      🔐 Automated notification from <strong>Ro<span style='color:#ffd166;'>💳</span>Hit<span style='color:#ffd166;'>📈</span>Fi</strong>. For assistance, visit your dashboard."
                    + "    </div>"
                    + "  </div>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Transaction receipt sent asynchronously to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send transaction receipt to {}", toEmail, e);
        }
    }

@Async
public void sendLoanDisbursementEmail(String toEmail, String loanType, BigDecimal amount,
                                      BigDecimal emiAmount, Integer tenureMonths,
                                      BigDecimal interestRate, BigDecimal accountBalance) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("🎉 Loan Disbursed Successfully — Ro💳Hit📈Fi");

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        String htmlContent = "<!doctype html>"
                + "<html><head>"
                + "<meta charset='utf-8'/>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                + "<style>"
                + "  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; color:#111827; background:#f3f4f6; margin:0; padding:20px; }"
                + "  .wrap { max-width:680px; margin:0 auto; }"
                + "  .card { background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 6px 18px rgba(15,23,42,0.08); }"
                + "  .header { padding:20px; background:linear-gradient(90deg,#16a34a,#22c55e); color:#fff; display:flex; align-items:center; gap:12px; }"
                + "  .title { font-size:20px; font-weight:700; }"
                + "  .body { padding:22px; }"
                + "  .table { width:100%; border-collapse:collapse; margin-top:12px; }"
                + "  .table td { padding:10px 8px; border-bottom:1px solid #f1f5f9; }"
                + "  .label { color:#6b7280; width:40%; }"
                + "  .value { font-weight:700; }"
                + "  .highlight { font-size:16px; font-weight:800; color:#065f46; }"
                + "  .footer { padding:14px 20px; background:#f9fafb; color:#6b7280; font-size:12px; }"
                + "</style>"
                + "</head><body>"
                + "<div class='wrap'>"
                + "  <div class='card'>"
                + "    <div class='header'>"
                + "      <div style='font-size:28px;'>💰</div>"
                + "      <div>"
                + "        <div class='title'>Loan Disbursement Alert</div>"
                + "        <div style='font-size:12px; opacity:0.9;'>Date: " + dateStr + "</div>"
                + "      </div>"
                + "    </div>"
                + "    <div class='body'>"
                + "      <p>Great news 🎉 — your <strong>" + loanType + "</strong> loan has been approved and credited to your account.</p>"
                + "      <table class='table'>"
                + "        <tr><td class='label'>Loan Type</td><td class='value'>" + loanType + "</td></tr>"
                + "        <tr><td class='label'>Disbursed Amount</td><td class='value highlight'>₹ " + amount + "</td></tr>"
                + "        <tr><td class='label'>Interest Rate</td><td class='value'>" + interestRate + "% p.a.</td></tr>"
                + "        <tr><td class='label'>Tenure</td><td class='value'>" + tenureMonths + " Months</td></tr>"
                + "        <tr><td class='label'>Monthly EMI</td><td class='value'>₹ " + emiAmount + "</td></tr>"
                + "        <tr><td class='label'>Updated Bank Balance</td><td class='value'>₹ " + accountBalance + "</td></tr>"
                + "      </table>"
                + "      <p style='margin-top:14px;'>📅 Your EMI schedule is now active. You can view the full schedule in your banking dashboard.</p>"
                + "      <p style='margin-top:12px;'>Thank you for banking with <strong>Ro<span style='color:#ffd166;'>💳</span>Hit<span style='color:#ffd166;'>📈</span>Fi</strong>.</p>"
                + "    </div>"
                + "    <div class='footer'>"
                + "      🔐 This is an automated notification. For queries, please visit your dashboard or contact support."
                + "    </div>"
                + "  </div>"
                + "</div>"
                + "</body></html>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
        log.info("Enriched loan disbursement email sent asynchronously to {}", toEmail);

    } catch (MessagingException e) {
        log.error("Failed to send loan email to {}", toEmail, e);
    }
}


    @Async
    public void sendInvestmentReceiptEmail(String toEmail, String assetName, int quantity, BigDecimal totalAmount, String orderType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("📈 Investment Order Executed — " + assetName);

            String color = orderType.equalsIgnoreCase("BUY") ? "#16a34a" : "#dc2626";
            String action = orderType.equalsIgnoreCase("BUY") ? "purchased" : "sold";
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));

            String htmlContent = "<!doctype html>"
                    + "<html><head>"
                    + "<meta charset='utf-8'/>"
                    + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                    + "<style>"
                    + "  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; color: #111827; background:#f3f4f6; margin:0; padding:20px; }"
                    + "  .wrap { max-width:720px; margin:0 auto; }"
                    + "  .card { background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 6px 18px rgba(15,23,42,0.06); }"
                    + "  .header { padding:18px 22px; background:linear-gradient(90deg,#f59e0b 0%,#f97316 100%); color:#fff; display:flex; align-items:center; gap:12px; }"
                    + "  .body { padding:20px; }"
                    + "  .table { width:100%; border-collapse:collapse; margin-top:12px; }"
                    + "  .table td { padding:10px 8px; border-bottom:1px solid #f1f5f9; }"
                    + "  .label { color:#6b7280; width:40%; }"
                    + "  .value { font-weight:700; }"
                    + "  .amount { color:" + color + "; font-size:16px; font-weight:800; }"
                    + "  .footer { padding:14px 20px; background:#f9fafb; color:#6b7280; font-size:12px; }"
                    + "</style>"
                    + "</head><body>"
                    + "<div class='wrap'>"
                    + "  <div class='card'>"
                    + "    <div class='header'>"
                    + "      <div style='font-size:26px;'>📊</div>"
                    + "      <div>"
                    + "        <div style='font-weight:700;'>Investment Confirmation</div>"
                    + "        <div style='font-size:12px; opacity:0.95;'>Executed: " + dateStr + "</div>"
                    + "      </div>"
                    + "    </div>"
                    + "    <div class='body'>"
                    + "      <p style='margin:0 0 8px 0;'>Your investment order has been successfully executed ✅</p>"
                    + "      <table class='table'>"
                    + "        <tr><td class='label'>Order Type</td><td class='value'>" + orderType + "</td></tr>"
                    + "        <tr><td class='label'>Asset Name</td><td class='value'>" + assetName + "</td></tr>"
                    + "        <tr><td class='label'>Quantity</td><td class='value'>" + quantity + " Units</td></tr>"
                    + "        <tr><td class='label'>Total Trade Value</td><td class='value amount'>₹ " + totalAmount + "</td></tr>"
                    + "      </table>"
                    + "      <p style='margin-top:12px;'>🎯 You have successfully " + action + " <strong>" + quantity + "</strong> units of <strong>" + assetName + "</strong>.</p>"
                    + "      <p style='margin-top:8px;'>Happy Investing!<br/><strong>Ro<span style='color:#ffd166;'>💳</span>Hit<span style='color:#ffd166;'>📈</span>Fi Wealth Management</strong></p>"
                    + "    </div>"
                    + "    <div class='footer'>"
                    + "      This is an automated confirmation. For details, check your portfolio in the dashboard."
                    + "    </div>"
                    + "  </div>"
                    + "</div>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Investment receipt email sent asynchronously to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send investment receipt to {}", toEmail, e);
        }
    }
}
