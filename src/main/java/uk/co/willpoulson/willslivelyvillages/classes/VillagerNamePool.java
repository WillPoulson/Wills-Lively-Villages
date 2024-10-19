package uk.co.willpoulson.willslivelyvillages.classes;

import java.util.List;

public class VillagerNamePool {
    private final List<String> firstNames;
    private final List<String> lastNames;

    public VillagerNamePool(List<String> firstNames, List<String> lastNames) {
        this.firstNames = firstNames;
        this.lastNames = lastNames;
    }

    public List<String> getFirstNames() {
        return firstNames;
    }

    public List<String> getLastNames() {
        return lastNames;
    }
}