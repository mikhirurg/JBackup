package io.github.mikhirurg.jbackup.model;

import java.time.LocalDateTime;

public class FixtureUtils {
    public static class Time {
        private static boolean isUsingSysTime = true;
        private static LocalDateTime startDate;

        public static void useSystemTime() {
            isUsingSysTime = true;
        }

        public static void useMockTime(LocalDateTime date) {
            isUsingSysTime = false;
            startDate = date;
        }

        public static LocalDateTime getCurrentTime() {
            return isUsingSysTime ? LocalDateTime.now() : startDate;
        }
    }
}
