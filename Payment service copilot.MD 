# PAYMENT SERVICE - GITHUB COPILOT IMPLEMENTATION GUIDE

**Support: COD + VNPay Sandbox | Ready for Frontend Integration**

---

## 📊 SERVICE OVERVIEW

**Port:** 8085
**Database:** payment_db (PostgreSQL - port 5436)
**Payment Methods:** COD, VNPAY
**VNPay Mode:** Sandbox (Test)

---

## 📁 PROJECT STRUCTURE

```
payment-service/
├── pom.xml
├── src/main/java/com/ecommerce/paymentservice/
│   ├── PaymentServiceApplication.java
│   ├── config/
│   │   ├── VNPayConfig.java
│   │   └── WebClientConfig.java
│   ├── controller/
│   │   ├── PaymentController.java
│   │   └── VNPayCallbackController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreatePaymentRequest.java
│   │   │   └── RefundPaymentRequest.java
│   │   └── response/
│   │       ├── PaymentResponse.java
│   │       └── VNPayPaymentResponse.java
│   ├── entity/
│   │   ├── Payment.java
│   │   ├── PaymentMethod.java (enum)
│   │   └── PaymentStatus.java (enum)
│   ├── repository/
│   │   └── PaymentRepository.java
│   ├── service/
│   │   ├── PaymentService.java
│   │   └── VNPayPaymentService.java
│   ├── exception/
│   │   ├── PaymentNotFoundException.java
│   │   └── InvalidSignatureException.java
│   └── util/
│       └── VNPayUtil.java
└── src/main/resources/
    └── application.yml

Total: 19 files
Time: 2-3 hours
```

---

## 🔧 PHASE 1: Dependencies & Configuration (15 min)

### File 1: `pom.xml`

**Copilot Prompt:**
```xml
Add dependencies for Payment Service with VNPay integration:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Spring Boot Starter WebFlux (for WebClient)
- Commons Codec (for HMAC SHA-512)
- Lombok
- MapStruct
- Eureka Client
```

**Expected Code:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

---

### File 2: `application.yml`

**Copilot Prompt:**
```yaml
Configure Payment Service with:
- Port 8085
- PostgreSQL database payment_db on port 5436
- VNPay Sandbox config (tmn-code: DEMO, hash-secret: DEMOHASHSECRET)
- Eureka client
```

**Expected Code:**
```yaml
server:
  port: 8085

spring:
  application:
    name: payment-service

  datasource:
    url: jdbc:postgresql://localhost:5436/payment_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

# VNPay Sandbox Configuration
vnpay:
  tmn-code: DEMO
  hash-secret: DEMOHASHSECRET
  url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  return-url: http://localhost:8085/api/payments/vnpay/callback
  version: 2.1.0
  command: pay

# Order Service URL (for callbacks)
order-service:
  url: http://localhost:8084

# Frontend URL (for redirects)
frontend:
  url: http://localhost:3000

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

---

## 🗄️ PHASE 2: Entities & Enums (20 min)

### File 3: `entity/PaymentMethod.java`

**Copilot Prompt:**
```java
Create PaymentMethod enum with values: COD, VNPAY
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.entity;

public enum PaymentMethod {
    COD,      // Cash on Delivery
    VNPAY     // VNPay online payment
}
```

---

### File 4: `entity/PaymentStatus.java`

**Copilot Prompt:**
```java
Create PaymentStatus enum with values: PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.entity;

public enum PaymentStatus {
    PENDING,      // Created, waiting for payment
    SUCCESS,      // Payment successful
    FAILED,       // Payment failed
    CANCELLED,    // Payment cancelled
    REFUNDED      // Payment refunded
}
```

---

### File 5: `entity/Payment.java`

**Copilot Prompt:**
```java
Create Payment entity with JPA annotations:
Fields:
- id (Long, auto-generated)
- paymentNumber (String, unique, format: PAY-YYYYMMDD-XXXX)
- orderId (String)
- orderNumber (String)
- userId (String)
- amount (BigDecimal)
- currency (String, default VND)
- paymentMethod (PaymentMethod enum)
- status (PaymentStatus enum)
- vnpayTransactionNo (String, nullable)
- vnpayBankCode (String, nullable)
- vnpayCardType (String, nullable)
- transactionId (String, nullable)
- description (String, nullable)
- failureReason (String, nullable)
- createdAt (LocalDateTime)
- updatedAt (LocalDateTime)
- paidAt (LocalDateTime, nullable)

