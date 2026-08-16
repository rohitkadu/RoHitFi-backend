package com.rohitfi.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL:rohitkadufreelance@gmail.com}")
    private String senderEmail;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }

    /**
     * Centralized method to call Brevo SMTP API.
     * Includes explicit status checking and robust error extraction.
     */
    private void sendViaBrevoApi(String toEmail, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("BREVO_API_KEY is not set. Skipping email dispatch to {}", toEmail);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", "RoHitFi Digital Banking & Finance", "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            ResponseEntity<Void> response = restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ SUCCESS: Email dispatched to {} | Status: {}", toEmail, response.getStatusCode());
            } else {
                log.warn("⚠️ WARNING: Brevo API returned unexpected status {} for email to {}", response.getStatusCode(), toEmail);
            }

        } catch (RestClientResponseException e) {
            // This specifically catches HTTP 4xx and 5xx errors from Brevo (e.g., Bad API Key, Unverified Sender)
            log.error("❌ CRITICAL EMAIL FAILURE: Brevo API rejected the email to {}.", toEmail);
            log.error("   ↳ HTTP Status: {}", e.getRawStatusCode());
            log.error("   ↳ Response Body: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            // Catches general network timeouts or Java errors
            log.error("❌ NETWORK ERROR: Failed to reach Brevo REST API for {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }

    /* -------------------------
       Shared style helpers
       ------------------------- */

    private String wrapInBaseTemplate(String title, String preheader, String bodyHtml) {
        // Modern, mobile-friendly wrapper with improved spacing and a refined logo block
        return "<!doctype html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='utf-8'/>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'/>"
                + "<title>" + escapeHtml(title) + "</title>"
                + "</head>"
                + "<body style='margin:0; padding:28px; background:#f6f8fb; font-family: \"Inter\", \"Segoe UI\", Roboto, Arial, sans-serif;'>"
                + "<div style='max-width:720px; margin:0 auto;'>"
                + "  <div style='display:flex; align-items:center; gap:16px; margin-bottom:18px;'>"
                + "    <div style='width:64px; height:64px; border-radius:14px; display:flex; align-items:center; justify-content:center; "
                + "                background:linear-gradient(135deg,#7c3aed,#06b6d4); color:white; font-weight:800; font-size:20px; box-shadow:0 8px 24px rgba(12,18,40,0.12);'>"
                + "      RF"
                + "    </div>"
                + "    <div style='line-height:1.05;'>"
                + "      <div style='font-size:18px; color:#0f172a; font-weight:800; margin-bottom:4px;'>" + escapeHtml(title) + "</div>"
                + "      <div style='font-size:13px; color:#6b7280;'>" + escapeHtml(preheader) + "</div>"
                + "    </div>"
                + "  </div>"

                + "  <div style='background:linear-gradient(180deg,#ffffff,#fbfdff); border-radius:14px; padding:20px; box-shadow:0 10px 30px rgba(2,6,23,0.06); border:1px solid rgba(15,23,42,0.03);'>"
                + bodyHtml
                + "  </div>"

                + "  <div style='margin-top:14px; font-size:13px; color:#6b7280;'>"
                + "    If you didn't expect this email, please ignore it or contact support at <a href='mailto:support@rohitfi.com' style='color:#7c3aed; text-decoration:none;'>support@rohitfi.com</a>."
                + "  </div>"

                + "  <div style='text-align:center; margin-top:18px; color:#9ca3af; font-size:12px;'>© " + LocalDateTime.now().getYear() + " RoHitFi Digital Banking & Finance</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /* -------------------------
       Email templates (modern / GenZ aesthetic)
       ------------------------- */

    @Async
    public void sendWelcomeEmail(String toEmail, String mobile) {
        String subject = "🎉 Welcome to RoHitFi Digital Banking & Finance!";
        String preheader = "Your account is ready — let's get you started.";
        String body = ""
                + "<div style='padding:18px; border-radius:12px; background:linear-gradient(180deg,#ffffff,#fbfdff);'>"
                + "  <h1 style='margin:0; font-size:20px; color:#0f172a;'>👋 Hey there!</h1>"
                + "  <p style='color:#374151; font-size:14px; margin-top:10px;'>Welcome to <strong>RoHitFi</strong>. Your account linked to <strong>" + escapeHtml(mobile) + "</strong> is now active.</p>"

                + "  <div style='margin-top:16px; padding:14px; border-radius:12px; background:linear-gradient(90deg,#f8fafc,#f3f7fb); display:flex; gap:12px; align-items:center;'>"
                + "    <div style='width:52px; height:52px; border-radius:10px; background:#eef2ff; display:flex; align-items:center; justify-content:center; color:#4f46e5; font-size:20px;'>🔒</div>"
                + "    <div style='font-size:13px; color:#374151;'>Security note: Your session tokens (JWT) are valid for 24 hours. Never share your credentials.</div>"
                + "  </div>"

                + "  <div style='margin-top:18px;'>"
                + "    <a href='https://rohitfi-backend.onrender.com/login' style='display:inline-block; padding:12px 18px; border-radius:10px; "
                + "       background:linear-gradient(90deg,#7c3aed,#06b6d4); color:white; text-decoration:none; font-weight:700; box-shadow:0 8px 20px rgba(12,18,40,0.12);'>"
                + "      Get started"
                + "    </a>"
                + "  </div>"

                + "  <p style='margin-top:16px; color:#6b7280; font-size:13px;'>Cheers,<br/><strong>RoHitFi Security Team</strong></p>"
                + "</div>";

        sendViaBrevoApi(toEmail, subject, wrapInBaseTemplate(subject, preheader, body));
    }

    @Async
    public void sendTransactionReceipt(String toEmail, String refNo, BigDecimal amount, String type, BigDecimal balanceAfter) {
        String subject = "💳 Transaction Alert • " + refNo;
        String preheader = "A transaction just happened on your account.";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
        String accent = type.equalsIgnoreCase("CREDIT") ? "#16a34a" : "#ef4444";

        String body = ""
                + "<div style='padding:18px; border-radius:12px; background:#ffffff;'>"
                + "  <h2 style='margin:0; font-size:18px; color:#0f172a;'>Transaction confirmed</h2>"
                + "  <p style='color:#374151; margin-top:10px;'>We processed a transaction on your account. Details below.</p>"

                + "  <div style='margin-top:12px; border-radius:10px; overflow:hidden; border:1px solid #eef2f7;'>"
                + "    <table style='width:100%; border-collapse:collapse; font-size:14px;'>"
                + row("Reference", refNo)
                + row("Date", dateStr)
                + rowWithColor("Type", type, accent)
                + row("Amount", "₹" + amount)
                + row("Available Balance", "₹" + balanceAfter)
                + "    </table>"
                + "  </div>"

                + "  <div style='margin-top:14px; padding:12px; border-radius:10px; background:#f8fafc; color:#374151; font-size:13px;'>"
                + "    If you did not authorize this, lock your account immediately from the dashboard or contact support."
                + "  </div>"
                + "</div>";

        sendViaBrevoApi(toEmail, subject, wrapInBaseTemplate(subject, preheader, body));
    }

    @Async
    public void sendLoanDisbursementEmail(String toEmail, String loanType, BigDecimal amount,
                                          BigDecimal emiAmount, Integer tenureMonths,
                                          BigDecimal interestRate, BigDecimal accountBalance) {
        String subject = "💰 Loan Disbursed • RoHitFi";
        String preheader = "Funds have been credited to your account.";
        String body = ""
                + "<div style='padding:18px; border-radius:12px; background:#ffffff;'>"
                + "  <h2 style='margin:0; font-size:18px; color:#0f172a;'>Loan Disbursed 🎉</h2>"
                + "  <p style='color:#374151; margin-top:10px;'>Your <strong>" + escapeHtml(loanType) + "</strong> loan has been approved and disbursed.</p>"

                + "  <div style='margin-top:12px; border-radius:10px; overflow:hidden; border:1px solid #eef2f7;'>"
                + "    <table style='width:100%; border-collapse:collapse; font-size:14px;'>"
                + row("Loan Type", loanType)
                + row("Disbursed Amount", "₹" + amount)
                + row("Interest Rate", interestRate + "% p.a.")
                + row("Tenure", tenureMonths + " months")
                + row("Monthly EMI", "₹" + emiAmount)
                + row("Updated Balance", "₹" + accountBalance)
                + "    </table>"
                + "  </div>"

                + "  <p style='margin-top:12px; color:#6b7280; font-size:13px;'>Your EMI schedule is active. View full details in your dashboard.</p>"
                + "</div>";

        sendViaBrevoApi(toEmail, subject, wrapInBaseTemplate(subject, preheader, body));
    }

    @Async
    public void sendInvestmentReceiptEmail(String toEmail, String assetName, int quantity, BigDecimal totalAmount, String orderType) {
        String subject = "📈 Trade Confirmed • " + assetName;
        String preheader = "Your investment order has been executed.";
        String color = orderType.equalsIgnoreCase("BUY") ? "#16a34a" : "#ef4444";
        String action = orderType.equalsIgnoreCase("BUY") ? "purchased" : "sold";

        String body = ""
                + "<div style='padding:18px; border-radius:12px; background:#ffffff;'>"
                + "  <h2 style='margin:0; font-size:18px; color:#0f172a;'>Trade Confirmation</h2>"
                + "  <p style='color:#374151; margin-top:10px;'>You have " + escapeHtml(action) + " <strong>" + quantity + "</strong> units of <strong>" + escapeHtml(assetName) + "</strong>.</p>"

                + "  <div style='margin-top:12px; border-radius:10px; overflow:hidden; border:1px solid #eef2f7;'>"
                + "    <table style='width:100%; border-collapse:collapse; font-size:14px;'>"
                + rowWithColor("Order Type", orderType, color)
                + row("Asset", assetName)
                + row("Quantity", quantity + " Units")
                + row("Total Value", "₹" + totalAmount)
                + "    </table>"
                + "  </div>"

                + "  <p style='margin-top:12px; color:#6b7280; font-size:13px;'>This is your official trade receipt. Check your portfolio for updated holdings.</p>"
                + "</div>";

        sendViaBrevoApi(toEmail, subject, wrapInBaseTemplate(subject, preheader, body));
    }

    /* -------------------------
       Small HTML helpers for table rows
       ------------------------- */

    private String row(String label, String value) {
        return "<tr style='border-top:1px solid #eef2f7;'><td style='padding:12px 10px; color:#6b7280; width:40%; vertical-align:top;'><strong>"
                + escapeHtml(label) + "</strong></td>"
                + "<td style='padding:12px 10px; color:#0f172a;'>" + escapeHtml(value) + "</td></tr>";
    }

    private String rowWithColor(String label, String value, String color) {
        return "<tr style='border-top:1px solid #eef2f7;'><td style='padding:12px 10px; color:#6b7280; width:40%; vertical-align:top;'><strong>"
                + escapeHtml(label) + "</strong></td>"
                + "<td style='padding:12px 10px; color:" + color + "; font-weight:700;'>" + escapeHtml(value) + "</td></tr>";
    }
}
