package com.safety.service;

import com.safety.config.ContentGuardProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 由 static 工具类改为 @Service，以便注入配置
 */
@Service
@RequiredArgsConstructor
public class TextProcessingService {

    private final ContentGuardProperties properties;

    public String extractSample(String title, String content) {
        // 使用配置阈值
        int threshold = properties.getText().getPremiumThreshold();
        int len = content.length();

        if (len <= threshold) {
            return title + "\n" + content;
        }

        int part10 = (int) (len * 0.1);
        String start = content.substring(0, part10);
        String end = content.substring(len - part10);

        int middleLen = len - (part10 * 2);
        int sampleSize = (int) (len * 0.2);
        int randomStart = part10 + new Random().nextInt(Math.max(1, middleLen - sampleSize));
        String middle = content.substring(randomStart, Math.min(len - part10, randomStart + sampleSize));

        return "Title:" + title + "\nStart:" + start + "\nMiddle:" + middle + "\nEnd:" + end;
    }

    public List<String> shardContent(String content) {
        // 使用配置的分片参数
        int chunkSize = properties.getText().getChunkSize();
        double overlapRatio = properties.getText().getOverlapRatio();

        List<String> chunks = new ArrayList<>();
        int len = content.length();
        int overlap = (int) (chunkSize * overlapRatio);
        int step = chunkSize - overlap;

        // 防止步长为0死循环
        if (step <= 0) step = 1;

        for (int i = 0; i < len; i += step) {
            int end = Math.min(len, i + chunkSize);
            chunks.add(content.substring(i, end));
            if (end == len) break;
        }
        return chunks;
    }
}