Add @PrePersist and @PreUpdate for timestamps
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String paymentNumber;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 100)
    private String vnpayTransactionNo;

    @Column(length = 50)
    private String vnpayBankCode;

    @Column(length = 50)
    private String vnpayCardType;

    @Column(length = 100)
    private String transactionId;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 📝 PHASE 3: DTOs (25 min)

### File 6: `dto/request/CreatePaymentRequest.java`

**Copilot Prompt:**
```java
Create CreatePaymentRequest DTO with validation:
- orderId (required)
- orderNumber (required)
- userId (required)
- amount (required, min 1000)
- paymentMethod (required: COD or VNPAY)
- description (optional)
Add Lombok annotations
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.dto.request;

import com.ecommerce.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Amount must be at least 1000 VND")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String description;
}
```

---

### File 7: `dto/response/PaymentResponse.java`

**Copilot Prompt:**
```java
Create PaymentResponse DTO for API responses:
- id
- paymentNumber
- orderId
- orderNumber
- amount
- currency
- paymentMethod
- status
- vnpayTransactionNo
- transactionId
- paymentUrl (for VNPAY, nullable)
- createdAt
- paidAt
Add Lombok annotations
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.dto.response;

import com.ecommerce.paymentservice.entity.PaymentMethod;
import com.ecommerce.paymentservice.entity.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private String orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String vnpayTransactionNo;
    private String transactionId;
    private String paymentUrl;  // For VNPAY redirect
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
```

---

## 🔧 PHASE 4: VNPay Utility (30 min)

### File 8: `util/VNPayUtil.java`

**Copilot Prompt:**
```java
Create VNPayUtil class with static methods:

1. hmacSHA512(String key, String data) - Generate HMAC SHA-512 hash
   - Use javax.crypto.Mac with HmacSHA512
   - Return hex string

2. buildQueryString(Map<String, String> params) - Build sorted query string
   - Sort params alphabetically by key
   - URL encode values
   - Join with & separator
   - Skip null/empty values

3. getRandomNumber(int len) - Generate random number string
   - Use Random class
   - Return numeric string

4. getCurrentDateTime() - Get current datetime in format yyyyMMddHHmmss
   - Use SimpleDateFormat
   - Return formatted string
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VNPayUtil {

    /**
     * Generate HMAC SHA-512 hash.
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
            );
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA-512", e);
        }
    }

    /**
     * Build query string from parameters (sorted alphabetically).
     */
    public static String buildQueryString(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append("=");
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append("&");
                }
            }
        }
        return query.toString();
    }

    /**
     * Generate random number string.
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Get current datetime in format yyyyMMddHHmmss.
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        return formatter.format(calendar.getTime());
    }
}
```

---

## ⚙️ PHASE 5: Configuration (15 min)

### File 9: `config/VNPayConfig.java`

**Copilot Prompt:**
```java
Create VNPayConfig with @Configuration and @Getter:
- tmnCode (from application.yml)
- hashSecret (from application.yml)
- vnpayUrl (from application.yml)
- returnUrl (from application.yml)
- version (from application.yml)
- command (from application.yml)
Use @Value annotations
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;
}
```

---

### File 10: `config/WebClientConfig.java`

**Copilot Prompt:**
```java
Create WebClientConfig with @Configuration:
- Create orderServiceClient WebClient bean
- Base URL from application.yml: ${order-service.url}
- Set default headers: Content-Type = application/json
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${order-service.url}")
    private String orderServiceUrl;

    @Bean
    public WebClient orderServiceClient() {
        return WebClient.builder()
            .baseUrl(orderServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

---

## 💼 PHASE 6: Services (45 min)

### File 11: `service/VNPayPaymentService.java`

**Copilot Prompt:**
```java
Create VNPayPaymentService with @Service:

Method 1: createPaymentUrl(Payment payment, String ipAddress)
- Build VNPay params map with:
  vnp_Version, vnp_Command, vnp_TmnCode, vnp_Amount (amount * 100),
  vnp_CurrCode=VND, vnp_TxnRef (payment number),
  vnp_OrderInfo, vnp_OrderType=other, vnp_Locale=vn,
  vnp_ReturnUrl, vnp_IpAddr, vnp_CreateDate, vnp_ExpireDate (+15 min)
- Build query string using VNPayUtil.buildQueryString()
- Sign with VNPayUtil.hmacSHA512(hashSecret, queryString)
- Build final URL: vnpayUrl + ? + queryString + &vnp_SecureHash=signature
- Return PaymentResponse with paymentUrl

