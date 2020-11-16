package io.github.mikhirurg.jbackup.model;

import java.time.LocalDateTime;
import java.util.Arrays;

public class CleaningAlgorithm {

    private enum FlagType {
        AMOUNT_LIMIT(0),
        VOLUME_LIMIT(1),
        DATE_LIMIT(2),
        SIZE(3);

        private final int val;

        public int getVal() {
            return val;
        }

        FlagType(int type) {
            this.val = type;
        }
    }

    private Integer amountLimit = null;
    private LocalDateTime minDate = null;
    private Long volumeLimit = null;
    private boolean removeIfAny = false;
    private final Boolean[] flags;

    private CleaningAlgorithm() {
        flags = new Boolean[FlagType.SIZE.getVal()];
    }

    public static CleaningAlgorithm createCleaningAlgorithm() {
        return new CleaningAlgorithm();
    }

    public CleaningAlgorithm addAmountLimit(int amountLimit) {
        this.amountLimit = amountLimit;
        return this;
    }

    public CleaningAlgorithm addVolumeLimit(long volumeLimit) {
        this.volumeLimit = volumeLimit;
        return this;
    }

    public CleaningAlgorithm addMinDate(LocalDateTime minDate) {
        this.minDate = minDate;
        return this;
    }

    public CleaningAlgorithm removeIfAll() {
        removeIfAny = false;
        return this;
    }

    public CleaningAlgorithm removeIfAny() {
        removeIfAny = true;
        return this;
    }

    public boolean checkPoint(long totalVolume, long totalAmount, RestorePoint point) {

        if (volumeLimit == null && amountLimit == null && minDate == null) {
            return false;
        }

        if (volumeLimit != null) {
            flags[FlagType.VOLUME_LIMIT.getVal()] = totalVolume + point.getVolume() > volumeLimit;
        }

        if (amountLimit != null) {
            flags[FlagType.AMOUNT_LIMIT.getVal()] = totalAmount + 1 > amountLimit;
        }

        if (minDate != null) {
            flags[FlagType.DATE_LIMIT.getVal()] = point.getCreationDate().compareTo(minDate) < 0;
        }

        if (removeIfAny) {
            return Arrays.stream(flags).anyMatch(e -> e != null && e);
        }

        return Arrays.stream(flags).allMatch(e -> e == null || e);
    }
}
