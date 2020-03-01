package yay.linda.mydaybackend.model;

import lombok.Getter;

@Getter
public enum CountUpdateType {

    INCREMENT(1),
    DECREMENT(-1);

    private int amount;

    CountUpdateType(int amount) {
        this.amount = amount;
    }

}