Method 2: processCallback(Map<String, String> params)
- Extract vnp_SecureHash from params
- Remove vnp_SecureHash and vnp_SecureHashType from params
- Build query string from remaining params
- Calculate signature with VNPayUtil.hmacSHA512()
- Verify signature matches
- Get payment by vnp_TxnRef (payment number)
- Check vnp_ResponseCode:
  - "00" = SUCCESS: Update payment status, save transaction details, set paidAt
  - Other = FAILED: Update status, save failure reason
- Return updated payment

Add proper logging and exception handling
```

---

### File 12: `service/PaymentService.java`

**Copilot Prompt:**
```java
Create PaymentService with @Service and @Transactional:

Method 1: createPayment(CreatePaymentRequest request, String ipAddress)
- Generate payment number: PAY-YYYYMMDD-XXXX (random 4 digits)
- Build Payment entity from request
- Set status = PENDING
- Set currency = VND
- Save to repository
- If paymentMethod == VNPAY:
  - Call vnpayPaymentService.createPaymentUrl()
  - Return PaymentResponse with paymentUrl
- If paymentMethod == COD:
  - Return PaymentResponse without paymentUrl
- Use mapper to convert entity to response

Method 2: getPaymentById(Long id)
- Find payment by id or throw PaymentNotFoundException
- Return PaymentResponse

Method 3: getPaymentByOrderId(String orderId)
- Find payment by orderId or throw PaymentNotFoundException
- Return PaymentResponse

Method 4: confirmPayment(String paymentNumber)
- Find payment by paymentNumber
- If status == SUCCESS:
  - Call Order Service: POST /api/orders/{orderId}/payment-confirmed
  - Body: { paymentId, status: "SUCCESS" }
- Return updated payment

Inject: PaymentRepository, VNPayPaymentService, WebClient, PaymentMapper
Add logging for all operations
```

---

## 🎮 PHASE 7: Controllers (30 min)

### File 13: `controller/PaymentController.java`

**Copilot Prompt:**
```java
Create PaymentController with @RestController and @RequestMapping("/api/payments"):

Endpoint 1: POST /create
- @RequestBody CreatePaymentRequest (validated)
- HttpServletRequest to get client IP
- Extract IP from X-Forwarded-For header or X-Real-IP or remoteAddr
- Call paymentService.createPayment(request, ipAddress)
- Return ResponseEntity<PaymentResponse>

Endpoint 2: GET /{id}
- @PathVariable Long id
- Call paymentService.getPaymentById(id)
- Return ResponseEntity<PaymentResponse>

Endpoint 3: GET /order/{orderId}
- @PathVariable String orderId
- Call paymentService.getPaymentByOrderId(orderId)
- Return ResponseEntity<PaymentResponse>

Endpoint 4: POST /{id}/confirm
- Internal endpoint called by VNPay callback handler
- @PathVariable Long id
- Call paymentService.confirmPayment(id)
- Return ResponseEntity<PaymentResponse>

