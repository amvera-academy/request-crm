package avishgreen.amvera.crm.services.telegramhandlers;

import avishgreen.amvera.crm.pojo.SpamCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramAnispamHandler {
    private final RestTemplate restTemplate;

    // Внедрение значений из application.yml
    @Value("${application.antispam.url}")
    private String apiUrl;

    @Value("${application.antispam.bearer-token}")
    private String bearerToken;

    @SneakyThrows
    public boolean isSpam(Integer messageId){
        Thread.sleep(Duration.ofSeconds(10));

        String fullUrl = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("messageId", messageId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            //дернем ручку проверятора на спам
            ResponseEntity<SpamCheckResponse> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    SpamCheckResponse.class
            );

            // проверим статус ответа и тело
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Anti-spam check for message {} successful. Spam status: {}", messageId, response.getBody().isSpam());
                return response.getBody().isSpam();
            }

            // Если статус не OK (например, 204 No Content), считаем, что это не спам (или ошибка)
            log.warn("Anti-spam API returned non-OK status {} for message {}. Defaulting to non-spam (false).", response.getStatusCode(), messageId);
            return false;

        } catch (HttpClientErrorException e) {
            // 5. Обработка ошибок HTTP (4xx, 5xx)
            log.error("HTTP error during anti-spam check for message {}: Status: {}, Body: {}", messageId, e.getStatusCode(), e.getResponseBodyAsString());
            return false; // При ошибке возвращаем false, как запрашивалось
        } catch (ResourceAccessException e) {
            // 6. Обработка проблем с сетью (таймауты, недоступность)
            log.error("Network access error during anti-spam check for message {}: {}", messageId, e.getMessage());
            return false;
        } catch (Exception e) {
            // 7. Обработка других неожиданных ошибок
            log.error("Unexpected error during anti-spam check for message {}: {}", messageId, e.getMessage());
            return false;
        }

    }
}
