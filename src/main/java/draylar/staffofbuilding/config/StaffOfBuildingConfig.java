package draylar.staffofbuilding.config;

public class StaffOfBuildingConfig {
    public int woodenSize = 3 * 3;
    public int stoneSize = 5 * 5;
    public int ironSize = 7 * 7;
    public int goldenSize = 9 * 9;
    public int diamondSize = 11 * 11;
    public int netheriteSize = 13 * 13;
    public int infiniteSize = 25 * 25;

    public String getName() {
        return "staff-of-building";
    }

    public String getExtension() {
        return "json5";
    }
}