Add @Slf4j for logging
Add proper exception handling with @ExceptionHandler
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        HttpServletRequest httpRequest
    ) {
        log.info("Creating payment for order: {}", request.getOrderNumber());
        String ipAddress = getClientIp(httpRequest);
        PaymentResponse response = paymentService.createPayment(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        log.info("Getting payment: {}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable String orderId) {
        log.info("Getting payment for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

---

### File 14: `controller/VNPayCallbackController.java`

**Copilot Prompt:**
```java
Create VNPayCallbackController with @Controller (not RestController):

Endpoint: GET /api/payments/vnpay/callback
- @RequestParam Map<String, String> params (all VNPay callback params)
- Call vnpayPaymentService.processCallback(params)
- Get updated payment
- If payment status == SUCCESS:
  - Call paymentService.confirmPayment(payment.getPaymentNumber())
- Redirect to frontend:
  - Success: {frontendUrl}/payment/result?orderId={orderId}&status=SUCCESS&paymentNumber={paymentNumber}
  - Failed: {frontendUrl}/payment/result?orderId={orderId}&status=FAILED&reason={reason}
- Return RedirectView
- Add try-catch for errors and redirect to error page

Inject: VNPayPaymentService, PaymentService
Frontend URL from @Value("${frontend.url}")
Add logging
```

**Expected Code:**
```java
package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.PaymentStatus;
import com.ecommerce.paymentservice.service.PaymentService;
import com.ecommerce.paymentservice.service.VNPayPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackController {

    private final VNPayPaymentService vnpayPaymentService;
    private final PaymentService paymentService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/api/payments/vnpay/callback")
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("Received VNPay callback with params: {}", params);

        try {
            // Process callback and update payment
            Payment payment = vnpayPaymentService.processCallback(params);

            // If success, notify Order Service
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                paymentService.confirmPayment(payment.getPaymentNumber());
            }

            // Build redirect URL
            String redirectUrl = buildRedirectUrl(payment);
            log.info("Redirecting to: {}", redirectUrl);

            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing VNPay callback", e);
            String errorUrl = frontendUrl + "/payment/error?message=" +
                URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView(errorUrl);
        }
    }

    private String buildRedirectUrl(Payment payment) {
        StringBuilder url = new StringBuilder(frontendUrl);
        url.append("/payment/result");
        url.append("?orderId=").append(payment.getOrderId());
        url.append("&status=").append(payment.getStatus());
        url.append("&paymentNumber=").append(payment.getPaymentNumber());

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            url.append("&transactionId=").append(
                payment.getTransactionId() != null ? payment.getTransactionId() : ""
            );
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            url.append("&reason=").append(
                URLEncoder.encode(
                    payment.getFailureReason() != null ? payment.getFailureReason() : "Unknown",
                    StandardCharsets.UTF_8
                )
            );
        }

        return url.toString();
    }
}
```

---

## 📡 API ENDPOINTS SUMMARY

### **Public Endpoints (Frontend):**

```
1. POST /api/payments/create
   Body: {
     "orderId": "123",
     "orderNumber": "ORD-20240422-0001",
     "userId": "user123",
     "amount": 1000000,
     "paymentMethod": "VNPAY",  // or "COD"
     "description": "Payment for order"
   }
   Response: {
     "id": 1,
     "paymentNumber": "PAY-20240422-0001",
     "orderId": "123",
     "orderNumber": "ORD-20240422-0001",
     "amount": 1000000,
     "currency": "VND",
     "paymentMethod": "VNPAY",
     "status": "PENDING",
     "paymentUrl": "https://sandbox.vnpayment.vn/...",  // Only for VNPAY
     "createdAt": "2024-04-22T10:30:00"
   }

2. GET /api/payments/{id}
   Response: PaymentResponse

3. GET /api/payments/order/{orderId}
   Response: PaymentResponse

4. GET /api/payments/vnpay/callback (VNPay redirects here)
   Params: vnp_Amount, vnp_ResponseCode, vnp_SecureHash, etc.
   Redirects to: {frontendUrl}/payment/result?...
```

---

## 🧪 TESTING

### **Test COD Payment:**
```bash
curl -X POST http://localhost:8085/api/payments/create \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "123",
    "orderNumber": "ORD-20240422-0001",
    "userId": "user123",
    "amount": 1000000,
    "paymentMethod": "COD"
  }'

# Expected: 200 OK with payment response (no paymentUrl)
```

### **Test VNPay Payment:**
```bash
curl -X POST http://localhost:8085/api/payments/create \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "124",
    "orderNumber": "ORD-20240422-0002",
    "userId": "user123",
    "amount": 1000000,
    "paymentMethod": "VNPAY"
  }'

# Expected: 200 OK with paymentUrl
# Copy paymentUrl and open in browser
# Use test card: 9704198526191432198, OTP: 123456
```

---

## ✅ IMPLEMENTATION CHECKLIST

```
Setup:
[ ] pom.xml dependencies added
[ ] application.yml configured
[ ] Database payment_db created

Entities & Enums:
[ ] PaymentMethod enum created
[ ] PaymentStatus enum created
[ ] Payment entity created with all fields

DTOs:
[ ] CreatePaymentRequest created with validation
[ ] PaymentResponse created

Utilities:
[ ] VNPayUtil created with all 4 methods
[ ] HMAC SHA-512 tested
[ ] Query string builder tested

Configuration:
[ ] VNPayConfig created
[ ] WebClientConfig created

Services:
[ ] VNPayPaymentService created
[ ] PaymentService created
[ ] All methods implemented

Controllers:
[ ] PaymentController created
[ ] VNPayCallbackController created
[ ] All endpoints tested

Database:
[ ] payments table created
[ ] Indexes added

Integration:
[ ] Can create COD payment
[ ] Can create VNPay payment
[ ] VNPay URL generated correctly
[ ] Callback processed correctly
[ ] Order Service notified on success
[ ] Frontend redirect works
```

---

**TOTAL: 14 Files | 2-3 Hours | Ready for Frontend Integration! 🚀**