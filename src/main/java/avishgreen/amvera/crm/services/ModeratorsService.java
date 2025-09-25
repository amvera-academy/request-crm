package avishgreen.amvera.crm.services;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@Getter
@Slf4j
public class ModeratorsService {
    @Value("classpath:moderators.yml")
    private Resource settingsFile;

    private List<Long> moderatorsUserIdList;

    @PostConstruct
    public void init() {
        Yaml yaml = new Yaml();
        try {
            // Загружаем файл как Map
            Map<String, List<Map<String, Object>>> data = yaml.load(settingsFile.getInputStream());

            // Извлекаем список модераторов по ключу и преобразуем его
            List<Map<String, Object>> moderatorsRaw = data.get("moderatorsUserIds");

            if (moderatorsRaw != null) {
                this.moderatorsUserIdList = moderatorsRaw.stream()
                        .map(moderatorMap -> ((Number) moderatorMap.get("id")).longValue())
                        .collect(Collectors.toList());
            } else {
                log.error("Ключ 'moderatorsUserIds' не найден в файле moderators.yml");
                this.moderatorsUserIdList = Collections.emptyList();
            }
            log.info("Загружено {} ID ответственных пользователей из moderators.yml", this.moderatorsUserIdList.size());
        } catch (IOException e) {
            log.error("Не удалось загрузить moderators.yml", e);
            this.moderatorsUserIdList = Collections.emptyList();
        }
    }

    public boolean isModerator(Long telegramUserId) {
        return moderatorsUserIdList.contains(telegramUserId);
    }
}